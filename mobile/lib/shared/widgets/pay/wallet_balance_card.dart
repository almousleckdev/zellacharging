import 'package:flutter/material.dart';

import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../features/wallet/domain/wallet.dart';
import '../gradient_card.dart';

class WalletBalanceCard extends StatelessWidget {
  const WalletBalanceCard({super.key, required this.wallet});

  final Wallet wallet;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final soft = AppColors.onPrimary.withValues(alpha: 0.85);
    return GradientCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(AppIcons.walletFill, color: soft, size: 20),
              const SizedBox(width: AppSpacing.sm),
              Expanded(
                child: Text(
                  PayText.walletTitle,
                  style: theme.textTheme.bodyMedium?.copyWith(color: soft),
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.sm),
          FittedBox(
            fit: BoxFit.scaleDown,
            alignment: Alignment.centerLeft,
            child: Text(
              MoneyFormat.format(
                wallet.balanceMinor,
                wallet.currency,
                wallet.decimals,
              ),
              style: theme.textTheme.headlineMedium?.copyWith(
                color: AppColors.onPrimary,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          Row(
            children: [
              Expanded(
                child: _stat(
                  context,
                  PayText.walletAvailable,
                  wallet.availableMinor,
                ),
              ),
              Expanded(
                child: _stat(
                  context,
                  PayText.walletReserved,
                  wallet.reservedMinor,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _stat(BuildContext context, String label, int minor) {
    final theme = Theme.of(context);
    final soft = AppColors.onPrimary.withValues(alpha: 0.85);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: theme.textTheme.bodySmall?.copyWith(color: soft)),
        Text(
          MoneyFormat.format(minor, wallet.currency, wallet.decimals),
          style: theme.textTheme.titleSmall?.copyWith(
            color: AppColors.onPrimary,
            fontWeight: FontWeight.w700,
          ),
        ),
      ],
    );
  }
}
