import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/charging/presentation/setup/charge_setup_page.dart';
import 'package:zella/shared/widgets/pay/amount_card.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';

void main() {
  setUpAll(loadTestFonts);

  testWidgets(
    'the customer never types an amount, the wallet balance is used',
    (tester) async {
      await pumpApp(
        tester,
        const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
        overrides: overrides(),
      );
      await tester.pump(const Duration(milliseconds: 600));

      expect(find.byType(AmountCard), findsNothing);
      expect(find.byType(TextField), findsNothing);
      expect(find.text(PayText.balanceUsed), findsOneWidget);
      await closeApp(tester);
    },
  );
}
