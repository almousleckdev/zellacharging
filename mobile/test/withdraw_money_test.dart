import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/network/api_error.dart';
import 'package:zella/features/wallet/domain/mobile_operator.dart';
import 'package:zella/features/wallet/domain/topup_result.dart';
import 'package:zella/features/wallet/domain/wallet.dart';
import 'package:zella/features/wallet/domain/wallet_entry.dart';
import 'package:zella/features/wallet/domain/wallet_entry_detail.dart';

import 'package:zella/features/wallet/domain/wallet_repository.dart';
import 'package:zella/features/wallet/domain/withdraw_money.dart';
import 'package:zella/features/wallet/domain/withdraw_stage.dart';
import 'package:zella/features/wallet/domain/withdrawal_result.dart';

class _Wallets implements WalletRepository {
  _Wallets(this.states);

  final List<String> states;
  var created = 0;
  var polls = 0;

  @override
  Future<WithdrawalResult> withdraw({
    required int amountMinor,
    required MobileOperator operator,
    required String msisdn,
    required String password,
    required String idempotencyKey,
  }) async {
    created++;
    return WithdrawalResult(id: 'w1', state: states.first);
  }

  @override
  Future<WithdrawalResult> withdrawalStatus(String id) async {
    polls++;
    return WithdrawalResult(
      id: id,
      state: states[polls.clamp(0, states.length - 1)],
    );
  }

  @override
  Future<Wallet> fetch() => throw UnimplementedError();

  @override
  Future<TopupResult> topUp({
    required int amountMinor,
    required String msisdn,
    required MobileOperator operator,
    required String idempotencyKey,
  }) => throw UnimplementedError();

  @override
  Future<TopupResult> topupStatus(String id) => throw UnimplementedError();

  @override
  Future<List<WalletEntry>> entries() async => const [];

  @override
  Future<WalletEntryDetail> entryDetail(String id) =>
      throw UnimplementedError();

  @override
  Future<void> hideEntry(String id) => throw UnimplementedError();
}

Future<List<WithdrawStage>> _run(_Wallets wallets) async {
  final stages = <WithdrawStage>[];
  await WithdrawMoney(wallets, pollInterval: Duration.zero, maxPolls: 3)(
    amountMinor: 2000,
    operator: MobileOperator.moov,
    msisdn: '+24162000000',
    password: 'secret123',
    idempotencyKey: 'k',
    onStage: stages.add,
  );
  return stages;
}

void main() {
  test('a withdrawal reports its stages until the money is sent', () async {
    final stages = await _run(_Wallets(['PROCESSING', 'PROCESSING', 'PAID']));
    expect(stages, [
      WithdrawStage.requesting,
      WithdrawStage.processing,
      WithdrawStage.completed,
    ]);
  });

  test('an instant payout skips the processing stage', () async {
    final stages = await _run(_Wallets(['PAID']));
    expect(stages, [WithdrawStage.requesting, WithdrawStage.completed]);
  });

  test('a failed withdrawal says the money stayed in the wallet', () async {
    await expectLater(
      _run(_Wallets(['FAILED'])),
      throwsA(
        isA<ApiError>()
            .having((e) => e.code, 'code', 'WITHDRAWAL_FAILED')
            .having((e) => e.isUncertain, 'uncertain', isFalse)
            .having((e) => e.message, 'message', contains('portefeuille')),
      ),
    );
  });

  test('a withdrawal under review or still processing is uncertain', () async {
    for (final state in ['REVIEW', 'PROCESSING']) {
      await expectLater(
        _run(_Wallets([state])),
        throwsA(
          isA<ApiError>()
              .having((e) => e.code, 'code', 'WITHDRAWAL_VERIFYING')
              .having((e) => e.isUncertain, 'uncertain', isTrue),
        ),
      );
    }
  });

  test('one request is sent however long the wait is', () async {
    final wallets = _Wallets(['PROCESSING', 'PAID']);
    await _run(wallets);
    expect(wallets.created, 1);
  });
}
