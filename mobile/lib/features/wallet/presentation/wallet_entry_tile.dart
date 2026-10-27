import 'package:flutter/material.dart';

import '../../../core/format/date_format.dart';
import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../domain/wallet_entry.dart';

class WalletEntryTile extends StatelessWidget {
  const WalletEntryTile({
    super.key,
    required this.entry,
    required this.currency,
    required this.decimals,
  });

  final WalletEntry entry;
  final String currency;
  final int decimals;

  IconData get _icon => switch (entry.type) {
    'CAPTURE' => AppIcons.lightningLine,
    'WITHDRAWAL' => AppIcons.signOut,
    _ => AppIcons.wallet,
  };

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final tone = entry.credit ? AppColors.success : AppColors.textPrimary;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.sm),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: tone.withValues(alpha: 0.1),
            ),
            child: Icon(_icon, color: tone, size: 20),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  WithdrawText.entryLabel(entry.type),
                  style: theme.textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  DateFormatter.dateTime(entry.createdAt),
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Text(
            '${entry.credit ? '+' : '−'}${MoneyFormat.format(entry.amountMinor, currency, decimals)}',
            style: theme.textTheme.bodyMedium?.copyWith(
              fontWeight: FontWeight.w700,
              color: tone,
            ),
          ),
        ],
      ),
    );
  }
}
