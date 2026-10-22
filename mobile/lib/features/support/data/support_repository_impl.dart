import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../domain/support_info.dart';
import '../domain/support_repository.dart';

class SupportRepositoryImpl implements SupportRepository {
  SupportRepositoryImpl(this._client);

  final Dio _client;

  @override
  Future<SupportInfo> fetch() async {
    try {
      final json = (await _client.get<Map<String, dynamic>>(
        '/v1/support',
      )).data!;
      return SupportInfo(
        email: json['email'] as String?,
        phone: json['phone'] as String?,
        whatsapp: json['whatsapp'] as String?,
        agreementUrl: json['agreementUrl'] as String?,
      );
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }
}
