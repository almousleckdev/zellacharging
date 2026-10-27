import 'package:flutter/material.dart';

import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../shared/widgets/pay/flow_panel.dart';
import '../../../shared/widgets/pay/payment_step.dart';
import '../domain/mobile_operator.dart';
import '../domain/payment_stage.dart';
import 'topup_flow_state.dart';

class TopupFlowView extends StatelessWidget {
  const TopupFlowView({
    super.key,
    required this.flow,
    required this.currency,
    required this.operator,
    required this.phone,
    required this.onRetry,
    required this.onClose,
  });

  final TopupFlowState flow;
  final String currency;
  final MobileOperator operator;
  final String phone;
  final VoidCallback onRetry;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context) {
    final error = flow.error;
    if (flow.phase == TopupPhase.done) {
      return FlowPanel(
        kind: FlowKind.success,
        title: PayFlowText.topupDoneTitle,
        message: PayFlowText.topupDoneBody(
          MoneyFormat.format(flow.creditedMinor, currency, 0),
        ),
        primaryLabel: PayFlowText.done,
        onPrimary: onClose,
      );
    }
    if (error != null) {
      return FlowPanel(
        kind: error.isUncertain ? FlowKind.verifying : FlowKind.failed,
        title: error.isUncertain
            ? PayFlowText.verifyingTitle
            : PayFlowText.failedTitle,
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
      title: PayFlowText.approveTitle,
      message: PayFlowText.approveBody(operator.label, phone),
      steps: _steps(flow.stage ?? PaymentStage.requesting),
    );
  }

  List<PaymentStep> _steps(PaymentStage stage) {
    final current = switch (stage) {
      PaymentStage.requesting => 0,
      PaymentStage.awaitingApproval => 1,
      PaymentStage.confirmed => 2,
    };
    StepStatus status(int index) => index < current
        ? StepStatus.done
        : index == current
        ? StepStatus.active
        : StepStatus.pending;
    return [
      PaymentStep(PayFlowText.stepRequest, status(0)),
      PaymentStep(PayFlowText.stepApprove, status(1)),
      PaymentStep(PayFlowText.stepConfirm, status(2)),
    ];
  }
}
