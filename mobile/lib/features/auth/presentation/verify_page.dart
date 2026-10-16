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
import '../domain/verification_target.dart';
import 'auth_providers.dart';

class VerifyPage extends ConsumerStatefulWidget {
  const VerifyPage({super.key, required this.target});

  final VerificationTarget target;

  @override
  ConsumerState<VerifyPage> createState() => _VerifyPageState();
}

class _VerifyPageState extends ConsumerState<VerifyPage> with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _code = TextEditingController();

  @override
  void dispose() {
    _code.dispose();
    super.dispose();
  }

  void _verify() {
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      await ref
          .read(authRepositoryProvider)
          .verify(widget.target, _code.text.trim());
      if (mounted) context.go(Routes.login);
    });
  }

  void _resend() {
    submit(() async {
      await ref.read(authRepositoryProvider).resendCode(widget.target);
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text(Strings.codeResent)));
    });
  }

  @override
  Widget build(BuildContext context) {
    return FormPage(
      title: Strings.verifyTitle,
      subtitle: widget.target.value,
      errorMessage: errorMessage,
      showBack: true,
      children: [
        Form(
          key: _formKey,
          child: AppTextField(
            controller: _code,
            label: Strings.codeLabel,
            icon: Icons.pin_outlined,
            validator: Validators.otp,
            keyboardType: TextInputType.number,
            textInputAction: TextInputAction.done,
            maxLength: 6,
            autofillHints: const [AutofillHints.oneTimeCode],
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        PrimaryButton(
          label: Strings.verify,
          loading: loading,
          onPressed: _verify,
        ),
        const SizedBox(height: AppSpacing.sm),
        Center(
          child: TextButton(
            onPressed: loading ? null : _resend,
            child: const Text(Strings.resendCode),
          ),
        ),
      ],
    );
  }
}
