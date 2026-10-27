import 'package:flutter/material.dart';

import '../../../core/format/date_format.dart';
import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/icon_badge.dart';
import '../../../shared/widgets/soft_card.dart';
import '../domain/wallet_entry.dart';

class WalletEntryTile extends StatelessWidget {
  const WalletEntryTile({
    super.key,
    required this.entry,
    required this.currency,
    required this.decimals,
    required this.onTap,
  });

  final WalletEntry entry;
  final String currency;
  final int decimals;
  final VoidCallback onTap;

  IconData get _icon => switch (entry.type) {
    'CAPTURE' => AppIcons.lightningLine,
    'WITHDRAWAL' => AppIcons.signOut,
    _ => AppIcons.wallet,
  };

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final tone = entry.credit ? AppColors.success : AppColors.textPrimary;
    return SoftCard(
      onTap: onTap,
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.sm + 2,
      ),
      child: Row(
        children: [
          IconBadge(icon: _icon, size: 40, color: tone),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  WithdrawText.entryLabel(entry.type),
                  style: theme.textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                Text(
                  DateFormatter.time(entry.createdAt),
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
              fontWeight: FontWeight.w800,
              color: tone,
            ),
          ),
          const SizedBox(width: AppSpacing.xs),
          const Icon(AppIcons.caretRight, size: 16, color: AppColors.neutral),
        ],
      ),
    );
  }
}
