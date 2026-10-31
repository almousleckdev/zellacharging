import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../../../core/storage/token_store.dart';
import '../domain/auth_repository.dart';
import '../domain/contact_channel.dart';
import '../domain/registration_data.dart';
import '../domain/social_credential.dart';
import '../domain/verification_target.dart';

class AuthRepositoryImpl implements AuthRepository {
  AuthRepositoryImpl({required this.client, required this.store});

  final Dio client;
  final TokenStore store;

  Future<T> _guard<T>(Future<T> Function() call) async {
    try {
      return await call();
    } on DioException catch (error) {
      throw ApiError.fromDio(error);
    }
  }

  @override
  Future<VerificationTarget> register(RegistrationData data) =>
      _guard(() async {
        await client.post<void>(
          '/v1/auth/register',
          data: {
            'firstName': data.firstName,
            'lastName': data.lastName,
            'password': data.password,
            'email': data.email,
            if (data.phone != null) 'phone': data.phone,
          },
        );
        return VerificationTarget(
          channel: ContactChannel.email,
          value: data.email,
        );
      });

  @override
  Future<void> verify(VerificationTarget target, String code) => _guard(
    () => client.post<void>(
      '/v1/auth/verify',
      data: {
        'channel': target.channel.apiName,
        'target': target.value,
        'code': code,
      },
    ),
  );

  @override
  Future<void> resendCode(VerificationTarget target) => _guard(
    () => client.post<void>(
      '/v1/auth/verify/resend',
      data: {'channel': target.channel.apiName, 'target': target.value},
    ),
  );

  @override
  Future<void> forgotPassword(String email) => _guard(
    () => client.post<void>('/v1/auth/password/forgot', data: {'email': email}),
  );

  @override
  Future<void> resetPassword({
    required String email,
    required String code,
    required String newPassword,
  }) => _guard(
    () => client.post<void>(
      '/v1/auth/password/reset',
      data: {'email': email, 'code': code, 'newPassword': newPassword},
    ),
  );

  @override
  Future<void> login({required String identifier, required String password}) =>
      _guard(() async {
        final response = await client.post<Map<String, dynamic>>(
          '/v1/auth/login',
          data: {'identifier': identifier, 'password': password},
        );
        await _saveTokens(response.data!);
      });

  @override
  Future<void> socialLogin(SocialCredential credential) => _guard(() async {
    final response = await client.post<Map<String, dynamic>>(
      '/v1/auth/social',
      data: credential.toJson(),
    );
    await _saveTokens(response.data!);
  });

  Future<void> _saveTokens(Map<String, dynamic> body) => store.save(
    accessToken: body['accessToken'] as String,
    refreshToken: body['refreshToken'] as String,
  );
}
