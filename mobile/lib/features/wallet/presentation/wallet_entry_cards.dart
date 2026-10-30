import 'package:flutter/material.dart';

import '../../../core/format/date_format.dart';
import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../shared/widgets/info_card.dart';
import '../../../shared/widgets/info_row.dart';
import '../domain/mobile_operator.dart';
import '../domain/wallet_entry_detail.dart';

class WalletEntryCards extends StatelessWidget {
  const WalletEntryCards({
    super.key,
    required this.detail,
    required this.currency,
    required this.decimals,
  });

  final WalletEntryDetail detail;
  final String currency;
  final int decimals;

  @override
  Widget build(BuildContext context) {
    final entry = detail.entry;
    return Column(
      children: [
        InfoCard(
          title: WalletHistoryText.operationTitle,
          children: [
            InfoRow(
              label: WalletHistoryText.type,
              value: WithdrawText.entryLabel(entry.type),
            ),
            InfoRow(
              label: WalletHistoryText.date,
              value: DateFormatter.date(entry.createdAt),
            ),
            InfoRow(
              label: WalletHistoryText.time,
              value: DateFormatter.time(entry.createdAt),
            ),
            InfoRow(
              label: WalletHistoryText.status,
              value: WalletHistoryText.statusLabel(detail.state),
            ),
            if (detail.reference != null)
              InfoRow(
                label: WalletHistoryText.reference,
                value: detail.reference!,
              ),
          ],
        ),
        if (detail.hasMobileMoney) ...[
          const SizedBox(height: 12),
          InfoCard(
            title: WalletHistoryText.mobileMoneyTitle,
            children: [
              InfoRow(
                label: WalletHistoryText.operator,
                value:
                    MobileOperator.fromCode(detail.operator)?.label ??
                    detail.operator!,
              ),
              if (detail.msisdn != null)
                InfoRow(label: WalletHistoryText.number, value: detail.msisdn!),
              if (detail.providerReference != null)
                InfoRow(
                  label: WalletHistoryText.providerReference,
                  value: detail.providerReference!,
                ),
            ],
          ),
        ],
        const SizedBox(height: 12),
        InfoCard(
          title: WalletHistoryText.walletTitle,
          children: [
            InfoRow(
              label: WalletHistoryText.amount,
              value: MoneyFormat.format(entry.amountMinor, currency, decimals),
            ),
            InfoRow(
              label: WalletHistoryText.balanceAfter,
              value: MoneyFormat.format(
                detail.balanceAfterMinor,
                currency,
                decimals,
              ),
              emphasized: true,
            ),
          ],
        ),
      ],
    );
  }
}
