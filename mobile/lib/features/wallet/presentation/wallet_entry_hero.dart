import 'package:flutter/material.dart';

import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/icon_badge.dart';
import '../../../shared/widgets/operator_logo.dart';
import '../../../shared/widgets/soft_card.dart';
import '../../../shared/widgets/status_chip.dart';
import '../domain/mobile_operator.dart';
import '../domain/wallet_entry_detail.dart';

class WalletEntryHero extends StatelessWidget {
  const WalletEntryHero({
    super.key,
    required this.detail,
    required this.currency,
    required this.decimals,
  });

  final WalletEntryDetail detail;
  final String currency;
  final int decimals;

  IconData get _icon => switch (detail.entry.type) {
    'CAPTURE' => AppIcons.lightningLine,
    'WITHDRAWAL' => AppIcons.signOut,
    _ => AppIcons.wallet,
  };

  Widget _mark(Color tone) {
    final operator = MobileOperator.fromCode(detail.operator);
    if (operator != null) return OperatorLogo(operator: operator, size: 56);
    return IconBadge(icon: _icon, size: 56, color: tone);
  }

  Color get _statusColor => detail.failed
      ? AppColors.danger
      : detail.inProgress || detail.needsReview
      ? AppColors.primary
      : AppColors.success;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final entry = detail.entry;
    final tone = entry.credit ? AppColors.success : AppColors.textPrimary;
    return SoftCard(
      padding: const EdgeInsets.all(AppSpacing.lg),
      child: Column(
        children: [
          _mark(tone),
          const SizedBox(height: AppSpacing.md),
          Text(
            WithdrawText.entryLabel(entry.type),
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          FittedBox(
            fit: BoxFit.scaleDown,
            child: Text(
              '${entry.credit ? '+' : '−'}${MoneyFormat.format(entry.amountMinor, currency, decimals)}',
              style: theme.textTheme.headlineMedium?.copyWith(
                fontWeight: FontWeight.w800,
                color: tone,
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          StatusChip(
            label: WalletHistoryText.statusLabel(detail.state),
            color: _statusColor,
          ),
        ],
      ),
    );
  }
}
