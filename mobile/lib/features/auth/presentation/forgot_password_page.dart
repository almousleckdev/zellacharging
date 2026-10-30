import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/primary_button.dart';
import 'auth_providers.dart';
import 'otp_args.dart';
import 'widgets/auth_field.dart';
import 'widgets/auth_page.dart';

class ForgotPasswordPage extends ConsumerStatefulWidget {
  const ForgotPasswordPage({super.key});

  @override
  ConsumerState<ForgotPasswordPage> createState() => _ForgotPasswordPageState();
}

class _ForgotPasswordPageState extends ConsumerState<ForgotPasswordPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _identifier = TextEditingController();

  @override
  void dispose() {
    _identifier.dispose();
    super.dispose();
  }

  void _send() {
    if (!_formKey.currentState!.validate()) return;
    final value = _identifier.text.trim();
    if (!value.contains('@')) {
      AppToast.info(context, AuthText.phoneResetSoon);
      return;
    }
    submit(() async {
      await ref.read(authRepositoryProvider).forgotPassword(value);
      if (mounted) {
        context.push(Routes.otp, extra: OtpArgs.passwordReset(value));
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return AuthPage(
      title: AuthText.forgotTitle,
      subtitle: AuthText.forgotBody,
      showBack: true,
      children: [
        Form(
          key: _formKey,
          child: AuthField(
            controller: _identifier,
            hint: AuthText.identifierLabel,
            icon: AppIcons.envelope,
            validator: Validators.required,
            keyboardType: TextInputType.emailAddress,
            textInputAction: TextInputAction.done,
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        PrimaryButton(
          height: authButtonHeight,
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
