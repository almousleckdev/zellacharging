import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/profile/domain/profile.dart';
import 'package:zella/features/profile/presentation/complete_profile_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets('a social account is asked to add its phone first', (
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
      const CompleteProfilePage(),
      overrides: overridesWith(profile: profile),
    );
    await settle(tester);

    expect(find.text(ContactGateText.title), findsOneWidget);
    expect(find.text(ContactGateText.addPhone), findsOneWidget);
    expect(find.text(ContactGateText.missing), findsOneWidget);
    expect(find.text(ContactGateText.verified), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('an unverified email is verified without retyping it', (
    tester,
  ) async {
    final profile = FakeProfile()
      ..served = const Profile(
        firstName: 'Aline',
        lastName: 'Obame',
        email: 'aline@example.com',
        phone: '+24177123456',
        phoneVerified: true,
      );
    await pumpApp(
      tester,
      const CompleteProfilePage(),
      overrides: overridesWith(profile: profile),
    );
    await settle(tester);

    expect(find.text(ContactGateText.verifyEmail), findsOneWidget);
    await tester.tap(find.text(ContactGateText.verifyEmail));
    await settle(tester);

    expect(profile.emailCodesSent, 1);
    await tester.pump(const Duration(seconds: 5));
    await closeApp(tester);
  });
}
