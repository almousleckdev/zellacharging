import 'package:flutter/material.dart';

import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../shared/widgets/pay/summary_card.dart';
import '../../../shared/widgets/pay/summary_line.dart';
import '../domain/mobile_operator.dart';

class TopupSummary extends StatelessWidget {
  const TopupSummary({
    super.key,
    required this.amountMinor,
    required this.currency,
    required this.operator,
  });

  final int amountMinor;
  final String currency;
  final MobileOperator operator;

  @override
  Widget build(BuildContext context) {
    final amount = MoneyFormat.format(amountMinor, currency, 0);
    return SummaryCard(
      title: PayText.summaryTitle,
      lines: [
        SummaryLine(PayText.byMobileMoney, operator.label),
        SummaryLine(PayText.creditedToWallet, amount),
        SummaryLine(PayText.toPayNow, amount, emphasis: true),
      ],
    );
  }
}
