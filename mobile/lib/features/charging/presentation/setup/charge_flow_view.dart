import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/pay/flow_panel.dart';
import '../../../../shared/widgets/pay/payment_step.dart';
import '../../../wallet/domain/mobile_operator.dart';
import '../../domain/charge_stage.dart';
import '../charge_flow_state.dart';

class ChargeFlowView extends StatelessWidget {
  const ChargeFlowView({
    super.key,
    required this.flow,
    required this.paying,
    required this.operator,
    required this.phone,
    required this.onRetry,
    required this.onClose,
  });

  final ChargeFlowState flow;
  final bool paying;
  final MobileOperator operator;
  final String phone;
  final VoidCallback onRetry;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context) {
    final error = flow.error;
    if (error != null) {
      return FlowPanel(
        kind: error.isUncertain ? FlowKind.verifying : FlowKind.failed,
        title: error.isUncertain
            ? PayFlowText.verifyingTitle
            : PayFlowText.failedTitle,
        message: error.message,
        primaryLabel: error.isUncertain ? PayFlowText.done : PayFlowText.retry,
        onPrimary: error.isUncertain ? onClose : onRetry,
        secondaryLabel: error.isUncertain ? null : PayFlowText.done,
        onSecondary: onClose,
      );
    }
    final stage = flow.stage ?? ChargeStage.startingCharge;
    final starting = stage == ChargeStage.startingCharge;
    return FlowPanel(
      kind: FlowKind.progress,
      title: starting ? PayFlowText.startingTitle : PayFlowText.approveTitle,
      message: starting
          ? PayFlowText.startingBody
          : PayFlowText.approveBody(operator.label, phone),
      steps: _steps(stage),
    );
  }

  List<PaymentStep> _steps(ChargeStage stage) {
    PaymentStep step(String label, int index, int current) => PaymentStep(
      label,
      index < current
          ? StepStatus.done
          : index == current
          ? StepStatus.active
          : StepStatus.pending,
    );
    final current = switch (stage) {
      ChargeStage.requestingPayment => 0,
      ChargeStage.awaitingApproval => 1,
      ChargeStage.startingCharge => paying ? 3 : 0,
    };
    return paying
        ? [
            step(PayFlowText.stepRequest, 0, current),
            step(PayFlowText.stepApprove, 1, current),
            step(PayFlowText.stepConfirm, 2, current),
            step(PayFlowText.stepStart, 3, current),
          ]
        : [step(PayFlowText.stepStart, 0, current)];
  }
}
