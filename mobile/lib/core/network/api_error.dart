import 'package:dio/dio.dart';

import '../l10n/strings.dart';

class ApiError implements Exception {
  const ApiError({required this.code, required this.message});

  final String code;
  final String message;

  factory ApiError.fromCode(String? code) => ApiError(
    code: code ?? 'UNKNOWN',
    message: Strings.errorsByCode[code] ?? Strings.unknownError,
  );

  factory ApiError.fromDio(DioException error) {
    final data = error.response?.data;
    if (data is Map && data['code'] is String) {
      final code = data['code'] as String;
      return ApiError(
        code: code,
        message: Strings.errorsByCode[code] ?? Strings.unknownError,
      );
    }
    final unreachable = error.response == null;
    return ApiError(
      code: unreachable ? 'NETWORK' : 'UNKNOWN',
      message: unreachable ? Strings.networkError : Strings.unknownError,
    );
  }
}
