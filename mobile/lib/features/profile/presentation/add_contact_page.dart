import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/primary_button.dart';
import '../domain/contact_kind.dart';
import 'profile_providers.dart';

class AddContactPage extends ConsumerStatefulWidget {
  const AddContactPage({super.key, required this.kind});

  final ContactKind kind;

  @override
  ConsumerState<AddContactPage> createState() => _AddContactPageState();
}

class _AddContactPageState extends ConsumerState<AddContactPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _value = TextEditingController();

  bool get _isEmail => widget.kind == ContactKind.email;

  @override
  void dispose() {
    _value.dispose();
    super.dispose();
  }

  void _save() {
    if (!_formKey.currentState!.validate()) return;
    final value = _value.text.trim();
    submit(() async {
      final repository = ref.read(profileRepositoryProvider);
      if (_isEmail) {
        await repository.addEmail(value);
        if (mounted) context.pushReplacement(Routes.confirmEmail, extra: value);
        return;
      }
      await repository.addPhone(value);
      ref.invalidate(profileProvider);
      if (!mounted) return;
      AppToast.success(context, ProfileText.phoneAdded);
      context.pop();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.surfaceMuted,
      appBar: AppBar(
        title: Text(_isEmail ? ProfileText.addEmail : ProfileText.addPhone),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            Text(
              _isEmail ? ProfileText.addEmailHint : ProfileText.addPhoneHint,
              style: Theme.of(
                context,
              ).textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
            ),
            const SizedBox(height: AppSpacing.lg),
            Form(
              key: _formKey,
              child: AppTextField(
                controller: _value,
                label: _isEmail ? AuthText.emailLabel : AuthText.phoneLabel,
                icon: _isEmail ? AppIcons.envelope : AppIcons.phone,
                validator: _isEmail
                    ? Validators.email
                    : Validators.requiredPhone,
                keyboardType: _isEmail
                    ? TextInputType.emailAddress
                    : TextInputType.phone,
                textInputAction: TextInputAction.done,
              ),
            ),
            const SizedBox(height: AppSpacing.xl),
            PrimaryButton(
              label: _isEmail ? ProfileText.sendCode : ProfileText.savePhone,
              loading: loading,
              onPressed: _save,
            ),
          ],
        ),
      ),
    );
  }
}
