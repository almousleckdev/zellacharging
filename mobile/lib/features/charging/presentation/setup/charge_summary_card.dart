import 'package:flutter/material.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/pay/summary_card.dart';
import '../../../../shared/widgets/pay/summary_line.dart';
import '../../domain/charge_plan.dart';

class ChargeSummaryCard extends StatelessWidget {
  const ChargeSummaryCard({super.key, required this.plan});

  final ChargePlan plan;

  @override
  Widget build(BuildContext context) {
    final energy = plan.energyKwh;
    return SummaryCard(
      title: SetupText.summaryTitle,
      lines: [
        SummaryLine(
          SetupText.summaryAmount,
          MoneyFormat.format(plan.amountMinor, plan.currency, plan.decimals),
          emphasis: true,
        ),
        if (energy != null)
          SummaryLine(
            SetupText.summaryEnergy,
            '≈ ${energy.toStringAsFixed(1)} ${Strings.energyUnit}',
          ),
      ],
      footnote: PayText.budgetHelper,
    );
  }
}
