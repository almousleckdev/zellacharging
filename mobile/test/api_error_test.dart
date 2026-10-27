import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/app_language.dart';
import 'package:zella/core/network/api_error.dart';
import 'package:zella/core/network/api_error_kind.dart';

DioException _failure(int? status, [Map<String, dynamic>? body]) {
  final options = RequestOptions(path: '/x');
  return DioException(
    requestOptions: options,
    response: status == null
        ? null
        : Response<dynamic>(
            requestOptions: options,
            statusCode: status,
            data: body,
          ),
  );
}

void main() {
  setUp(() => AppLanguage.current = AppLanguage.fr);

  test('a known code is localized by the app, not parsed from server text', () {
    final error = ApiError.fromDio(
      _failure(402, {'code': 'INSUFFICIENT_FUNDS', 'detail': 'whatever'}),
    );
    expect(error.code, 'INSUFFICIENT_FUNDS');
    expect(error.message, contains('portefeuille'));
    AppLanguage.current = AppLanguage.en;
    expect(
      ApiError.fromDio(_failure(402, {'code': 'INSUFFICIENT_FUNDS'})).message,
      contains('wallet'),
    );
  });

  test(
    'an unknown code falls back to the already localized server message',
    () {
      final error = ApiError.fromDio(
        _failure(409, {
          'code': 'SOMETHING_NEW',
          'detail': 'Message du serveur.',
        }),
      );
      expect(error.message, 'Message du serveur.');
      expect(error.kind, ApiErrorKind.terminal);
    },
  );

  test('a server failure shows a short reference for support', () {
    final error = ApiError.fromDio(
      _failure(500, {
        'code': 'INTERNAL_ERROR',
        'correlationId': '1234abcd-0000-0000-0000-000000000000',
      }),
    );
    expect(error.message, contains('1234abcd'));
    expect(error.isRecoverable, isTrue);
    expect(error.message, isNot(contains('0000-0000')));
  });

  test('network loss is recoverable and a pending payment is uncertain', () {
    expect(ApiError.fromDio(_failure(null)).isRecoverable, isTrue);
    expect(ApiError.fromCode('PAYMENT_VERIFYING').isUncertain, isTrue);
    expect(ApiError.fromCode('TOPUP_PENDING').isUncertain, isTrue);
    expect(ApiError.fromCode('INSUFFICIENT_FUNDS').isRecoverable, isFalse);
  });

  test(
    'the payment messages never claim charging started or hide uncertainty',
    () {
      expect(
        ApiError.fromCode('PAYMENT_NOT_COMPLETED').message,
        contains('n’a pas démarré'),
      );
      final verifying = ApiError.fromCode('PAYMENT_VERIFYING').message;
      expect(verifying, contains('vérification'));
      expect(verifying, isNot(contains('Rien n’a été débité')));
    },
  );
}
