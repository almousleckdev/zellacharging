import '../../../core/network/api_error.dart';
import '../domain/withdraw_stage.dart';

enum WithdrawPhase { idle, running, done, failed }

class WithdrawFlowState {
  const WithdrawFlowState._(
    this.phase,
    this.stage,
    this.error,
    this.amountMinor,
  );

  const WithdrawFlowState.idle() : this._(WithdrawPhase.idle, null, null, 0);

  const WithdrawFlowState.running(WithdrawStage stage)
    : this._(WithdrawPhase.running, stage, null, 0);

  const WithdrawFlowState.done(int amountMinor)
    : this._(WithdrawPhase.done, null, null, amountMinor);

  const WithdrawFlowState.failed(ApiError error)
    : this._(WithdrawPhase.failed, null, error, 0);

  final WithdrawPhase phase;
  final WithdrawStage? stage;
  final ApiError? error;
  final int amountMinor;
}
