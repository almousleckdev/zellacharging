import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/primary_button.dart';
import '../../support/presentation/open_agreement.dart';
import '../domain/registration_data.dart';
import 'auth_providers.dart';
import 'auth_switch_link.dart';
import 'otp_args.dart';
import 'terms_checkbox.dart';
import 'widgets/auth_field.dart';
import 'widgets/auth_page.dart';

class RegisterPage extends ConsumerStatefulWidget {
  const RegisterPage({super.key});

  @override
  ConsumerState<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends ConsumerState<RegisterPage> with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _firstName = TextEditingController();
  final _lastName = TextEditingController();
  final _email = TextEditingController();
  final _password = TextEditingController();
  final _confirm = TextEditingController();
  var _accepted = false;
  var _submitted = false;

  @override
  void dispose() {
    for (final controller in [
      _firstName,
      _lastName,
      _email,
      _password,
      _confirm,
    ]) {
      controller.dispose();
    }
    super.dispose();
  }

  void _submit() {
    setState(() => _submitted = true);
    if (!_formKey.currentState!.validate() || !_accepted) return;
    submit(() async {
      final target = await ref
          .read(authRepositoryProvider)
          .register(
            RegistrationData(
              firstName: _firstName.text.trim(),
              lastName: _lastName.text.trim(),
              password: _password.text,
              email: _email.text.trim(),
            ),
          );
      if (mounted) {
        context.go(Routes.otp, extra: OtpArgs.registration(target.value));
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return AuthPage(
      title: AuthText.registerTitle,
      subtitle: AuthText.registerSubtitleNew,
      children: [
        Form(
          key: _formKey,
          child: Column(
            children: [
              AuthField(
                controller: _firstName,
                hint: AuthText.firstNameLabel,
                icon: AppIcons.user,
                validator: Validators.required,
                autofillHints: const [AutofillHints.givenName],
              ),
              const SizedBox(height: AppSpacing.md),
              AuthField(
                controller: _lastName,
                hint: AuthText.lastNameLabel,
                icon: AppIcons.user,
                validator: Validators.required,
                autofillHints: const [AutofillHints.familyName],
              ),
              const SizedBox(height: AppSpacing.md),
              AuthField(
                controller: _email,
                hint: AuthText.identifierLabel,
                icon: AppIcons.envelope,
                validator: Validators.emailForSignUp,
                keyboardType: TextInputType.emailAddress,
                autofillHints: const [AutofillHints.email],
              ),
              const SizedBox(height: AppSpacing.md),
              AuthField(
                controller: _password,
                hint: AuthText.passwordLabel,
                icon: AppIcons.lock,
                validator: Validators.newPassword,
                obscure: true,
              ),
              const SizedBox(height: AppSpacing.md),
              AuthField(
                controller: _confirm,
                hint: AuthText.confirmPassword,
                icon: AppIcons.lock,
                validator: Validators.sameAs(_password),
                obscure: true,
                textInputAction: TextInputAction.done,
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        TermsCheckbox(
          accepted: _accepted,
          showError: _submitted,
          onChanged: (value) => setState(() => _accepted = value),
          onOpenTerms: () => openAgreement(context, ref),
        ),
        const SizedBox(height: AppSpacing.lg),
        PrimaryButton(
          label: AuthText.createAccount,
          loading: loading,
          onPressed: _submit,
        ),
        const SizedBox(height: AppSpacing.md),
        AuthSwitchLink(
          prompt: AuthText.haveAccount,
          action: AuthText.signIn,
          onTap: () => context.go(Routes.login),
        ),
      ],
    );
  }
}
