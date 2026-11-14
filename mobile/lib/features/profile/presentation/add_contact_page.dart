import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/format/phone_number.dart';
import '../../../core/validation/validators.dart';
import '../../auth/presentation/widgets/auth_field.dart';
import '../../auth/presentation/widgets/auth_page.dart';
import '../../auth/presentation/widgets/phone_field.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/primary_button.dart';
import '../domain/contact_confirmation.dart';
import '../domain/contact_kind.dart';
import '../../../shared/otp_delivery.dart';
import '../../../shared/widgets/otp_channel_sheet.dart';
import 'profile_providers.dart';

class AddContactPage extends ConsumerStatefulWidget {
  const AddContactPage({super.key, required this.kind, this.changing = false});

  final ContactKind kind;
  final bool changing;

  @override
  ConsumerState<AddContactPage> createState() => _AddContactPageState();
}

class _AddContactPageState extends ConsumerState<AddContactPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _value = TextEditingController();

  bool get _isEmail => widget.kind == ContactKind.email;

  String get _title => switch ((_isEmail, widget.changing)) {
    (true, true) => ProfileText.changeEmail,
    (true, false) => ProfileText.addEmail,
    (false, true) => ProfileText.changePhone,
    (false, false) => ProfileText.addPhone,
  };

  String get _hint => switch ((_isEmail, widget.changing)) {
    (true, true) => ProfileText.changeEmailHint,
    (true, false) => ProfileText.addEmailHint,
    (false, true) => ProfileText.changePhoneHint,
    (false, false) => ProfileText.addPhoneHint,
  };

  @override
  void dispose() {
    _value.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    final value = _isEmail
        ? _value.text.trim()
        : PhoneNumber.e164(PhoneNumber.defaultCountry, _value.text);
    final OtpDelivery? delivery = _isEmail
        ? null
        : await showOtpChannelSheet(context);
    if (!_isEmail && delivery == null) return;
    await submit(() async {
      final repository = ref.read(profileRepositoryProvider);
      if (_isEmail) {
        await repository.addEmail(value);
      } else {
        await repository.addPhone(value, delivery!);
      }
      if (!mounted) return;
      context.pushReplacement(
        _isEmail ? Routes.confirmEmail : Routes.confirmPhone,
        extra: ContactConfirmation(
          target: value,
          delivery: delivery,
          changing: widget.changing,
        ),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return AuthPage(
      title: _title,
      subtitle: _hint,
      children: [
        Form(
          key: _formKey,
          child: _isEmail
              ? AuthField(
                  controller: _value,
                  hint: AuthText.emailLabel,
                  icon: AppIcons.envelope,
                  validator: Validators.email,
                  keyboardType: TextInputType.emailAddress,
                  textInputAction: TextInputAction.done,
                  autofillHints: const [AutofillHints.email],
                )
              : PhoneField(
                  controller: _value,
                  textInputAction: TextInputAction.done,
                ),
        ),
        const SizedBox(height: AppSpacing.xl),
        PrimaryButton(
          label: ProfileText.sendCode,
          loading: loading,
          onPressed: _save,
        ),
      ],
    );
  }
}
