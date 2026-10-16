import 'package:flutter/material.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/validation/validators.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../../../wallet/domain/mobile_operator.dart';
import '../../../wallet/presentation/operator_selector.dart';
import '../../domain/charge_quote.dart';

class PaymentSection extends StatelessWidget {
  const PaymentSection({
    super.key,
    required this.quote,
    required this.currency,
    required this.operator,
    required this.phone,
    required this.onOperator,
  });

  final ChargeQuote quote;
  final String currency;
  final MobileOperator operator;
  final TextEditingController phone;
  final ValueChanged<MobileOperator> onOperator;

  @override
  Widget build(BuildContext context) {
    final muted = Theme.of(
      context,
    ).textTheme.bodySmall?.copyWith(color: AppColors.textSecondary);
    if (!quote.needsPayment) {
      return Text(Strings.paidFromWallet, style: muted);
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '${Strings.toPayByMobileMoney}: '
          '${MoneyFormat.format(quote.shortfallMinor, currency, 0)}',
          style: const TextStyle(fontWeight: FontWeight.w700),
        ),
        const SizedBox(height: AppSpacing.md),
        OperatorSelector(selected: operator, onSelected: onOperator),
        const SizedBox(height: AppSpacing.md),
        AppTextField(
          controller: phone,
          label: Strings.mobileMoneyNumberLabel,
          hint: Strings.mobileMoneyNumberHint,
          icon: Icons.phone_outlined,
          validator: Validators.mobileMoneyNumber,
          keyboardType: TextInputType.phone,
        ),
        const SizedBox(height: AppSpacing.sm),
        Text(Strings.unusedReturned, style: muted),
      ],
    );
  }
}
