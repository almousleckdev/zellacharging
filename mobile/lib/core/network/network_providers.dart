import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../session/session_controller.dart';
import '../storage/secure_token_store.dart';
import '../storage/token_store.dart';
import 'auth_interceptor.dart';
import 'language_interceptor.dart';
import 'retry_interceptor.dart';
import 'token_refresher.dart';

const _baseUrl = String.fromEnvironment(
  'API_BASE_URL',
  defaultValue: 'https://apicharging.up.railway.app',
);

final tokenStoreProvider = Provider<TokenStore>((ref) => SecureTokenStore());

Dio _newClient() => Dio(
  BaseOptions(
    baseUrl: _baseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 20),
    contentType: Headers.jsonContentType,
  ),
);

bool _isSignedIn(SessionStatus status) => status == SessionStatus.authenticated;

final dioProvider = Provider<Dio>((ref) {
  final store = ref.watch(tokenStoreProvider);
  ref.watch(sessionControllerProvider.select(_isSignedIn));
  final client = _newClient();
  final refresher = TokenRefresher(plainClient: _newClient(), store: store);
  client.interceptors.add(LanguageInterceptor());
  client.interceptors.add(
    AuthInterceptor(
      store: store,
      refresher: refresher,
      client: client,
      onSessionExpired: ({required replaced}) => ref
          .read(sessionControllerProvider.notifier)
          .expire(replaced: replaced),
    ),
  );
  client.interceptors.add(RetryInterceptor(client: client));
  return client;
});
