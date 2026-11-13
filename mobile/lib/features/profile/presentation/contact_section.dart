import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/info_card.dart';
import '../../../shared/submit_mixin.dart';
import '../domain/contact_confirmation.dart';
import '../domain/profile.dart';
import 'otp_channel_sheet.dart';
import 'profile_providers.dart';

class ContactSection extends ConsumerStatefulWidget {
  const ContactSection({super.key, required this.profile});

  final Profile profile;

  @override
  ConsumerState<ContactSection> createState() => _ContactSectionState();
}

class _ContactSectionState extends ConsumerState<ContactSection>
    with SubmitMixin {
  Profile get profile => widget.profile;

  Future<void> _verifyPhone() async {
    final delivery = await showOtpChannelSheet(context);
    if (delivery == null) return;
    await submit(() async {
      await ref.read(profileRepositoryProvider).sendPhoneCode(delivery);
      if (!mounted) return;
      context.push(
        Routes.confirmPhone,
        extra: ContactConfirmation(target: profile.phone!, delivery: delivery),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return InfoCard(
      title: ProfileText.contactDetails,
      children: [
        _ContactRow(
          icon: AppIcons.envelope,
          label: Strings.email,
          value: profile.email,
          verified: profile.emailVerified,
          addLabel: ProfileText.addEmail,
          onAdd: () => context.push(Routes.addEmail),
        ),
        const Divider(height: AppSpacing.lg),
        _ContactRow(
          icon: AppIcons.phone,
          label: Strings.phone,
          value: profile.phone,
          verified: profile.phoneVerified,
          addLabel: ProfileText.addPhone,
          onAdd: () => context.push(Routes.addPhone),
          onVerify: _verifyPhone,
          verifying: loading,
        ),
      ],
    );
  }
}

class _ContactRow extends StatelessWidget {
  const _ContactRow({
    required this.icon,
    required this.label,
    required this.value,
    required this.verified,
    required this.addLabel,
    required this.onAdd,
    this.onVerify,
    this.verifying = false,
  });

  final IconData icon;
  final String label;
  final String? value;
  final bool verified;
  final String addLabel;
  final VoidCallback onAdd;
  final VoidCallback? onVerify;
  final bool verifying;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = verified ? AppColors.success : AppColors.textSecondary;
    if (value == null) {
      return ListTile(
        contentPadding: EdgeInsets.zero,
        leading: Icon(icon, color: AppColors.primary),
        title: Text(
          addLabel,
          style: theme.textTheme.bodyMedium?.copyWith(
            color: AppColors.primary,
            fontWeight: FontWeight.w700,
          ),
        ),
        trailing: Icon(AppIcons.plus, color: AppColors.primary),
        onTap: onAdd,
      );
    }
    return Row(
      children: [
        Icon(icon, color: AppColors.textSecondary),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
              Text(
                value!,
                style: theme.textTheme.bodyMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
        if (!verified && onVerify != null)
          TextButton(
            onPressed: verifying ? null : onVerify,
            child: Text(ProfileText.verifyAction),
          )
        else
          Text(
            verified ? ProfileText.verified : ProfileText.notVerified,
            style: theme.textTheme.bodySmall?.copyWith(
              color: color,
              fontWeight: FontWeight.w700,
            ),
          ),
      ],
    );
  }
}
