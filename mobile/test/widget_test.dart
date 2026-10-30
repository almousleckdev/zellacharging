import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/auth/presentation/reset_success_page.dart';

import 'ui/screens.dart';
import 'ui/test_app.dart';
import 'ui/test_fonts.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets('the reset success page sends the customer to login', (
    tester,
  ) async {
    await pumpApp(tester, const ResetSuccessPage(), overrides: overridesWith());

    expect(find.text(AuthText.resetDoneTitle), findsOneWidget);
    expect(find.text(AuthText.goToLogin), findsOneWidget);
    await closeApp(tester);
  });
}
