import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../domain/profile.dart';
import '../domain/profile_repository.dart';

class ProfileRepositoryImpl implements ProfileRepository {
  ProfileRepositoryImpl(this._client);

  final Dio _client;

  @override
  Future<Profile> fetch() async {
    try {
      final response = await _client.get<Map<String, dynamic>>('/v1/me');
      final json = response.data!;
      return Profile(
        firstName: json['firstName'] as String,
        lastName: json['lastName'] as String,
        email: json['email'] as String?,
        phone: json['phone'] as String?,
      );
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }
}
