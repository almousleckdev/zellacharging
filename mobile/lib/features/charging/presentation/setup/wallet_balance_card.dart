import 'package:flutter/material.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_icons.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/pay/pay_card.dart';
import '../../domain/charge_plan.dart';

class WalletBalanceCard extends StatelessWidget {
  const WalletBalanceCard({super.key, required this.plan});

  final ChargePlan plan;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final free = plan.freeCharging;
    final low = plan.needsTopup;
    final color = !plan.walletKnown
        ? AppColors.textSecondary
        : low
        ? AppColors.danger
        : AppColors.success;
    final minimum = MoneyFormat.format(
      plan.minimumMinor ?? ChargePlan.minimumChargeMinor,
      plan.currency,
      plan.decimals,
    );
    return PayCard(
      title: PayText.walletTitle,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(AppIcons.walletFill, color: color),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  !plan.walletKnown
                      ? PayText.walletUnknown
                      : free
                      ? PayText.freeCharging
                      : MoneyFormat.format(
                          plan.availableMinor,
                          plan.currency,
                          plan.decimals,
                        ),
                  style: theme.textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  !plan.walletKnown
                      ? PayText.walletUnknownNote
                      : free
                      ? PayText.freeChargingNote
                      : low
                      ? PayText.rechargeNeeded(minimum)
                      : PayText.balanceUsed,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: low ? AppColors.danger : AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
