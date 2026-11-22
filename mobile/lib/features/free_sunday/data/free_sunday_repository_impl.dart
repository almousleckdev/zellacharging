import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../domain/free_sunday_offer.dart';
import '../domain/free_sunday_repository.dart';
import 'free_sunday_mapper.dart';

class FreeSundayRepositoryImpl implements FreeSundayRepository {
  FreeSundayRepositoryImpl(this._client);

  final Dio _client;

  @override
  Future<FreeSundayOffer> current() async {
    try {
      final response = await _client.get<Map<String, dynamic>>(
        '/v1/free-sunday',
      );
      return freeSundayOfferFromJson(response.data!);
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }
}
