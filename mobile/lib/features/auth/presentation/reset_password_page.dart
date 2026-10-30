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
import 'auth_providers.dart';
import 'reset_args.dart';
import 'widgets/auth_field.dart';
import 'widgets/auth_page.dart';

class ResetPasswordPage extends ConsumerStatefulWidget {
  const ResetPasswordPage({super.key, required this.args});

  final ResetArgs args;

  @override
  ConsumerState<ResetPasswordPage> createState() => _ResetPasswordPageState();
}

class _ResetPasswordPageState extends ConsumerState<ResetPasswordPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _password = TextEditingController();
  final _confirm = TextEditingController();

  @override
  void dispose() {
    _password.dispose();
    _confirm.dispose();
    super.dispose();
  }

  void _reset() {
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      await ref
          .read(authRepositoryProvider)
          .resetPassword(
            email: widget.args.email,
            code: widget.args.code,
            newPassword: _password.text,
          );
      if (mounted) context.go(Routes.resetSuccess);
    });
  }

  @override
  Widget build(BuildContext context) {
    return AuthPage(
      title: AuthText.newPasswordTitle,
      subtitle: AuthText.newPasswordBody,
      showBack: true,
      children: [
        Form(
          key: _formKey,
          child: Column(
            children: [
              AuthField(
                controller: _password,
                hint: AuthText.newPassword,
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
        const SizedBox(height: AppSpacing.lg),
        PrimaryButton(
          height: authButtonHeight,
          label: AuthText.resetPassword,
          loading: loading,
          onPressed: _reset,
        ),
      ],
    );
  }
}
