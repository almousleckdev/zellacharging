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
import '../domain/contact_confirmation.dart';
import '../domain/contact_kind.dart';
import '../domain/otp_delivery.dart';
import 'profile_providers.dart';

class ConfirmContactPage extends ConsumerStatefulWidget {
  const ConfirmContactPage({
    super.key,
    required this.kind,
    required this.confirmation,
  });

  final ContactKind kind;
  final ContactConfirmation confirmation;

  @override
  ConsumerState<ConfirmContactPage> createState() => _ConfirmContactPageState();
}

class _ConfirmContactPageState extends ConsumerState<ConfirmContactPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _code = TextEditingController();

  bool get _isEmail => widget.kind == ContactKind.email;

  @override
  void dispose() {
    _code.dispose();
    super.dispose();
  }

  void _confirm() {
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      final repository = ref.read(profileRepositoryProvider);
      final code = _code.text.trim();
      await (_isEmail
          ? repository.confirmEmail(code)
          : repository.confirmPhone(code));
      ref.invalidate(profileProvider);
      if (!mounted) return;
      AppToast.success(
        context,
        _isEmail ? ProfileText.emailAdded : ProfileText.phoneAdded,
      );
      context.pop();
    });
  }

  void _resend() {
    submit(() async {
      final repository = ref.read(profileRepositoryProvider);
      await (_isEmail
          ? repository.addEmail(widget.confirmation.target)
          : repository.addPhone(
              widget.confirmation.target,
              widget.confirmation.delivery ?? OtpDelivery.whatsapp,
            ));
      if (mounted) AppToast.success(context, AuthText.codeResent);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.surfaceMuted,
      appBar: AppBar(
        title: Text(
          _isEmail
              ? ProfileText.confirmEmailTitle
              : ProfileText.confirmPhoneTitle,
        ),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            Text(
              _isEmail
                  ? ProfileText.confirmEmailBody(widget.confirmation.target)
                  : ProfileText.confirmPhoneBody(
                      widget.confirmation.target,
                      bySms: widget.confirmation.delivery == OtpDelivery.sms,
                    ),
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
