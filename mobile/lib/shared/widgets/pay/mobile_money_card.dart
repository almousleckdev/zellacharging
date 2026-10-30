import 'package:flutter/material.dart';

import '../../../core/format/phone_mask.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../features/wallet/domain/mobile_operator.dart';
import '../../../features/wallet/domain/payout_number.dart';
import '../app_text_field.dart';
import '../operator_logo.dart';
import 'operator_tiles.dart';
import 'pay_card.dart';

class MobileMoneyCard extends StatelessWidget {
  const MobileMoneyCard({
    super.key,
    required this.title,
    required this.phone,
    required this.operator,
    required this.onOperator,
    required this.validator,
    this.known = const [],
    this.note,
  });

  final String title;
  final TextEditingController phone;
  final MobileOperator operator;
  final ValueChanged<MobileOperator> onOperator;
  final String? Function(String?) validator;
  final List<PayoutNumber> known;
  final String? note;

  @override
  Widget build(BuildContext context) {
    return PayCard(
      title: title,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          AppTextField(
            controller: phone,
            label: WalletText.mobileMoneyNumberLabel,
            hint: WalletText.mobileMoneyNumberHint,
            icon: AppIcons.phone,
            validator: validator,
            keyboardType: TextInputType.phone,
            textInputAction: TextInputAction.done,
          ),
          if (known.isNotEmpty) ...[
            const SizedBox(height: AppSpacing.sm),
            Wrap(
              spacing: AppSpacing.sm,
              runSpacing: AppSpacing.sm,
              children: [
                for (final number in known)
                  _KnownNumber(
                    number: number,
                    onTap: () {
                      phone.text = number.msisdn;
                      onOperator(number.operator);
                    },
                  ),
              ],
            ),
          ],
          const SizedBox(height: AppSpacing.md),
          ListenableBuilder(
            listenable: phone,
            builder: (context, _) => OperatorTiles(
              selected: operator,
              phone: phone.text,
              onSelected: onOperator,
            ),
          ),
          if (note != null) ...[
            const SizedBox(height: AppSpacing.sm),
            Text(
              note!,
              style: Theme.of(
                context,
              ).textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
            ),
          ],
        ],
      ),
    );
  }
}

class _KnownNumber extends StatelessWidget {
  const _KnownNumber({required this.number, required this.onTap});

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
