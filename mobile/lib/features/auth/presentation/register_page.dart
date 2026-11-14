import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/format/phone_number.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/otp_delivery.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/otp_channel_sheet.dart';
import '../../../shared/widgets/primary_button.dart';
import '../../support/presentation/open_agreement.dart';
import '../domain/contact_channel.dart';
import '../domain/registration_data.dart';
import '../domain/verification_target.dart';
import 'auth_providers.dart';
import 'auth_switch_link.dart';
import 'otp_args.dart';
import 'register_fields.dart';
import 'terms_checkbox.dart';
import 'widgets/auth_page.dart';

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

  Future<void> _submit() async {
    setState(() => _submitted = true);
    if (!_formKey.currentState!.validate() || !_accepted) return;
    final OtpDelivery? delivery = await showOtpChannelSheet(context);
    if (delivery == null) return;
    final email = _email.text.trim();
    await submit(() async {
      final phone = await ref
          .read(authRepositoryProvider)
          .register(
            RegistrationData(
              firstName: _firstName.text.trim(),
              lastName: _lastName.text.trim(),
              password: _password.text,
              email: email,
              phone: PhoneNumber.e164(PhoneNumber.defaultCountry, _phone.text),
              delivery: delivery,
            ),
          );
      if (!mounted) return;
      context.push(
        Routes.otp,
        extra: OtpArgs.registration(
          phone,
          delivery: delivery,
          followUp: VerificationTarget(
            channel: ContactChannel.email,
            value: email,
          ),
        ),
      );
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
          child: RegisterFields(
            firstName: _firstName,
            lastName: _lastName,
            email: _email,
            phone: _phone,
            password: _password,
            confirm: _confirm,
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
