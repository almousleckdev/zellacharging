import 'mobile_operator.dart';
import 'topup_result.dart';
import 'wallet.dart';

abstract class WalletRepository {
  Future<Wallet> fetch();

  Future<TopupResult> topUp({
    required int amountMinor,
    required String msisdn,
    required MobileOperator operator,
    required String idempotencyKey,
  });

  Future<TopupResult> topupStatus(String id);
}
