import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../network/network_providers.dart';
import 'session_controller.dart';

class SessionVerifier {
  SessionVerifier(this._ref);

  final Ref _ref;

  Future<void> check() async {
    if (_ref.read(sessionControllerProvider) != SessionStatus.authenticated) {
      return;
    }
    try {
      await _ref.read(dioProvider).get<dynamic>('/v1/me');
    } on DioException {
      return;
    }
  }
}

final sessionVerifierProvider = Provider<SessionVerifier>(SessionVerifier.new);
