import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/network/api_error.dart';
import 'package:zella/features/charging/domain/charge_quote.dart';
import 'package:zella/features/charging/domain/charge_request.dart';
import 'package:zella/features/charging/domain/charging_session.dart';
import 'package:zella/features/charging/domain/session_quote.dart';
import 'package:zella/features/charging/domain/session_repository.dart';
import 'package:zella/features/charging/domain/session_state.dart';
import 'package:zella/features/charging/domain/start_charge.dart';
import 'package:zella/features/wallet/domain/mobile_operator.dart';
import 'package:zella/features/wallet/domain/topup_result.dart';
import 'package:zella/features/wallet/domain/wallet.dart';
import 'package:zella/features/wallet/domain/wallet_repository.dart';

class _FakeWallets implements WalletRepository {
  _FakeWallets(this.available, {this.states = const ['SUCCEEDED']});

  final int available;
  final List<String> states;
  final topups = <int>[];
  var polls = 0;

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
    topups.add(amountMinor);
    return TopupResult(id: 't1', state: states.first);
  }

  @override
  Future<TopupResult> topupStatus(String id) async {
    polls++;
    return TopupResult(
      id: id,
      state: states[polls.clamp(0, states.length - 1)],
    );
  }
}

class _FakeSessions implements SessionRepository {
  final started = <int>[];
  final keys = <String>[];

  @override
  Future<void> delete(String sessionId) => throw UnimplementedError();

  @override
  Future<ChargingSession> start(
    String stationId,
    int connectorId,
    int amount, {
    required String idempotencyKey,
  }) {
    started.add(amount);
    keys.add(idempotencyKey);
    return Future.value(
      ChargingSession(
        id: 's1',
        stationId: stationId,
        connectorId: connectorId,
        state: SessionState.startAccepted,
        createdAt: DateTime(2026),
      ),
    );
  }

  @override
  Future<SessionQuote> quote(String stationId, int connectorId, int amount) =>
      throw UnimplementedError();

  @override
  Future<ChargingSession> stop(String sessionId) => throw UnimplementedError();

  @override
  Future<ChargingSession> get(String sessionId) => throw UnimplementedError();

  @override
  Future<ChargingSession?> active() => throw UnimplementedError();

  @override
  Future<List<ChargingSession>> history() => throw UnimplementedError();
}

ChargeRequest _request({bool withPayment = true}) => ChargeRequest(
  stationId: 'CP-1',
  connectorId: 1,
  amountMinor: 2000,
  attemptKey: 'k',
  payment: withPayment
      ? const ChargePayment(
          operator: MobileOperator.airtel,
          msisdn: '+24177000000',
        )
      : null,
);

StartCharge _useCase(_FakeWallets wallets, _FakeSessions sessions) =>
    StartCharge(wallets, sessions, pollInterval: Duration.zero, maxPolls: 3);

void main() {
  test('quote only asks for the missing part of the amount', () {
    const wallet = Wallet(
      balanceMinor: 1500,
      reservedMinor: 0,
      availableMinor: 1500,
      currency: 'XAF',
      decimals: 0,
    );
    expect(ChargeQuote.of(2000, wallet).shortfallMinor, 500);
    expect(ChargeQuote.of(1000, wallet).needsPayment, isFalse);
  });

  test('starts straight away when the wallet covers the amount', () async {
    final wallets = _FakeWallets(5000);
    final sessions = _FakeSessions();
    await _useCase(wallets, sessions).call(_request(withPayment: false));
    expect(wallets.topups, isEmpty);
    expect(sessions.started, [2000]);
  });

  test('pays only the shortfall then starts', () async {
    final wallets = _FakeWallets(1200);
    final sessions = _FakeSessions();
    await _useCase(wallets, sessions).call(_request());
    expect(wallets.topups, [800]);
    expect(sessions.started, [2000]);
  });

  test('waits for a pending payment before starting', () async {
    final wallets = _FakeWallets(
      0,
      states: ['PENDING', 'PENDING', 'SUCCEEDED'],
    );
    final sessions = _FakeSessions();
    await _useCase(wallets, sessions).call(_request());
    expect(wallets.polls, greaterThan(0));
    expect(sessions.started, [2000]);
  });

  test('never starts when the payment fails', () async {
    final wallets = _FakeWallets(0, states: ['FAILED']);
    final sessions = _FakeSessions();
    await expectLater(
      _useCase(wallets, sessions).call(_request()),
      throwsA(isA<ApiError>()),
    );
    expect(sessions.started, isEmpty);
  });

  test('refuses to start without a payment method when short', () async {
    final sessions = _FakeSessions();
    await expectLater(
      _useCase(_FakeWallets(0), sessions).call(_request(withPayment: false)),
      throwsA(isA<ApiError>()),
    );
    expect(sessions.started, isEmpty);
  });

  test(
    'a payment still pending is reported as uncertain, never as failed',
    () async {
      final wallets = _FakeWallets(0, states: ['PENDING']);
      final sessions = _FakeSessions();
      await expectLater(
        _useCase(wallets, sessions).call(_request()),
        throwsA(
          isA<ApiError>()
              .having((e) => e.code, 'code', 'PAYMENT_VERIFYING')
              .having((e) => e.isUncertain, 'uncertain', isTrue),
        ),
      );
      expect(sessions.started, isEmpty);
    },
  );

  test('a failed or expired payment is a definite failure', () async {
    for (final state in ['FAILED', 'EXPIRED']) {
      final sessions = _FakeSessions();
      await expectLater(
        _useCase(_FakeWallets(0, states: [state]), sessions).call(_request()),
        throwsA(
          isA<ApiError>()
              .having((e) => e.code, 'code', 'PAYMENT_NOT_COMPLETED')
              .having((e) => e.isUncertain, 'uncertain', isFalse),
        ),
      );
      expect(sessions.started, isEmpty);
    }
  });

  test(
    'the session start carries the attempt key so a retry cannot start twice',
    () async {
      final sessions = _FakeSessions();
      await _useCase(
        _FakeWallets(5000),
        sessions,
      ).call(_request(withPayment: false));
      expect(sessions.keys, ['k']);
    },
  );
}
