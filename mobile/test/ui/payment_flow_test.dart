import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/format/money_format.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/charging/presentation/setup/charge_setup_page.dart';
import 'package:zella/features/charging/domain/session_quote.dart';
import 'package:zella/features/charging/domain/session_state.dart';
import 'package:zella/features/stations/domain/station.dart';
import 'package:zella/features/stations/domain/station_connector.dart';
import 'package:zella/features/wallet/domain/collect_payment.dart';
import 'package:zella/features/wallet/domain/mobile_operator.dart';
import 'package:zella/features/wallet/domain/topup_result.dart';
import 'package:zella/features/wallet/domain/wallet.dart';
import 'package:zella/features/wallet/domain/wallet_entry.dart';
import 'package:zella/features/wallet/domain/wallet_entry_detail.dart';

import 'package:zella/features/wallet/domain/withdrawal_result.dart';

import 'package:zella/features/wallet/domain/wallet_repository.dart';
import 'package:zella/features/wallet/presentation/topup_flow_controller.dart';
import 'package:zella/features/wallet/presentation/topup_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

class _ScriptedWallet implements WalletRepository {
  _ScriptedWallet(this.states, {this.available = 984567});

  final List<String> states;
  final int available;
  var polls = 0;
  var created = 0;

  @override
  Future<Wallet> fetch() async => Wallet(
    balanceMinor: available,
    reservedMinor: 0,
    availableMinor: available,
    currency: 'XAF',
    decimals: 0,
  );

  @override
  Future<TopupResult> topUp({
    required int amountMinor,
    required String msisdn,
    required MobileOperator operator,
    required String idempotencyKey,
  }) async {
    created++;
    return TopupResult(id: 't', state: states.first);
  }

  @override
  Future<TopupResult> topupStatus(String id) async {
    polls++;
    return TopupResult(
      id: id,
      state: states[polls.clamp(0, states.length - 1)],
    );
  }

  @override
  Future<WithdrawalResult> withdraw({
    required int amountMinor,
    required MobileOperator operator,
    required String password,
    required String idempotencyKey,
  }) => throw UnimplementedError();

  @override
  Future<WithdrawalResult> withdrawalStatus(String id) =>
      throw UnimplementedError();

  @override
  Future<List<WalletEntry>> entries() async => const [];

  @override
  Future<WalletEntryDetail> entryDetail(String id) =>
      throw UnimplementedError();

  @override
  Future<void> hideEntry(String id) => throw UnimplementedError();
}

class _ShortSessions extends FakeSessions {
  _ShortSessions() : super(SessionState.active);

  @override
  Future<SessionQuote> quote(String s, int c, int a) async => SessionQuote(
    amountMinor: a,
    currency: 'XAF',
    decimals: 0,
    availableMinor: 500,
    shortfallMinor: a - 500,
    minimumMinor: 1000,
    maximumMinor: 200000,
    vatPercent: 0,
    serviceFeePercent: 0,
  );
}

class _PluggingStation extends FakeStations {
  var calls = 0;

  @override
  Future<Station> byId(String id) async {
    calls++;
    final plugged = calls > 1;
    return Station(
      id: 'CP-1',
      online: true,
      availableConnectors: 1,
      totalConnectors: 2,
      connectors: [
        StationConnector(
          connectorId: 1,
          status: plugged ? 'Preparing' : 'Available',
          available: !plugged,
          startable: true,
        ),
        const StationConnector(
          connectorId: 2,
          status: 'Charging',
          available: false,
        ),
      ],
    );
  }
}

Future<void> _openTopup(WidgetTester tester, _ScriptedWallet wallet) async {
  await pumpApp(
    tester,
    const TopupPage(),
    overrides: overridesWith(
      wallet: wallet,
      extra: [
        collectPaymentProvider.overrideWithValue(
          CollectPayment(wallet, pollInterval: Duration.zero, maxPolls: 3),
        ),
      ],
    ),
  );
  await tester.enterText(find.byType(TextFormField).first, '5000');
  await tester.enterText(find.byType(TextFormField).last, '077 12 34 56');
}

Future<void> _tapPay(WidgetTester tester) async {
  await tester.tap(find.textContaining('Payer'));
  for (var i = 0; i < 6; i++) {
    await tester.pump(const Duration(milliseconds: 100));
  }
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('a confirmed top-up ends on the success panel', (tester) async {
    await _openTopup(tester, _ScriptedWallet(['PENDING', 'SUCCEEDED']));
    await _tapPay(tester);

    expect(find.text(PayFlowText.topupDoneTitle), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('a payment still pending is never shown as failed', (
    tester,
  ) async {
    final wallet = _ScriptedWallet(['PENDING']);
    await _openTopup(tester, wallet);
    await _tapPay(tester);

    expect(find.text(PayFlowText.verifyingTitle), findsOneWidget);
    expect(find.text(PayFlowText.failedTitle), findsNothing);
    expect(find.text(PayFlowText.retry), findsNothing);
    expect(find.text(PayFlowText.backToWallet), findsOneWidget);
    expect(wallet.created, 1);
    await closeApp(tester);
  });

  testWidgets('a declined payment offers a retry that returns to the form', (
    tester,
  ) async {
    await _openTopup(tester, _ScriptedWallet(['FAILED']));
    await _tapPay(tester);

    expect(find.text(PayFlowText.failedTitle), findsOneWidget);
    await tester.tap(find.text(PayFlowText.retry));
    await tester.pump(const Duration(milliseconds: 200));

    expect(find.text(PayText.methodTitle), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('the setup button asks for the exact shortfall', (tester) async {
    await pumpApp(
      tester,
      const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
      overrides: overridesWith(
        wallet: _ScriptedWallet(['SUCCEEDED'], available: 500),
        sessionRepo: _ShortSessions(),
      ),
    );
    await tester.enterText(find.byType(TextField).first, '2000');
    await tester.pump(const Duration(milliseconds: 600));

    expect(
      find.text(PayText.payAndStart(MoneyFormat.format(1500, 'XAF', 0))),
      findsOneWidget,
    );
    await closeApp(tester);
  });

  testWidgets('a wallet that covers the budget starts without payment', (
    tester,
  ) async {
    await pumpApp(
      tester,
      const ChargeSetupPage(stationId: 'CP-1', initialConnectorId: 1),
      overrides: overrides(),
    );
    await tester.pump(const Duration(milliseconds: 600));

    expect(
      find.widgetWithText(FilledButton, PayText.startCharging),
      findsOneWidget,
    );
    await closeApp(tester);
  });

  testWidgets(
    'a cable plugged in while the page is open selects its connector',
    (tester) async {
      await pumpApp(
        tester,
        const ChargeSetupPage(stationId: 'CP-1'),
        overrides: overridesWith(stationRepo: _PluggingStation()),
      );
      expect(find.text(PayText.selectConnectorFirst), findsOneWidget);

      await tester.pump(const Duration(seconds: 5));
      await tester.pump(const Duration(milliseconds: 400));

      expect(find.text(PayText.cableDetected(1)), findsOneWidget);
      expect(find.text(PayText.selectConnectorFirst), findsNothing);
      await closeApp(tester);
    },
  );
}
