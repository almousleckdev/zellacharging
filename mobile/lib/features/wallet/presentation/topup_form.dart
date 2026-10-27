import 'package:flutter/material.dart';

import '../../../core/format/amount_input.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/validation/validators.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/pay/amount_hero.dart';
import '../../../shared/widgets/pay/operator_tiles.dart';
import '../../../shared/widgets/pay/pay_card.dart';
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
    return Form(
      key: formKey,
      child: ListView(
        padding: const EdgeInsets.all(AppSpacing.lg),
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
          AmountHero(
            title: PayText.amountTitle,
            controller: amount,
            currency: currency,
            quickAmounts: WalletText.topUpAmounts,
            validator: Validators.topupAmount,
            onChanged: onAmountChanged,
          ),
          const SizedBox(height: AppSpacing.lg),
          PayCard(
            title: PayText.methodTitle,
            child: Column(
              children: [
                ListenableBuilder(
                  listenable: phone,
                  builder: (context, _) => OperatorTiles(
                    selected: operator,
                    phone: phone.text,
                    onSelected: onOperator,
                  ),
                ),
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
            ),
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
      ),
    );
  }
}
