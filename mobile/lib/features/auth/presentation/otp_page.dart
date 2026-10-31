import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/primary_button.dart';
import '../domain/contact_channel.dart';
import '../domain/verification_target.dart';
import 'auth_providers.dart';
import 'otp_args.dart';
import 'reset_args.dart';
import 'widgets/auth_page.dart';
import 'widgets/otp_boxes.dart';
import 'widgets/resend_countdown.dart';

class OtpPage extends ConsumerStatefulWidget {
  const OtpPage({super.key, required this.args});

  final OtpArgs args;

  @override
  ConsumerState<OtpPage> createState() => _OtpPageState();
}

class _OtpPageState extends ConsumerState<OtpPage> with SubmitMixin {
  final _code = TextEditingController();

  bool get _isReset => widget.args.purpose == OtpPurpose.passwordReset;

  VerificationTarget get _target => VerificationTarget(
    channel: ContactChannel.email,
    value: widget.args.email,
  );

  @override
  void dispose() {
    _code.dispose();
    super.dispose();
  }

  Future<void> _verify() async {
    final invalid = Validators.otp(_code.text);
    if (invalid != null) {
      showError(invalid);
      return;
    }
    if (_isReset) {
      context.push(
        Routes.resetPassword,
        extra: ResetArgs(email: widget.args.email, code: _code.text.trim()),
      );
      return;
    }
    await submit(() async {
      await ref.read(authRepositoryProvider).verify(_target, _code.text.trim());
      if (!mounted) return;
      AppToast.success(context, AuthText.accountVerified);
      context.go(Routes.login);
    });
  }

  Future<void> _resend() async {
    await submit(() async {
      final repository = ref.read(authRepositoryProvider);
      _isReset
          ? await repository.forgotPassword(widget.args.email)
          : await repository.resendCode(_target);
      if (mounted) AppToast.success(context, AuthText.codeResent);
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return AuthPage(
      title: AuthText.otpTitle,
      showBack: true,
      subtitle: AuthText.otpSentTo,
      children: [
        Text(
          widget.args.email,
          style: theme.textTheme.bodyLarge?.copyWith(
            fontWeight: FontWeight.w500,
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        OtpBoxes(controller: _code),
        const SizedBox(height: AppSpacing.md),
        ResendCountdown(onResend: _resend),
        const SizedBox(height: AppSpacing.xl),
        PrimaryButton(
          label: AuthText.verify,
          loading: loading,
          onPressed: _verify,
        ),
        const SizedBox(height: AppSpacing.sm),
        Center(
          child: TextButton(
            onPressed: () => context.pop(),
            child: Text(
              AuthText.changeEmail,
              style: const TextStyle(
                color: AppColors.primary,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ),
      ],
    );
  }
}
