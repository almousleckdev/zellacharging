import 'dart:typed_data';

import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../domain/profile.dart';
import '../domain/profile_repository.dart';

class ProfileRepositoryImpl implements ProfileRepository {
  ProfileRepositoryImpl(this._client);

  final Dio _client;

  Future<T> _guard<T>(Future<T> Function() call) async {
    try {
      return await call();
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }

  Future<Profile> _profile(Future<Response<Map<String, dynamic>>> call) =>
      _guard(() async {
        final json = (await call).data!;
        return Profile(
          firstName: json['firstName'] as String,
          lastName: json['lastName'] as String,
          email: json['email'] as String?,
          phone: json['phone'] as String?,
        );
      });

  @override
  Future<Profile> fetch() =>
      _profile(_client.get<Map<String, dynamic>>('/v1/me'));

  @override
  Future<Profile> updateName(String firstName, String lastName) => _profile(
    _client.patch<Map<String, dynamic>>(
      '/v1/me',
      data: {'firstName': firstName, 'lastName': lastName},
    ),
  );

  @override
  Future<Uint8List?> fetchAvatar() => _guard(() async {
    final response = await _client.get<List<int>>(
      '/v1/me/avatar',
      options: Options(responseType: ResponseType.bytes),
    );
    final data = response.data;
    return data == null || data.isEmpty ? null : Uint8List.fromList(data);
  });

  @override
  Future<void> uploadAvatar(Uint8List bytes, String contentType) => _guard(
    () => _client.put<void>(
      '/v1/me/avatar',
      data: Stream.fromIterable([bytes]),
      options: Options(
        contentType: contentType,
        headers: {Headers.contentLengthHeader: bytes.length},
      ),
    ),
  );

  @override
  Future<void> deleteAvatar() =>
      _guard(() => _client.delete<void>('/v1/me/avatar'));

  @override
  Future<void> changePassword(String currentPassword, String newPassword) =>
      _guard(
        () => _client.post<void>(
          '/v1/me/password',
          data: {
            'currentPassword': currentPassword,
            'newPassword': newPassword,
          },
        ),
      );

  @override
  Future<void> deleteAccount(String password) => _guard(
    () => _client.post<void>('/v1/me/delete', data: {'password': password}),
  );
}
