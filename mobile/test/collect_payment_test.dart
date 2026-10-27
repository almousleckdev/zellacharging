import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/network/api_error.dart';
import 'package:zella/features/wallet/domain/collect_payment.dart';
import 'package:zella/features/wallet/domain/mobile_operator.dart';
import 'package:zella/features/wallet/domain/payment_stage.dart';
import 'package:zella/features/wallet/domain/topup_result.dart';
import 'package:zella/features/wallet/domain/wallet.dart';
import 'package:zella/features/wallet/domain/wallet_repository.dart';

class _Wallets implements WalletRepository {
  _Wallets(this.states);

  final List<String> states;
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

  test('only one payment is ever created for one attempt', () async {
    final wallets = _Wallets(['PENDING', 'SUCCEEDED']);
    await _run(wallets);
    expect(wallets.created, 1);
  });
}
