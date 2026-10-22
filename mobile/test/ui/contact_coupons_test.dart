import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/features/charging/domain/session_state.dart';
import 'package:ndzela/features/coupons/presentation/coupons_page.dart';
import 'package:ndzela/features/support/domain/support_info.dart';
import 'package:ndzela/features/support/presentation/contact_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets('the support contacts open the right apps', (tester) async {
    final links = FakeLinks();
    await pumpApp(
      tester,
      const ContactPage(),
      overrides: overrides(SessionState.active, null, null, links),
    );

    await tester.tap(find.text('support@ndzela.example'));
    await settle(tester);
    await tester.tap(find.text('+24177000000'));
    await settle(tester);
    await tester.tap(find.textContaining(ProfileText.whatsapp));
    await settle(tester);

    expect(links.opened.map((link) => link.toString()), [
      'mailto:support@ndzela.example',
      'tel:+24177000000',
      'https://wa.me/24177000001',
    ]);
    await closeApp(tester);
  });

  testWidgets('without configured contacts a message is shown', (tester) async {
    await pumpApp(
      tester,
      const ContactPage(),
      overrides: overrides(
        SessionState.active,
        null,
        null,
        null,
        null,
        FakeSupport(const SupportInfo()),
      ),
    );

    expect(find.text(ProfileText.contactNotConfigured), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('the coupons are listed with their validity', (tester) async {
    await pumpApp(tester, const CouponsPage(), overrides: overrides());

    expect(find.text('Première recharge offerte'), findsOneWidget);
    expect(
      find.text(ProfileText.couponValidUntil('31/01/2027')),
      findsOneWidget,
    );
    expect(find.text('Coupon sans date'), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('no coupons shows an empty message', (tester) async {
    await pumpApp(
      tester,
      const CouponsPage(),
      overrides: overrides(
        SessionState.active,
        null,
        null,
        null,
        null,
        null,
        FakeCoupons(),
      ),
    );

    expect(find.text(ProfileText.couponsEmpty), findsOneWidget);
    await closeApp(tester);
  });
}
