import 'package:flutter/material.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/pay/summary_card.dart';
import '../../../../shared/widgets/pay/summary_line.dart';
import '../../domain/charge_plan.dart';

class ChargeSummary extends StatelessWidget {
  const ChargeSummary({super.key, required this.plan});

  final ChargePlan plan;

  String _money(int minor) =>
      MoneyFormat.format(minor, plan.currency, plan.decimals);

  @override
  Widget build(BuildContext context) {
    return SummaryCard(
      title: PayText.summaryTitle,
      lines: [
        SummaryLine(PayText.budget, _money(plan.amountMinor)),
        SummaryLine(PayText.fromWallet, _money(plan.fromWalletMinor)),
        if (plan.needsPayment)
          SummaryLine(PayText.byMobileMoney, _money(plan.payNowMinor)),
        SummaryLine(PayText.toPayNow, _money(plan.payNowMinor), emphasis: true),
      ],
      footnote: [
        if (plan.hasFees) PayText.feesIncluded,
        PayText.unusedReturned,
      ].join(' '),
    );
  }
}
