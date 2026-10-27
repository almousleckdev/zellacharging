import '../../../core/network/api_error.dart';
import '../domain/payment_stage.dart';

enum TopupPhase { idle, running, done, failed }

class TopupFlowState {
  const TopupFlowState._(
    this.phase,
    this.stage,
    this.error,
    this.creditedMinor,
  );

  const TopupFlowState.idle() : this._(TopupPhase.idle, null, null, 0);

  const TopupFlowState.running(PaymentStage stage)
    : this._(TopupPhase.running, stage, null, 0);

  const TopupFlowState.done(int creditedMinor)
    : this._(TopupPhase.done, null, null, creditedMinor);

  const TopupFlowState.failed(ApiError error)
    : this._(TopupPhase.failed, null, error, 0);

  final TopupPhase phase;
  final PaymentStage? stage;
  final ApiError? error;
  final int creditedMinor;

  bool get running => phase == TopupPhase.running;
}
