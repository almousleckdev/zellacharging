import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_error.dart';
import '../../wallet/presentation/wallet_providers.dart';
import '../domain/charge_request.dart';
import '../domain/charge_stage.dart';
import '../domain/charging_session.dart';
import 'charge_flow_state.dart';
import 'session_providers.dart';

final chargeFlowProvider =
    NotifierProvider.autoDispose<ChargeFlowController, ChargeFlowState>(
      ChargeFlowController.new,
    );

class ChargeFlowController extends Notifier<ChargeFlowState> {
  @override
  ChargeFlowState build() => const ChargeFlowState.idle();

  Future<ChargingSession?> launch(ChargeRequest request) async {
    state = ChargeFlowState.running(
      request.payment == null
          ? ChargeStage.startingCharge
          : ChargeStage.requestingPayment,
    );
    try {
      final session = await ref
          .read(startChargeProvider)
          .call(request, onStage: _advance);
      ref.invalidate(activeSessionProvider);
      return session;
    } on ApiError catch (error) {
      if (ref.mounted) state = ChargeFlowState.failed(error);
      return null;
    } finally {
      if (ref.mounted) ref.invalidate(walletProvider);
    }
  }

  void reset() => state = const ChargeFlowState.idle();

  void _advance(ChargeStage stage) {
    if (ref.mounted) state = ChargeFlowState.running(stage);
  }
}
