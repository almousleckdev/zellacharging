import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/session/session_controller.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/adaptive_dialog.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/primary_button.dart';
import 'profile_providers.dart';

class DeleteAccountPage extends ConsumerStatefulWidget {
  const DeleteAccountPage({super.key});

  @override
  ConsumerState<DeleteAccountPage> createState() => _DeleteAccountPageState();
}

class _DeleteAccountPageState extends ConsumerState<DeleteAccountPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _password = TextEditingController();

  @override
  void dispose() {
    _password.dispose();
    super.dispose();
  }

  Future<void> _delete() async {
    if (!_formKey.currentState!.validate()) return;
    final confirmed = await showConfirmDialog(
      context,
      title: ProfileText.deleteAccountTitle,
      message: ProfileText.deleteAccountBody,
      confirmLabel: ProfileText.deleteAccountConfirm,
      destructive: true,
    );
    if (!confirmed) return;
    await submit(() async {
      await ref.read(profileRepositoryProvider).deleteAccount(_password.text);
      await ref.read(sessionControllerProvider.notifier).signOut();
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(ProfileText.deleteAccount)),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            Container(
              padding: const EdgeInsets.all(AppSpacing.md),
              decoration: BoxDecoration(
                color: AppColors.danger.withValues(alpha: 0.08),
                borderRadius: BorderRadius.circular(AppSpacing.radius),
              ),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Icon(AppIcons.warning, color: AppColors.danger),
                  const SizedBox(width: AppSpacing.sm),
                  Expanded(
                    child: Text(
                      ProfileText.deleteAccountBody,
                      style: theme.textTheme.bodyMedium,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: AppSpacing.lg),
            Form(
              key: _formKey,
              child: AppTextField(
                controller: _password,
                label: AuthText.passwordLabel,
                icon: AppIcons.lock,
                validator: Validators.required,
                obscure: true,
                textInputAction: TextInputAction.done,
              ),
            ),
            const SizedBox(height: AppSpacing.xl),
            PrimaryButton(
              label: ProfileText.deleteAccountConfirm,
              icon: AppIcons.trash,
              destructive: true,
              loading: loading,
              onPressed: _delete,
            ),
          ],
        ),
      ),
    );
  }
}
