import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/network/api_error.dart';
import 'package:zella/features/wallet/domain/collect_payment.dart';
import 'package:zella/features/wallet/domain/mobile_operator.dart';
import 'package:zella/features/wallet/domain/payment_stage.dart';
import 'package:zella/features/wallet/domain/topup_result.dart';
import 'package:zella/features/wallet/domain/wallet.dart';
import 'package:zella/features/wallet/domain/wallet_entry.dart';
import 'package:zella/features/wallet/domain/wallet_entry_detail.dart';

import 'package:zella/features/wallet/domain/withdrawal_result.dart';

import 'package:zella/features/wallet/domain/wallet_repository.dart';

class _Wallets implements WalletRepository {
  _Wallets(this.states, {this.failureCode});

  final List<String> states;
  final String? failureCode;
  var created = 0;
  var polls = 0;

  @override
  Future<Wallet> fetch() => throw UnimplementedError();

  @override
  Future<TopupResult> topUp({
    required int amountMinor,
    required String msisdn,
    required MobileOperator operator,
    required String idempotencyKey,
  }) async {
    created++;
    return TopupResult(id: 't', state: states.first, failureCode: failureCode);
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
    required String msisdn,
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

Future<List<PaymentStage>> _run(_Wallets wallets) async {
  final stages = <PaymentStage>[];
  await CollectPayment(wallets, pollInterval: Duration.zero, maxPolls: 3)(
    amountMinor: 1000,
    msisdn: '+24177123456',
    operator: MobileOperator.airtel,
    idempotencyKey: 'k',
    onStage: stages.add,
  );
  return stages;
}

void main() {
  test('stages follow the real progress of a delayed approval', () async {
    final stages = await _run(_Wallets(['PENDING', 'PENDING', 'SUCCEEDED']));
    expect(stages, [
      PaymentStage.requesting,
      PaymentStage.awaitingApproval,
      PaymentStage.confirmed,
    ]);
  });

  test('an instant success skips the waiting stage', () async {
    final stages = await _run(_Wallets(['SUCCEEDED']));
    expect(stages, [PaymentStage.requesting, PaymentStage.confirmed]);
  });

  test('a payment still pending when we stop waiting is uncertain', () async {
    await expectLater(
      _run(_Wallets(['PENDING'])),
      throwsA(
        isA<ApiError>()
            .having((e) => e.code, 'code', 'PAYMENT_VERIFYING')
            .having((e) => e.isUncertain, 'uncertain', isTrue),
      ),
    );
  });

  test('declined and expired payments are definite failures', () async {
    for (final state in ['FAILED', 'EXPIRED']) {
      await expectLater(
        _run(_Wallets([state])),
        throwsA(
          isA<ApiError>().having(
            (e) => e.code,
            'code',
            'PAYMENT_NOT_COMPLETED',
          ),
        ),
      );
    }
  });

  test('an invalid mobile money number gets its own clear message', () async {
    await expectLater(
      _run(_Wallets(['FAILED'], failureCode: 'INVALID_ACCOUNT_NUMBER')),
      throwsA(
        isA<ApiError>()
            .having((e) => e.code, 'code', 'INVALID_ACCOUNT_NUMBER')
            .having((e) => e.message, 'message', contains('Moov Money')),
      ),
    );
  });

  test('an unknown failure code falls back to the generic message', () async {
    await expectLater(
      _run(_Wallets(['FAILED'], failureCode: 'SOMETHING_ELSE')),
      throwsA(
        isA<ApiError>().having((e) => e.code, 'code', 'PAYMENT_NOT_COMPLETED'),
      ),
    );
  });

  test('only one payment is ever created for one attempt', () async {
    final wallets = _Wallets(['PENDING', 'SUCCEEDED']);
    await _run(wallets);
    expect(wallets.created, 1);
  });
}
