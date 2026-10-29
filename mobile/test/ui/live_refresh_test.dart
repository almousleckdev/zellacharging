import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/core/refresh/live_data_refresher.dart';
import 'package:zella/features/charging/domain/charging_session.dart';
import 'package:zella/features/charging/domain/session_state.dart';
import 'package:zella/features/charging/presentation/active_session_banner.dart';
import 'package:zella/features/charging/presentation/session_formatting.dart';
import 'package:zella/features/wallet/domain/wallet.dart';
import 'package:zella/features/wallet/presentation/wallet_providers.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

class _EndingSessions extends FakeSessions {
  _EndingSessions() : super(SessionState.active);

  var calls = 0;

  @override
  Future<ChargingSession?> active() async =>
      calls++ == 0 ? session(SessionState.active) : null;
}

class _GrowingWallet extends FakeWallet {
  var balance = 1000;

  @override
  Future<Wallet> fetch() async {
    final current = balance;
    balance += 500;
    return Wallet(
      balanceMinor: current,
      reservedMinor: 0,
      availableMinor: current,
      currency: 'XAF',
      decimals: 0,
      withdrawalsEnabled: true,
    );
  }
}

class _Balance extends ConsumerWidget {
  const _Balance();

  @override
  Widget build(BuildContext context, WidgetRef ref) =>
      Text('balance ${ref.watch(walletProvider).value?.balanceMinor}');
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets(
    'the home banner disappears and the wallet updates without any manual refresh',
    (tester) async {
      await pumpApp(
        tester,
        const LiveDataRefresher(
          child: Scaffold(
            body: Column(children: [ActiveSessionBanner(), _Balance()]),
          ),
        ),
        overrides: overridesWith(
          sessionRepo: _EndingSessions(),
          wallet: _GrowingWallet(),
        ),
      );

      expect(find.textContaining(ChargeText.activeSession), findsOneWidget);
      expect(find.text('balance 1000'), findsOneWidget);

      await tester.pump(const Duration(seconds: 16));
      await settle(tester);

      expect(find.textContaining(ChargeText.activeSession), findsNothing);
      expect(find.text('balance 1500'), findsOneWidget);
      await closeApp(tester);
    },
  );

  test(
    'the energy keeps the three decimals of the meter without rounding drift',
    () {
      ChargingSession withEnergy(double kwh) => ChargingSession(
        id: 's',
        stationId: 'CP-1',
        connectorId: 1,
        state: SessionState.completed,
        createdAt: DateTime(2026, 10, 29),
        energyKwh: kwh,
      );

      expect(withEnergy(4.515).energyText, '4.515 ${Strings.kwhUnit}');
      expect(withEnergy(18.5).energyText, '18.500 ${Strings.kwhUnit}');
    },
  );
}
