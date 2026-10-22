import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/primary_button.dart';
import 'profile_providers.dart';

class ChangePasswordPage extends ConsumerStatefulWidget {
  const ChangePasswordPage({super.key});

  @override
  ConsumerState<ChangePasswordPage> createState() => _ChangePasswordPageState();
}

class _ChangePasswordPageState extends ConsumerState<ChangePasswordPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _current = TextEditingController();
  final _next = TextEditingController();

  @override
  void dispose() {
    _current.dispose();
    _next.dispose();
    super.dispose();
  }

  void _save() {
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      await ref
          .read(profileRepositoryProvider)
          .changePassword(_current.text, _next.text);
      if (!mounted) return;
      AppToast.info(context, ProfileText.passwordChanged);
      context.pop();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(ProfileText.changePassword)),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            Form(
              key: _formKey,
              child: Column(
                children: [
                  AppTextField(
                    controller: _current,
                    label: ProfileText.currentPassword,
                    icon: AppIcons.lock,
                    validator: Validators.required,
                    obscure: true,
                  ),
                  const SizedBox(height: AppSpacing.md),
                  AppTextField(
                    controller: _next,
                    label: ProfileText.newPassword,
                    icon: AppIcons.key,
                    validator: Validators.password,
                    obscure: true,
                    textInputAction: TextInputAction.done,
                  ),
                ],
              ),
            ),
            const SizedBox(height: AppSpacing.xl),
            PrimaryButton(
              label: ProfileText.saveChanges,
              loading: loading,
              onPressed: _save,
            ),
          ],
        ),
      ),
    );
  }
}
