import 'package:flutter/material.dart';

import '../../../core/format/phone_mask.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/operator_logo.dart';
import '../../../shared/widgets/pay/pay_card.dart';
import '../domain/payout_number.dart';

class WithdrawDestination extends StatelessWidget {
  const WithdrawDestination({
    super.key,
    required this.phone,
    required this.numbers,
  });

  final TextEditingController phone;
  final List<PayoutNumber> numbers;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return PayCard(
      title: WithdrawText.destinationTitle,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          AppTextField(
            controller: phone,
            label: WithdrawText.numberLabel,
            hint: WalletText.mobileMoneyNumberHint,
            icon: AppIcons.phone,
            keyboardType: TextInputType.phone,
            validator: (value) => Validators.payoutNumber(
              value,
              numbers.map((number) => number.msisdn),
            ),
          ),
          if (numbers.isNotEmpty) ...[
            const SizedBox(height: AppSpacing.sm),
            Wrap(
              spacing: AppSpacing.sm,
              runSpacing: AppSpacing.sm,
              children: [
                for (final number in numbers)
                  _NumberChip(
                    number: number,
                    onTap: () => phone.text = number.msisdn,
                  ),
              ],
            ),
          ],
          const SizedBox(height: AppSpacing.sm),
          Text(
            numbers.isEmpty
                ? WithdrawText.noPayoutNumber
                : WithdrawText.destinationNote,
            style: theme.textTheme.bodySmall?.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }
}

class _NumberChip extends StatelessWidget {
  const _NumberChip({required this.number, required this.onTap});

  final PayoutNumber number;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(999),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.sm + 2,
          vertical: AppSpacing.xs + 2,
        ),
        decoration: BoxDecoration(
          color: AppColors.surfaceMuted,
          borderRadius: BorderRadius.circular(999),
          border: Border.all(color: AppColors.border),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            OperatorLogo(operator: number.operator, size: 18),
            const SizedBox(width: AppSpacing.xs + 2),
            Text(
              PhoneMask.of(number.msisdn),
              style: Theme.of(
                context,
              ).textTheme.bodySmall?.copyWith(fontWeight: FontWeight.w600),
            ),
          ],
        ),
      ),
    );
  }
}
