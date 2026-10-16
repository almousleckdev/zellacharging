import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../domain/mobile_operator.dart';
import '../domain/topup_result.dart';
import '../domain/wallet.dart';
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

  TopupResult _result(Map<String, dynamic> json) => TopupResult(
    id: json['id'] as String,
    state: json['state'] as String,
    failureCode: json['failureCode'] as String?,
  );

  Future<T> _guard<T>(Future<T> Function() call) async {
    try {
      return await call();
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }
}
