import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/format/phone_number.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/otp_delivery.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/otp_channel_sheet.dart';
import '../../../shared/widgets/primary_button.dart';
import '../../support/presentation/open_agreement.dart';
import '../domain/contact_channel.dart';
import '../domain/registration_data.dart';
import 'auth_providers.dart';
import 'auth_switch_link.dart';
import 'otp_args.dart';
import 'terms_checkbox.dart';
import 'widgets/auth_field.dart';
import 'widgets/auth_page.dart';
import 'widgets/contact_mode_switch.dart';
import 'widgets/phone_field.dart';

class RegisterPage extends ConsumerStatefulWidget {
  const RegisterPage({super.key});

  @override
  ConsumerState<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends ConsumerState<RegisterPage> with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _firstName = TextEditingController();
  final _lastName = TextEditingController();
  final _email = TextEditingController();
  final _phone = TextEditingController();
  final _password = TextEditingController();
  final _confirm = TextEditingController();
  var _channel = ContactChannel.email;
  var _accepted = false;
  var _submitted = false;

  @override
  void dispose() {
    for (final controller in [
      _firstName,
      _lastName,
      _email,
      _phone,
      _password,
      _confirm,
    ]) {
      controller.dispose();
    }
    super.dispose();
  }

  bool get _byPhone => _channel == ContactChannel.phone;

  Future<void> _submit() async {
    setState(() => _submitted = true);
    if (!_formKey.currentState!.validate() || !_accepted) return;
    final OtpDelivery? delivery = _byPhone
        ? await showOtpChannelSheet(context)
        : null;
    if (_byPhone && delivery == null) return;
    await submit(() async {
      final target = await ref
          .read(authRepositoryProvider)
          .register(
            RegistrationData(
              firstName: _firstName.text.trim(),
              lastName: _lastName.text.trim(),
              password: _password.text,
              email: _byPhone ? null : _email.text.trim(),
              phone: _byPhone
                  ? PhoneNumber.e164(PhoneNumber.defaultCountry, _phone.text)
                  : null,
              delivery: delivery,
            ),
          );
      if (mounted) {
        context.push(
          Routes.otp,
          extra: OtpArgs.registration(target, delivery: delivery),
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return AuthPage(
      title: AuthText.registerTitle,
      subtitle: AuthText.registerSubtitleNew,
      children: [
        Form(
          key: _formKey,
          child: Column(
            children: [
              AuthField(
                controller: _firstName,
                hint: AuthText.firstNameLabel,
                icon: AppIcons.user,
                validator: Validators.required,
                autofillHints: const [AutofillHints.givenName],
              ),
              const SizedBox(height: AppSpacing.md),
              AuthField(
                controller: _lastName,
                hint: AuthText.lastNameLabel,
                icon: AppIcons.user,
                validator: Validators.required,
                autofillHints: const [AutofillHints.familyName],
              ),
              const SizedBox(height: AppSpacing.md),
              ContactModeSwitch(
                selected: _channel,
                onChanged: (channel) => setState(() => _channel = channel),
              ),
              const SizedBox(height: AppSpacing.md),
              if (_byPhone)
                PhoneField(controller: _phone)
              else
                AuthField(
                  controller: _email,
                  hint: AuthText.emailLabel,
                  icon: AppIcons.envelope,
                  validator: Validators.email,
                  keyboardType: TextInputType.emailAddress,
                  autofillHints: const [AutofillHints.email],
                ),
              const SizedBox(height: AppSpacing.md),
              AuthField(
                controller: _password,
                hint: AuthText.passwordLabel,
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
        const SizedBox(height: AppSpacing.md),
        TermsCheckbox(
          accepted: _accepted,
          showError: _submitted,
          onChanged: (value) => setState(() => _accepted = value),
          onOpenTerms: () => openAgreement(context, ref),
        ),
        const SizedBox(height: AppSpacing.lg),
        PrimaryButton(
          label: AuthText.createAccount,
          loading: loading,
          onPressed: _submit,
        ),
        const SizedBox(height: AppSpacing.md),
        AuthSwitchLink(
          prompt: AuthText.haveAccount,
          action: AuthText.signIn,
          onTap: () =>
              context.canPop() ? context.pop() : context.go(Routes.login),
        ),
      ],
    );
  }
}
