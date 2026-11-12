import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/l10n/suspension_text.dart';
import '../../../core/l10n/verification_text.dart';
import '../../../core/router/routes.dart';
import '../../../core/session/session_controller.dart';
import '../../../core/session/suspended_on_sign_out.dart';
import '../../../core/session/stay_signed_in.dart';
import '../../../core/storage/preference_providers.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/primary_button.dart';
import 'auth_providers.dart';
import 'auth_switch_link.dart';
import 'login_options_row.dart';
import 'otp_args.dart';
import 'social_buttons.dart';
import 'widgets/auth_field.dart';
import 'widgets/auth_page.dart';

class LoginPage extends ConsumerStatefulWidget {
  const LoginPage({super.key});

  @override
  ConsumerState<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends ConsumerState<LoginPage> with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _identifier = TextEditingController();
  final _password = TextEditingController();
  var _remember = true;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _announceReplacement());
  }

  void _announceReplacement() {
    if (!mounted) return;
    final suspension = ref.read(suspensionNoticeProvider);
    if (suspension != null) {
      ref.read(suspensionNoticeProvider.notifier).acknowledge();
      AppToast.warning(context, SuspensionText.signedOut(suspension.owedMinor));
      return;
    }
    if (!ref.read(signedOutElsewhereProvider)) return;
    ref.read(signedOutElsewhereProvider.notifier).acknowledge();
    AppToast.warning(context, AuthText.signedOutElsewhere);
  }

  @override
  void dispose() {
    _identifier.dispose();
    _password.dispose();
    super.dispose();
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      final identifier = _identifier.text.trim();
      final unverified = await ref
          .read(signInWithPasswordProvider)
          .call(identifier: identifier, password: _password.text);
      if (unverified != null) {
        if (!mounted) return;
        AppToast.info(context, VerificationText.resumed);
        context.go(Routes.otp, extra: OtpArgs.registration(unverified.value));
        return;
      }
      await ref
          .read(preferenceStoreProvider)
          .save(stayKey, _remember ? '1' : '0');
      if (mounted) context.go(Routes.welcomeBack);
    });
  }

  @override
  Widget build(BuildContext context) {
    return AuthPage(
      title: AuthText.signInTitle,
      subtitle: AuthText.signInSubtitle,
      children: [
        Form(
          key: _formKey,
          child: Column(
            children: [
              AuthField(
                controller: _identifier,
                hint: AuthText.identifierLabel,
                icon: AppIcons.envelope,
                validator: Validators.required,
                keyboardType: TextInputType.emailAddress,
                autofillHints: const [AutofillHints.username],
              ),
              const SizedBox(height: AppSpacing.md),
              AuthField(
                controller: _password,
                hint: AuthText.passwordLabel,
                icon: AppIcons.lock,
                validator: Validators.required,
                obscure: true,
                textInputAction: TextInputAction.done,
                autofillHints: const [AutofillHints.password],
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.sm),
        LoginOptionsRow(
          remember: _remember,
          onRememberChanged: (value) => setState(() => _remember = value),
          onForgot: () => context.push(Routes.forgotPassword),
        ),
        const SizedBox(height: AppSpacing.sm),
        PrimaryButton(
          label: AuthText.signIn,
          loading: loading,
          onPressed: _submit,
        ),
        const SizedBox(height: AppSpacing.lg),
        const SocialButtons(),
        const SizedBox(height: AppSpacing.md),
        AuthSwitchLink(
          prompt: AuthText.noAccount,
          action: AuthText.signUp,
          onTap: () => context.go(Routes.register),
        ),
      ],
    );
  }
}
