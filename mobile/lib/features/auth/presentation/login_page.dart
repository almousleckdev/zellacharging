import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/session/session_controller.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/form_page.dart';
import '../../../shared/widgets/primary_button.dart';
import 'auth_providers.dart';
import 'auth_switch_link.dart';
import 'social_buttons.dart';
import '../../../core/theme/app_icons.dart';

class LoginPage extends ConsumerStatefulWidget {
  const LoginPage({super.key});

  @override
  ConsumerState<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends ConsumerState<LoginPage> with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _identifier = TextEditingController();
  final _password = TextEditingController();

  @override
  void dispose() {
    _identifier.dispose();
    _password.dispose();
    super.dispose();
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      await ref
          .read(authRepositoryProvider)
          .login(identifier: _identifier.text.trim(), password: _password.text);
      ref.read(sessionControllerProvider.notifier).signedIn();
    });
  }

  @override
  Widget build(BuildContext context) {
    return FormPage(
      title: AuthText.signInTitle,
      subtitle: AuthText.signInSubtitle,
      children: [
        Form(
          key: _formKey,
          child: Column(
            children: [
              AppTextField(
                controller: _identifier,
                label: AuthText.identifierLabel,
                icon: AppIcons.envelope,
                validator: Validators.required,
                keyboardType: TextInputType.emailAddress,
              ),
              const SizedBox(height: AppSpacing.md),
              AppTextField(
                controller: _password,
                label: AuthText.passwordLabel,
                icon: AppIcons.lock,
                validator: Validators.required,
                obscure: true,
                textInputAction: TextInputAction.done,
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
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
