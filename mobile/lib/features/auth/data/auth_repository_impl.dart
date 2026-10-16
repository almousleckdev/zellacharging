import 'package:dio/dio.dart';

import '../../../core/network/api_error.dart';
import '../../../core/storage/token_store.dart';
import '../domain/auth_repository.dart';
import '../domain/contact_channel.dart';
import '../domain/registration_data.dart';
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
  Future<VerificationTarget> register(
    RegistrationData data,
  ) => _guard(() async {
    await client.post<void>(
      '/v1/auth/register',
      data: {
        'firstName': data.firstName,
        'lastName': data.lastName,
        'password': data.password,
        if (data.email != null) 'email': data.email,
        if (data.phone != null) 'phone': data.phone,
      },
    );
    return data.email != null
        ? VerificationTarget(channel: ContactChannel.email, value: data.email!)
        : VerificationTarget(channel: ContactChannel.phone, value: data.phone!);
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
  Future<void> login({required String identifier, required String password}) =>
      _guard(() async {
        final response = await client.post<Map<String, dynamic>>(
          '/v1/auth/login',
          data: {'identifier': identifier, 'password': password},
        );
        await store.save(
          accessToken: response.data!['accessToken'] as String,
          refreshToken: response.data!['refreshToken'] as String,
        );
      });
}
