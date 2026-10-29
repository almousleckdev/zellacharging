import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/primary_button.dart';
import 'profile_providers.dart';

class ConfirmEmailPage extends ConsumerStatefulWidget {
  const ConfirmEmailPage({super.key, required this.email});

  final String email;

  @override
  ConsumerState<ConfirmEmailPage> createState() => _ConfirmEmailPageState();
}

class _ConfirmEmailPageState extends ConsumerState<ConfirmEmailPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _code = TextEditingController();

  @override
  void dispose() {
    _code.dispose();
    super.dispose();
  }

  void _confirm() {
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      await ref.read(profileRepositoryProvider).confirmEmail(_code.text.trim());
      ref.invalidate(profileProvider);
      if (!mounted) return;
      AppToast.info(context, ProfileText.emailAdded);
      context.pop();
    });
  }

  void _resend() {
    submit(() async {
      await ref.read(profileRepositoryProvider).addEmail(widget.email);
      if (mounted) AppToast.info(context, AuthText.codeResent);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.surfaceMuted,
      appBar: AppBar(title: Text(ProfileText.confirmEmailTitle)),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            Text(
              ProfileText.confirmEmailBody(widget.email),
              style: Theme.of(
                context,
              ).textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
            ),
            const SizedBox(height: AppSpacing.lg),
            Form(
              key: _formKey,
              child: AppTextField(
                controller: _code,
                label: AuthText.codeLabel,
                icon: AppIcons.key,
                validator: Validators.otp,
                keyboardType: TextInputType.number,
                textInputAction: TextInputAction.done,
                maxLength: 6,
                autofillHints: const [AutofillHints.oneTimeCode],
              ),
            ),
            const SizedBox(height: AppSpacing.xl),
            PrimaryButton(
              label: AuthText.verify,
              loading: loading,
              onPressed: _confirm,
            ),
            const SizedBox(height: AppSpacing.sm),
            Center(
              child: TextButton(
                onPressed: loading ? null : _resend,
                child: Text(AuthText.resendCode),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
