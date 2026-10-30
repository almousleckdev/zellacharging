import '../../../shared/widgets/dismissible_form.dart';
import 'package:flutter/material.dart';

import '../../../core/format/amount_input.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/widgets/pay/amount_card.dart';
import '../../../shared/widgets/pay/mobile_money_card.dart';
import '../../../shared/widgets/pay/secure_note.dart';
import '../../../shared/widgets/pay/wallet_balance_card.dart';
import '../domain/mobile_operator.dart';
import '../domain/wallet.dart';
import 'topup_summary.dart';

class TopupForm extends StatelessWidget {
  const TopupForm({
    super.key,
    required this.formKey,
    required this.wallet,
    required this.currency,
    required this.amount,
    required this.phone,
    required this.operator,
    required this.onOperator,
    required this.onAmountChanged,
  });

  final GlobalKey<FormState> formKey;
  final Wallet? wallet;
  final String currency;
  final TextEditingController amount;
  final TextEditingController phone;
  final MobileOperator operator;
  final ValueChanged<MobileOperator> onOperator;
  final VoidCallback onAmountChanged;

  @override
  Widget build(BuildContext context) {
    final current = wallet;
    return DismissibleForm(
      formKey: formKey,
      children: [
        if (current != null) WalletBalanceCard(wallet: current),
        const SizedBox(height: AppSpacing.md),
        Text(
          PayText.topupExplained,
          style: Theme.of(
            context,
          ).textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
        ),
        const SizedBox(height: AppSpacing.lg),
        AmountCard(
          label: WalletText.topupAmountLabel,
          hint: WalletText.topupAmountHint,
          controller: amount,
          currency: currency,
          validator: Validators.topupAmount,
          onChanged: onAmountChanged,
        ),
        const SizedBox(height: AppSpacing.lg),
        MobileMoneyCard(
          title: PayText.methodTitle,
          phone: phone,
          operator: operator,
          onOperator: onOperator,
          validator: Validators.mobileMoneyNumber,
        ),
        const SizedBox(height: AppSpacing.lg),
        TopupSummary(
          amountMinor: AmountInput.parse(amount.text) ?? 0,
          currency: currency,
          operator: operator,
        ),
        const SizedBox(height: AppSpacing.lg),
        const SecureNote(),
      ],
    );
  }
}
