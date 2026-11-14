import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/session/session_controller.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/otp_delivery.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/otp_channel_sheet.dart';
import '../../../shared/widgets/primary_button.dart';
import '../../auth/presentation/widgets/auth_page.dart';
import '../domain/contact_confirmation.dart';
import '../domain/contact_kind.dart';
import '../domain/profile.dart';
import 'contact_status_tile.dart';
import 'contact_step.dart';
import 'profile_providers.dart';

class CompleteProfilePage extends ConsumerStatefulWidget {
  const CompleteProfilePage({super.key});

  @override
  ConsumerState<CompleteProfilePage> createState() =>
      _CompleteProfilePageState();
}

class _CompleteProfilePageState extends ConsumerState<CompleteProfilePage>
    with SubmitMixin {
  Future<void> _run(Profile profile, ContactStep step) async {
    if (!step.existing) {
      await context.push(
        step.kind == ContactKind.phone ? Routes.addPhone : Routes.addEmail,
      );
      return;
    }
    final isPhone = step.kind == ContactKind.phone;
    final OtpDelivery? delivery = isPhone
        ? await showOtpChannelSheet(context)
        : null;
    if (isPhone && delivery == null) return;
    await submit(() async {
      final repository = ref.read(profileRepositoryProvider);
      isPhone
          ? await repository.sendPhoneCode(delivery!)
          : await repository.sendEmailCode();
      if (!mounted) return;
      await context.push(
        isPhone ? Routes.confirmPhone : Routes.confirmEmail,
        extra: ContactConfirmation(
          target: (isPhone ? profile.phone : profile.email)!,
          delivery: delivery,
          existing: true,
        ),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final value = ref.watch(profileProvider);
    final profile = value.value;
    if (profile == null) {
      return Scaffold(
        body: AsyncBody(
          value: value,
          onRetry: () => ref.invalidate(profileProvider),
          builder: (_) => const SizedBox.shrink(),
        ),
      );
    }
    final step = ContactStep.next(profile);
    return AuthPage(
      title: ContactGateText.title,
      subtitle: ContactGateText.body,
      showBack: false,
      children: [
        ContactStatusTile(
          icon: AppIcons.phone,
          label: ContactGateText.phone,
          value: profile.phone,
          verified: profile.phoneVerified,
        ),
        const SizedBox(height: AppSpacing.md),
        ContactStatusTile(
          icon: AppIcons.envelope,
          label: ContactGateText.email,
          value: profile.email,
          verified: profile.emailVerified,
        ),
        const SizedBox(height: AppSpacing.xl),
        if (step != null)
          PrimaryButton(
            label: step.label,
            loading: loading,
            onPressed: () => _run(profile, step),
          ),
        const SizedBox(height: AppSpacing.sm),
        TextButton(
          onPressed: () =>
              ref.read(sessionControllerProvider.notifier).signOut(),
          child: Text(ContactGateText.signOut),
        ),
      ],
    );
  }
}
