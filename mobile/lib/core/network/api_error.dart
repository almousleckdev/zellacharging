import 'package:dio/dio.dart';

import '../l10n/strings.dart';
import '../l10n/suspension_text.dart';
import 'api_error_kind.dart';

class ApiError implements Exception {
  const ApiError({
    required this.code,
    required this.message,
    this.correlationId,
    this.kind = ApiErrorKind.terminal,
  });

  final String code;
  final String message;
  final String? correlationId;
  final ApiErrorKind kind;

  bool get isUncertain => kind == ApiErrorKind.uncertain;

  bool get isRecoverable => kind == ApiErrorKind.recoverable;

  factory ApiError.fromCode(String? code) {
    final resolved = code ?? 'UNKNOWN';
    return ApiError(
      code: resolved,
      message: ErrorText.byCode[resolved] ?? Strings.unknownError,
      kind: ApiErrorKind.of(resolved, null),
    );
  }

  static String? _owingMessage(String code, Map data) {
    final owed = data['owedMinor'];
    if (code != 'ACCOUNT_SUSPENDED' || owed is! num) return null;
    return SuspensionText.owing(owed.toInt());
  }

  factory ApiError.fromDio(DioException error) {
    final status = error.response?.statusCode;
    final data = error.response?.data;
    if (data is Map && data['code'] is String) {
      final code = data['code'] as String;
      final reference = data['correlationId'] as String?;
      final known = _owingMessage(code, data) ?? ErrorText.byCode[code];
      final detail = data['detail'] is String ? data['detail'] as String : null;
      final base = known ?? detail ?? Strings.unknownError;
      final unexplained = known == null && detail == null;
      return ApiError(
        code: code,
        message: unexplained || (status ?? 0) >= 500
            ? ErrorText.withReference(base, reference)
            : base,
        correlationId: reference,
        kind: ApiErrorKind.of(code, status),
      );
    }
    final unreachable = error.response == null;
    return ApiError(
      code: unreachable ? 'NETWORK' : 'UNKNOWN',
      message: unreachable ? Strings.networkError : Strings.unknownError,
      kind: ApiErrorKind.of(unreachable ? 'NETWORK' : 'UNKNOWN', status),
    );
  }
}
