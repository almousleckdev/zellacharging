import 'package:flutter/material.dart';

import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../shared/widgets/pay/flow_panel.dart';
import '../../../shared/widgets/pay/payment_step.dart';
import '../domain/mobile_operator.dart';
import '../domain/withdraw_stage.dart';
import 'withdraw_flow_state.dart';

class WithdrawFlowView extends StatelessWidget {
  const WithdrawFlowView({
    super.key,
    required this.flow,
    required this.currency,
    required this.operator,
    required this.phone,
    required this.onRetry,
    required this.onClose,
  });

  final WithdrawFlowState flow;
  final String currency;
  final MobileOperator operator;
  final String phone;
  final VoidCallback onRetry;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context) {
    final error = flow.error;
    if (flow.phase == WithdrawPhase.done) {
      final amount = MoneyFormat.format(flow.amountMinor, currency, 0);
      return FlowPanel(
        kind: FlowKind.success,
        title: flow.awaitingApproval
            ? WithdrawText.pendingTitle
            : WithdrawText.doneTitle,
        message: flow.awaitingApproval
            ? WithdrawText.pendingBody(amount)
            : WithdrawText.doneBody(amount),
        primaryLabel: PayFlowText.done,
        onPrimary: onClose,
      );
    }
    if (error != null) {
      return FlowPanel(
        kind: error.isUncertain ? FlowKind.verifying : FlowKind.failed,
        title: error.isUncertain
            ? WithdrawText.verifyingTitle
            : WithdrawText.failedTitle,
        message: error.message,
        primaryLabel: error.isUncertain
            ? PayFlowText.backToWallet
            : PayFlowText.retry,
        onPrimary: error.isUncertain ? onClose : onRetry,
        secondaryLabel: error.isUncertain ? null : PayFlowText.backToWallet,
        onSecondary: onClose,
      );
    }
    return FlowPanel(
      kind: FlowKind.progress,
      title: WithdrawText.progressTitle,
      message: WithdrawText.progressBody(operator.label, phone),
      steps: _steps(flow.stage ?? WithdrawStage.requesting),
    );
  }

  List<PaymentStep> _steps(WithdrawStage stage) {
    final current = switch (stage) {
      WithdrawStage.requesting => 0,
      WithdrawStage.processing => 1,
      WithdrawStage.completed => 2,
    };
    StepStatus status(int index) => index < current
        ? StepStatus.done
        : index == current
        ? StepStatus.active
        : StepStatus.pending;
    return [
      PaymentStep(WithdrawText.stepRequest, status(0)),
      PaymentStep(WithdrawText.stepProcess, status(1)),
      PaymentStep(WithdrawText.stepDone, status(2)),
    ];
  }
}
