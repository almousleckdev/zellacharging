import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/pay/flow_panel.dart';
import '../../../../shared/widgets/pay/payment_step.dart';
import '../charge_flow_state.dart';

class ChargeFlowView extends StatelessWidget {
  const ChargeFlowView({
    super.key,
    required this.flow,
    required this.onRetry,
    required this.onClose,
  });

  final ChargeFlowState flow;
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
    return FlowPanel(
      kind: FlowKind.progress,
      title: PayFlowText.startingTitle,
      message: PayFlowText.startingBody,
      steps: [PaymentStep(PayFlowText.stepStart, StepStatus.active)],
    );
  }
}
