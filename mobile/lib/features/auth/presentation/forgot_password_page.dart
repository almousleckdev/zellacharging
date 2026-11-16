import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/password_setup_text.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/otp_delivery.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/otp_channel_sheet.dart';
import '../../../shared/widgets/primary_button.dart';
import '../domain/contact_channel.dart';
import '../domain/verification_target.dart';
import 'auth_providers.dart';
import 'otp_args.dart';
import 'widgets/auth_field.dart';
import 'widgets/auth_page.dart';

class ForgotPasswordPage extends ConsumerStatefulWidget {
  const ForgotPasswordPage({super.key, this.setupIdentifier});

  final String? setupIdentifier;

  @override
  ConsumerState<ForgotPasswordPage> createState() => _ForgotPasswordPageState();
}

class _ForgotPasswordPageState extends ConsumerState<ForgotPasswordPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  late final _identifier = TextEditingController(text: widget.setupIdentifier);

  bool get _setup => widget.setupIdentifier != null;

  @override
  void dispose() {
    _identifier.dispose();
    super.dispose();
  }

  Future<void> _send() async {
    if (!_formKey.currentState!.validate()) return;
    final value = _identifier.text.trim();
    final target = VerificationTarget(
      channel: ContactChannel.of(value),
      value: value,
    );
    final OtpDelivery? delivery = target.channel == ContactChannel.phone
        ? await showOtpChannelSheet(context)
        : null;
    if (target.channel == ContactChannel.phone && delivery == null) return;
    await submit(() async {
      await ref
          .read(authRepositoryProvider)
          .forgotPassword(value, delivery: delivery);
      if (mounted) {
        context.push(
          Routes.otp,
          extra: OtpArgs.passwordReset(target, delivery: delivery),
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return AuthPage(
      title: _setup ? PasswordSetupText.title : AuthText.forgotTitle,
      subtitle: _setup ? PasswordSetupText.body : AuthText.forgotBody,
      showBack: true,
      children: [
        Form(
          key: _formKey,
          child: AuthField(
            controller: _identifier,
            hint: AuthText.forgotIdentifierLabel,
            icon: AppIcons.envelope,
            validator: Validators.required,
            keyboardType: TextInputType.emailAddress,
            textInputAction: TextInputAction.done,
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        PrimaryButton(
          label: AuthText.sendCode,
          loading: loading,
          onPressed: _send,
        ),
        const SizedBox(height: AppSpacing.xl),
        Center(
          child: TextButton(
            onPressed: () => context.go(Routes.login),
            child: Text(AuthText.backToLogin),
          ),
        ),
      ],
    );
  }
}
