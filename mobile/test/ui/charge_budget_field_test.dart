import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/charging/presentation/setup/charge_setup_page.dart';
import 'package:zella/features/wallet/domain/wallet.dart';
import 'package:zella/shared/widgets/pay/amount_card.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

class _FreeWallet extends FakeWallet {
  @override
  Future<Wallet> fetch() async => const Wallet(
    balanceMinor: 0,
    reservedMinor: 0,
    availableMinor: 0,
    currency: 'XAF',
    decimals: 0,
    freeCharging: true,
  );
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('a paying customer chooses the amount to charge', (tester) async {
    await pumpApp(
      tester,
      const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
      overrides: overrides(),
    );
    await tester.pump(const Duration(milliseconds: 600));

    expect(find.byType(AmountCard), findsOneWidget);
    expect(find.byType(TextField), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('a free account is never asked for an amount', (tester) async {
    await pumpApp(
      tester,
      const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
      overrides: overridesWith(wallet: _FreeWallet()),
    );
    await tester.pump(const Duration(milliseconds: 600));

    expect(find.byType(AmountCard), findsNothing);
    expect(find.text(PayText.freeCharging), findsOneWidget);
    await closeApp(tester);
  });
}
