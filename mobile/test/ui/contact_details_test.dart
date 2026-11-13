import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/profile/domain/contact_confirmation.dart';
import 'package:zella/features/profile/domain/contact_kind.dart';
import 'package:zella/features/profile/domain/otp_delivery.dart';
import 'package:zella/features/profile/domain/profile.dart';
import 'package:zella/features/profile/presentation/add_contact_page.dart';
import 'package:zella/features/profile/presentation/confirm_contact_page.dart';
import 'package:zella/features/profile/presentation/edit_profile_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets('a customer without an email is offered to add one', (
    tester,
  ) async {
    final profile = FakeProfile()
      ..served = const Profile(
        firstName: 'Aline',
        lastName: 'Obame',
        phone: '+24162820745',
      );
    await pumpApp(
      tester,
      const EditProfilePage(),
      overrides: overridesWith(profile: profile),
    );

    expect(find.text(ProfileText.addEmail), findsOneWidget);
    expect(find.text(ProfileText.addPhone), findsNothing);
    expect(find.text('+24162820745'), findsOneWidget);
    expect(find.text(ProfileText.verifyAction), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets(
    'verifying a number asks how to receive the code, then sends it',
    (tester) async {
      final profile = FakeProfile()
        ..served = const Profile(
          firstName: 'Aline',
          lastName: 'Obame',
          phone: '+24162820745',
        );
      await pumpApp(
        tester,
        const EditProfilePage(),
        overrides: overridesWith(profile: profile),
      );

      await tester.tap(find.text(ProfileText.verifyAction));
      await settle(tester);

      expect(find.text(ProfileText.otpSheetTitle), findsOneWidget);
      expect(find.text(ProfileText.otpViaWhatsapp), findsOneWidget);
      expect(find.text(ProfileText.recommended), findsOneWidget);
      expect(find.text(ProfileText.otpViaSms), findsOneWidget);

      await tester.tap(find.text(ProfileText.otpViaSms));
      await settle(tester);

      expect(profile.deliveries, [OtpDelivery.sms]);
      await tester.pump(const Duration(seconds: 5));
      await closeApp(tester);
    },
  );

  testWidgets('a customer without a phone number is offered to add one', (
    tester,
  ) async {
    final profile = FakeProfile()
      ..served = const Profile(
        firstName: 'Aline',
        lastName: 'Obame',
        email: 'aline@example.com',
        emailVerified: true,
      );
    await pumpApp(
      tester,
      const EditProfilePage(),
      overrides: overridesWith(profile: profile),
    );

    expect(find.text(ProfileText.addPhone), findsOneWidget);
    expect(find.text(ProfileText.addEmail), findsNothing);
    expect(find.text(ProfileText.verified), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('an invalid email is refused before anything is sent', (
    tester,
  ) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const AddContactPage(kind: ContactKind.email),
      overrides: overridesWith(profile: profile),
    );

    await tester.enterText(find.byType(TextFormField), 'not-an-email');
    await tester.tap(find.text(ProfileText.sendCode));
    await settle(tester);

    expect(profile.emailsAdded, isEmpty);
    expect(find.text(Strings.invalidEmail), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('a valid email is sent to the server to receive a code', (
    tester,
  ) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const AddContactPage(kind: ContactKind.email),
      overrides: overridesWith(profile: profile),
    );

    await tester.enterText(find.byType(TextFormField), 'aline@example.com');
    await tester.tap(find.text(ProfileText.sendCode));
    await settle(tester);

    expect(profile.emailsAdded, ['aline@example.com']);
    await closeApp(tester);
  });

  testWidgets('a phone number asks for a WhatsApp code before it is saved', (
    tester,
  ) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const AddContactPage(kind: ContactKind.phone),
      overrides: overridesWith(profile: profile),
    );

    await tester.enterText(find.byType(TextFormField), '+24162820745');
    await tester.tap(find.text(ProfileText.sendCode));
    await settle(tester);
    await tester.tap(find.text(ProfileText.otpViaWhatsapp));
    await settle(tester);

    expect(profile.phonesAdded, ['+24162820745']);
    expect(profile.deliveries, [OtpDelivery.whatsapp]);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('the code received on WhatsApp confirms the number', (
    tester,
  ) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const ConfirmContactPage(
        kind: ContactKind.phone,
        confirmation: ContactConfirmation(target: '+24162820745'),
      ),
      overrides: overridesWith(profile: profile),
    );

    expect(
      find.text(ProfileText.confirmPhoneBody('+24162820745')),
      findsOneWidget,
    );
    await tester.enterText(find.byType(TextFormField), '654321');
    await tester.tap(find.text(AuthText.verify));
    await settle(tester);

    expect(profile.confirmed, ['654321']);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });

  testWidgets('the code received by email confirms the address', (
    tester,
  ) async {
    final profile = FakeProfile();
    await pumpApp(
      tester,
      const ConfirmContactPage(
        kind: ContactKind.email,
        confirmation: ContactConfirmation(target: 'aline@example.com'),
      ),
      overrides: overridesWith(profile: profile),
    );

    expect(
      find.text(ProfileText.confirmEmailBody('aline@example.com')),
      findsOneWidget,
    );
    await tester.enterText(find.byType(TextFormField), '123456');
    await tester.tap(find.text(AuthText.verify));
    await settle(tester);

    expect(profile.confirmed, ['123456']);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });
}
