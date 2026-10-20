import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_error.dart';
import '../../../wallet/presentation/wallet_providers.dart';
import '../../domain/charge_request.dart';
import '../../domain/charging_session.dart';
import '../session_providers.dart';

class ChargeLauncher {
  const ChargeLauncher(this._ref);

  final WidgetRef _ref;

  Future<ChargingSession> launch(ChargeRequest request) async {
    try {
      final session = await _ref.read(startChargeProvider).call(request);
      _ref.invalidate(activeSessionProvider);
      return session;
    } on ApiError {
      rethrow;
    } finally {
      _ref.invalidate(walletProvider);
    }
  }
}
