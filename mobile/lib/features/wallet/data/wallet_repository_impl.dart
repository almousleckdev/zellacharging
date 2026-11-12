import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../domain/mobile_operator.dart';
import '../domain/payout_number.dart';
import '../domain/topup_result.dart';
import '../domain/wallet.dart';
import '../domain/wallet_entry.dart';
import '../domain/wallet_entry_detail.dart';
import '../domain/withdrawal_result.dart';
import '../domain/wallet_repository.dart';

class WalletRepositoryImpl implements WalletRepository {
  WalletRepositoryImpl(this._client);

  final Dio _client;

  @override
  Future<Wallet> fetch() => _guard(() async {
    final response = await _client.get<Map<String, dynamic>>('/v1/wallet');
    final json = response.data!;
    return Wallet(
      balanceMinor: (json['balanceMinor'] as num).toInt(),
      reservedMinor: (json['reservedMinor'] as num).toInt(),
      availableMinor: (json['availableMinor'] as num).toInt(),
      currency: json['currency'] as String,
      decimals: (json['decimals'] as num).toInt(),
      withdrawalsEnabled: json['withdrawalsEnabled'] as bool? ?? false,
      freeCharging: json['freeCharging'] as bool? ?? false,
      payoutNumber: json['payoutNumber'] as String?,
      payoutOperator: MobileOperator.values
          .where((operator) => operator.code == json['payoutOperator'])
          .firstOrNull,
      payoutNumbers: _payoutNumbers(json),
    );
  });

  @override
  Future<TopupResult> topUp({
    required int amountMinor,
    required String msisdn,
    required MobileOperator operator,
    required String idempotencyKey,
  }) => _guard(() async {
    final response = await _client.post<Map<String, dynamic>>(
      '/v1/wallet/topups',
      data: {
        'amountMinor': amountMinor,
        'msisdn': msisdn,
        'operator': operator.code,
      },
      options: Options(headers: {'Idempotency-Key': idempotencyKey}),
    );
    return _result(response.data!);
  });

  @override
  Future<TopupResult> topupStatus(String id) => _guard(() async {
    final response = await _client.get<Map<String, dynamic>>(
      '/v1/wallet/topups/$id',
    );
    return _result(response.data!);
  });

  @override
  Future<WithdrawalResult> withdraw({
    required int amountMinor,
    required MobileOperator operator,
    required String msisdn,
    required String password,
    required String idempotencyKey,
  }) => _guard(() async {
    final response = await _client.post<Map<String, dynamic>>(
      '/v1/wallet/withdrawals',
      data: {
        'amountMinor': amountMinor,
        'operator': operator.code,
        'msisdn': msisdn,
        'password': password,
      },
      options: Options(headers: {'Idempotency-Key': idempotencyKey}),
    );
    return _withdrawal(response.data!);
  });

  @override
  Future<WithdrawalResult> withdrawalStatus(String id) => _guard(() async {
    final response = await _client.get<Map<String, dynamic>>(
      '/v1/wallet/withdrawals/$id',
    );
    return _withdrawal(response.data!);
  });

  @override
  Future<List<WalletEntry>> entries() => _guard(() async {
    final response = await _client.get<List<dynamic>>(
      '/v1/wallet/transactions',
    );
    return [
      for (final item in response.data!)
        WalletEntry(
          id: (item as Map<String, dynamic>)['id'] as String,
          type: item['type'] as String,
          amountMinor: (item['amountMinor'] as num).toInt(),
          createdAt: DateTime.parse(item['createdAt'] as String),
        ),
    ];
  });

  @override
  Future<WalletEntryDetail> entryDetail(String id) => _guard(() async {
    final response = await _client.get<Map<String, dynamic>>(
      '/v1/wallet/transactions/$id',
    );
    final json = response.data!;
    return WalletEntryDetail(
      entry: WalletEntry(
        id: json['id'] as String,
        type: json['type'] as String,
        amountMinor: (json['amountMinor'] as num).toInt(),
        createdAt: DateTime.parse(json['createdAt'] as String),
      ),
      balanceAfterMinor: (json['balanceAfterMinor'] as num).toInt(),
      sessionId: json['sessionId'] as String?,
      operator: json['operator'] as String?,
      msisdn: json['msisdn'] as String?,
      state: json['state'] as String?,
      reference: json['reference'] as String?,
      providerReference: json['providerReference'] as String?,
    );
  });

  @override
  Future<void> hideEntry(String id) => _guard(() async {
    await _client.delete<void>('/v1/wallet/transactions/$id');
  });

  WithdrawalResult _withdrawal(Map<String, dynamic> json) => WithdrawalResult(
    id: json['id'] as String,
    state: json['state'] as String,
    failureCode: json['failureCode'] as String?,
  );

  TopupResult _result(Map<String, dynamic> json) => TopupResult(
    id: json['id'] as String,
    state: json['state'] as String,
    failureCode: json['failureCode'] as String?,
  );

  List<PayoutNumber> _payoutNumbers(Map<String, dynamic> json) {
    final listed = json['payoutNumbers'] as List<dynamic>?;
    if (listed == null) return _legacyPayoutNumber(json);
    return [
      for (final item in listed)
        if (MobileOperator.fromCode(
              (item as Map<String, dynamic>)['operator'] as String?,
            )
            case final operator?)
          PayoutNumber(msisdn: item['msisdn'] as String, operator: operator),
    ];
  }

  List<PayoutNumber> _legacyPayoutNumber(Map<String, dynamic> json) {
    final number = json['payoutNumber'] as String?;
    final operator = MobileOperator.fromCode(json['payoutOperator'] as String?);
    return number == null || operator == null
        ? const []
        : [PayoutNumber(msisdn: number, operator: operator)];
  }

  Future<T> _guard<T>(Future<T> Function() call) async {
    try {
      return await call();
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }
}
