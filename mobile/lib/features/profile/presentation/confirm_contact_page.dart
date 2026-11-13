import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/otp_delivery.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/otp_boxes.dart';
import '../../../shared/widgets/primary_button.dart';
import '../../../shared/widgets/resend_countdown.dart';
import '../domain/contact_confirmation.dart';
import '../domain/contact_kind.dart';
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
  final _code = TextEditingController();

  bool get _isEmail => widget.kind == ContactKind.email;
  ContactConfirmation get _confirmation => widget.confirmation;

  @override
  void dispose() {
    _code.dispose();
    super.dispose();
  }

  String get _doneMessage => switch ((_isEmail, _confirmation.changing)) {
    (true, true) => ProfileText.emailChanged,
    (true, false) => ProfileText.emailAdded,
    (false, true) => ProfileText.phoneChanged,
    (false, false) => ProfileText.phoneAdded,
  };

  Future<void> _confirm() async {
    final invalid = Validators.otp(_code.text);
    if (invalid != null) {
      showError(invalid);
      return;
    }
    await submit(() async {
      final repository = ref.read(profileRepositoryProvider);
      final code = _code.text.trim();
      await (_isEmail
          ? repository.confirmEmail(code)
          : repository.confirmPhone(code));
      ref.invalidate(profileProvider);
      if (!mounted) return;
      AppToast.success(context, _doneMessage);
      context.pop();
    });
  }

  Future<void> _resend() async {
    await submit(() async {
      final repository = ref.read(profileRepositoryProvider);
      await (_isEmail
          ? repository.addEmail(_confirmation.target)
          : repository.addPhone(
              _confirmation.target,
              _confirmation.delivery ?? OtpDelivery.whatsapp,
            ));
      if (mounted) AppToast.success(context, AuthText.codeResent);
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
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
                  ? ProfileText.confirmEmailBody(_confirmation.target)
                  : ProfileText.confirmPhoneBody(
                      _confirmation.target,
                      bySms: _confirmation.delivery == OtpDelivery.sms,
                    ),
              style: theme.textTheme.bodyMedium?.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.lg),
            OtpBoxes(controller: _code),
            const SizedBox(height: AppSpacing.md),
            ResendCountdown(onResend: _resend),
            const SizedBox(height: AppSpacing.xl),
            PrimaryButton(
              label: AuthText.verify,
              loading: loading,
              onPressed: _confirm,
            ),
          ],
        ),
      ),
    );
  }
}
