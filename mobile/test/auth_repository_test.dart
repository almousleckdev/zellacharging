import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/storage/token_store.dart';
import 'package:zella/features/auth/data/auth_repository_impl.dart';
import 'package:zella/features/auth/domain/contact_channel.dart';
import 'package:zella/features/auth/domain/registration_data.dart';
import 'package:zella/shared/otp_delivery.dart';

class _RecordingAdapter implements HttpClientAdapter {
  final requests = <RequestOptions>[];

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    requests.add(options);
    return ResponseBody.fromString(
      '{"customerId":"c1","channel":"PHONE","target":"${options.data['phone']}"}',
      201,
      headers: {
        Headers.contentTypeHeader: ['application/json'],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}

class _NoStore extends Fake implements TokenStore {}

void main() {
  late _RecordingAdapter adapter;
  late AuthRepositoryImpl repository;

  setUp(() {
    adapter = _RecordingAdapter();
    final client = Dio(BaseOptions(baseUrl: 'https://api.test'))
      ..httpClientAdapter = adapter;
    repository = AuthRepositoryImpl(client: client, store: _NoStore());
  });

  Map<String, dynamic> sentBody() =>
      adapter.requests.single.data as Map<String, dynamic>;

  test(
    'registration sends both contacts and the code goes to the phone',
    () async {
      final target = await repository.register(
        const RegistrationData(
          firstName: 'Aline',
          lastName: 'Obame',
          password: 'Secret123!',
          email: 'aline@example.com',
          phone: '+24177123456',
          delivery: OtpDelivery.sms,
        ),
      );

      expect(sentBody()['email'], 'aline@example.com');
      expect(sentBody()['phone'], '+24177123456');
      expect(sentBody()['delivery'], 'SMS');
      expect(target.channel, ContactChannel.phone);
      expect(target.value, '+24177123456');
    },
  );
}
