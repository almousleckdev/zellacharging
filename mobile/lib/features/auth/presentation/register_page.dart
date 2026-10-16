import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/form_page.dart';
import '../../../shared/widgets/primary_button.dart';
import '../domain/registration_data.dart';
import 'auth_providers.dart';
import 'auth_switch_link.dart';

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
  final _phone = TextEditingController();
  final _password = TextEditingController();

  @override
  void dispose() {
    for (final controller in [
      _firstName,
      _lastName,
      _email,
      _phone,
      _password,
    ]) {
      controller.dispose();
    }
    super.dispose();
  }

  String? _nullIfBlank(String value) =>
      value.trim().isEmpty ? null : value.trim();

  void _submit() {
    if (!_formKey.currentState!.validate()) return;
    final email = _nullIfBlank(_email.text);
    final phone = _nullIfBlank(_phone.text);
    if (email == null && phone == null) {
      setState(() => errorMessage = Strings.contactRequired);
      return;
    }
    submit(() async {
      final target = await ref
          .read(authRepositoryProvider)
          .register(
            RegistrationData(
              firstName: _firstName.text.trim(),
              lastName: _lastName.text.trim(),
              password: _password.text,
              email: email,
              phone: phone,
            ),
          );
      if (mounted) context.go(Routes.verify, extra: target);
    });
  }

  @override
  Widget build(BuildContext context) {
    return FormPage(
      title: Strings.createAccount,
      subtitle: Strings.registerSubtitle,
      errorMessage: errorMessage,
      children: [
        Form(
          key: _formKey,
          child: Column(
            children: [
              AppTextField(
                controller: _firstName,
                label: Strings.firstNameLabel,
                icon: Icons.person_outline,
                validator: Validators.required,
              ),
              const SizedBox(height: AppSpacing.md),
              AppTextField(
                controller: _lastName,
                label: Strings.lastNameLabel,
                icon: Icons.person_outline,
                validator: Validators.required,
              ),
              const SizedBox(height: AppSpacing.md),
              AppTextField(
                controller: _email,
                label: Strings.emailLabel,
                icon: Icons.mail_outline,
                validator: Validators.optionalEmail,
                keyboardType: TextInputType.emailAddress,
              ),
              const SizedBox(height: AppSpacing.md),
              AppTextField(
                controller: _phone,
                label: Strings.phoneLabel,
                icon: Icons.phone_outlined,
                keyboardType: TextInputType.phone,
              ),
              const SizedBox(height: AppSpacing.md),
              AppTextField(
                controller: _password,
                label: Strings.passwordLabel,
                icon: Icons.lock_outline,
                validator: Validators.password,
                obscure: true,
                textInputAction: TextInputAction.done,
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        PrimaryButton(
          label: Strings.createAccount,
          loading: loading,
          onPressed: _submit,
        ),
        const SizedBox(height: AppSpacing.md),
        AuthSwitchLink(
          prompt: Strings.haveAccount,
          action: Strings.signIn,
          onTap: () => context.go(Routes.login),
        ),
      ],
    );
  }
}
