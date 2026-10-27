import 'package:flutter/material.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_icons.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/validation/validators.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../../../../shared/widgets/pay/operator_tiles.dart';
import '../../../../shared/widgets/pay/pay_card.dart';
import '../../../wallet/domain/mobile_operator.dart';
import '../../domain/charge_plan.dart';

class PaymentMethodCard extends StatelessWidget {
  const PaymentMethodCard({
    super.key,
    required this.plan,
    required this.operator,
    required this.phone,
    required this.onOperator,
  });

  final ChargePlan plan;
  final MobileOperator operator;
  final TextEditingController phone;
  final ValueChanged<MobileOperator> onOperator;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final covered = !plan.needsPayment;
    return PayCard(
      title: PayText.methodTitle,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Icon(
                AppIcons.walletFill,
                color: covered ? AppColors.success : AppColors.primary,
              ),
              const SizedBox(width: AppSpacing.sm),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      covered
                          ? PayText.walletCovers
                          : PayText.shortfallExplained,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.xs),
                    Text(
                      '${PayText.walletAvailable} · ${MoneyFormat.format(plan.availableMinor, plan.currency, plan.decimals)}',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          if (!covered) ...[
            const SizedBox(height: AppSpacing.md),
            OperatorTiles(selected: operator, onSelected: onOperator),
            const SizedBox(height: AppSpacing.md),
            AppTextField(
              controller: phone,
              label: WalletText.mobileMoneyNumberLabel,
              hint: WalletText.mobileMoneyNumberHint,
              icon: AppIcons.phone,
              validator: Validators.mobileMoneyNumber,
              keyboardType: TextInputType.phone,
              textInputAction: TextInputAction.done,
            ),
          ],
        ],
      ),
    );
  }
}
