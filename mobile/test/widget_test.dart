import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/auth/presentation/welcome_page.dart';

import 'ui/screens.dart';
import 'ui/test_app.dart';
import 'ui/test_fonts.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets('the welcome page greets a new customer', (tester) async {
    await pumpApp(tester, const WelcomePage(), overrides: overridesWith());

    expect(find.text(AuthText.welcomeTitle), findsOneWidget);
    await closeApp(tester);
  });
}
