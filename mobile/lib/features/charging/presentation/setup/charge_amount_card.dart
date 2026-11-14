import 'package:flutter/material.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/pay/amount_card.dart';
import '../../domain/charge_plan.dart';
import '../../domain/charge_readiness.dart';

class ChargeAmountCard extends StatelessWidget {
  const ChargeAmountCard({
    super.key,
    required this.controller,
    required this.plan,
    required this.readiness,
    required this.onChanged,
  });

  static const quickAmounts = [1000, 2000, 3000, 5000];

  final TextEditingController controller;
  final ChargePlan plan;
  final ChargeReadiness readiness;
  final VoidCallback onChanged;

  String _money(int minor) =>
      MoneyFormat.format(minor, plan.currency, plan.decimals);

  String? get _error {
    if (controller.text.isEmpty || !readiness.isAmountProblem) return null;
    return switch (readiness) {
      ChargeReadiness.amountTooLow => SetupText.tooLow(_money(plan.floorMinor)),
      ChargeReadiness.amountTooHigh => SetupText.tooHigh(
        _money(plan.ceilingMinor),
      ),
      _ => SetupText.exceedsBalance(_money(plan.availableMinor)),
    };
  }

  @override
  Widget build(BuildContext context) {
    return AmountCard(
      label: SetupText.amountTitle,
      hint: SetupText.amountHint,
      controller: controller,
      currency: plan.currency,
      helper: PayText.range(_money(plan.floorMinor), _money(plan.ceilingMinor)),
      errorText: _error,
      quickAmounts: quickAmounts,
      onChanged: onChanged,
    );
  }
}
