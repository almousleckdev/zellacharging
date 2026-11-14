import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import 'widgets/auth_field.dart';
import 'widgets/phone_field.dart';

class RegisterFields extends StatelessWidget {
  const RegisterFields({
    super.key,
    required this.firstName,
    required this.lastName,
    required this.email,
    required this.phone,
    required this.password,
    required this.confirm,
  });

  final TextEditingController firstName;
  final TextEditingController lastName;
  final TextEditingController email;
  final TextEditingController phone;
  final TextEditingController password;
  final TextEditingController confirm;

  @override
  Widget build(BuildContext context) {
    const gap = SizedBox(height: AppSpacing.md);
    return Column(
      children: [
        AuthField(
          controller: firstName,
          hint: AuthText.firstNameLabel,
          icon: AppIcons.user,
          validator: Validators.required,
          autofillHints: const [AutofillHints.givenName],
        ),
        gap,
        AuthField(
          controller: lastName,
          hint: AuthText.lastNameLabel,
          icon: AppIcons.user,
          validator: Validators.required,
          autofillHints: const [AutofillHints.familyName],
        ),
        gap,
        AuthField(
          controller: email,
          hint: AuthText.emailLabel,
          icon: AppIcons.envelope,
          validator: Validators.email,
          keyboardType: TextInputType.emailAddress,
          autofillHints: const [AutofillHints.email],
        ),
        gap,
        PhoneField(controller: phone),
        gap,
        AuthField(
          controller: password,
          hint: AuthText.passwordLabel,
          icon: AppIcons.lock,
          validator: Validators.newPassword,
          obscure: true,
        ),
        gap,
        AuthField(
          controller: confirm,
          hint: AuthText.confirmPassword,
          icon: AppIcons.lock,
          validator: Validators.sameAs(password),
          obscure: true,
          textInputAction: TextInputAction.done,
        ),
      ],
    );
  }
}
