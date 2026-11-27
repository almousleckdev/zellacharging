import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/core/network/api_error.dart';
import 'package:zella/features/charging/presentation/setup/charge_setup_page.dart';
import 'package:zella/features/wallet/domain/wallet.dart';
import 'package:zella/features/wallet/presentation/wallet_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

const _free = Wallet(
  balanceMinor: 0,
  reservedMinor: 0,
  availableMinor: 0,
  currency: 'XAF',
  decimals: 0,
  freeCharging: true,
);

class _SlowWallet extends FakeWallet {
  @override
  Future<Wallet> fetch() => Completer<Wallet>().future;
}

class _FlakyWallet extends FakeWallet {
  bool online = false;

  @override
  Future<Wallet> fetch() async {
    if (!online) throw ApiError.fromCode('NETWORK');
    return _free;
  }
}

class _FreeWallet extends FakeWallet {
  @override
  Future<Wallet> fetch() async => _free;
}

const _setup = ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1);

Future<void> _open(WidgetTester tester, Widget page, FakeWallet wallet) async {
  await pumpApp(tester, page, overrides: overridesWith(wallet: wallet));
  await tester.pump(const Duration(milliseconds: 600));
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('a wallet still loading is never asked to top up', (
    tester,
  ) async {
    await _open(tester, _setup, _SlowWallet());

    expect(find.text(PayText.rechargeAccount), findsNothing);
    expect(find.text(PayText.walletUnknown), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('a wallet that failed to load is reloaded, not topped up', (
    tester,
  ) async {
    final wallet = _FlakyWallet();
    await _open(tester, _setup, wallet);

    expect(find.text(PayText.rechargeAccount), findsNothing);
    wallet.online = true;
    await tester.tap(find.text(PayText.refreshWallet));
    await tester.pump(const Duration(milliseconds: 600));

    expect(find.text(PayText.freeCharging), findsOneWidget);
    expect(find.text(PayText.rechargeAccount), findsNothing);
    await closeApp(tester);
  });

  testWidgets('the wallet page says when charging is free', (tester) async {
    await _open(tester, const WalletPage(), _FreeWallet());

    expect(find.text(PayText.freeCharging), findsOneWidget);
    expect(find.text(PayText.freeChargingNote), findsOneWidget);
    await closeApp(tester);
  });
}
