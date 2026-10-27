import '../../../core/network/api_error.dart';
import '../domain/charge_stage.dart';

enum ChargePhase { idle, running, failed }

class ChargeFlowState {
  const ChargeFlowState._(this.phase, this.stage, this.error);

  const ChargeFlowState.idle() : this._(ChargePhase.idle, null, null);

  const ChargeFlowState.running(ChargeStage stage)
    : this._(ChargePhase.running, stage, null);

  const ChargeFlowState.failed(ApiError error)
    : this._(ChargePhase.failed, null, error);

  final ChargePhase phase;
  final ChargeStage? stage;
  final ApiError? error;

  bool get running => phase == ChargePhase.running;
}
