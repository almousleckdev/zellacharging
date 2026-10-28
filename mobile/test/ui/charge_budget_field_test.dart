import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/features/charging/presentation/setup/charge_setup_page.dart';
import 'package:zella/shared/widgets/pay/amount_chips.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets('the budget is typed by the customer, with no preset amounts', (
    tester,
  ) async {
    await pumpApp(
      tester,
      const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
      overrides: overrides(),
    );

    final budget = tester.widget<TextField>(find.byType(TextField).first);
    expect(budget.controller!.text, isEmpty);
    expect(find.byType(AmountChips), findsNothing);
    await closeApp(tester);
  });
}
