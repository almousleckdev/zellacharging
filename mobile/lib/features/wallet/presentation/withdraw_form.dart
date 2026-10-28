import '../../../shared/widgets/dismissible_form.dart';
import 'package:flutter/material.dart';

import '../../../core/format/amount_input.dart';
import '../../../core/format/money_format.dart';
import '../../../core/format/phone_mask.dart';
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
import '../../../shared/widgets/pay/summary_card.dart';
import '../../../shared/widgets/pay/summary_line.dart';
import '../../../shared/widgets/pay/wallet_balance_card.dart';
import '../domain/mobile_operator.dart';
import '../domain/wallet.dart';

class WithdrawForm extends StatelessWidget {
  const WithdrawForm({
    super.key,
    required this.formKey,
    required this.wallet,
    required this.amount,
    required this.password,
    required this.phone,
    required this.operator,
    required this.onOperator,
    required this.onAmountChanged,
  });

  final GlobalKey<FormState> formKey;
  final Wallet wallet;
  final TextEditingController amount;
  final TextEditingController password;
  final String phone;
  final MobileOperator operator;
  final ValueChanged<MobileOperator> onOperator;
  final VoidCallback onAmountChanged;

  List<int> get _quick => {
    for (final value in [1000, 5000, 10000])
      if (value <= wallet.availableMinor) value,
    if (wallet.availableMinor >= WalletText.minimumWithdrawMinor)
      wallet.availableMinor,
  }.toList();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final value = AmountInput.parse(amount.text) ?? 0;
    final shown = MoneyFormat.format(value, wallet.currency, wallet.decimals);
    return DismissibleForm(
      formKey: formKey,
      children: [
        WalletBalanceCard(wallet: wallet),
        const SizedBox(height: AppSpacing.md),
        Text(
          WithdrawText.explained,
          style: theme.textTheme.bodySmall?.copyWith(
            color: AppColors.textSecondary,
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        AmountHero(
          title: WithdrawText.amountTitle,
          controller: amount,
          currency: wallet.currency,
          quickAmounts: _quick,
          validator: (text) =>
              Validators.withdrawAmount(text, wallet.availableMinor),
          onChanged: onAmountChanged,
        ),
        const SizedBox(height: AppSpacing.lg),
        PayCard(
          title: WithdrawText.destinationTitle,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              OperatorTiles(
                selected: operator,
                phone: phone,
                onSelected: onOperator,
              ),
              const SizedBox(height: AppSpacing.md),
              Row(
                children: [
                  const Icon(AppIcons.phone, size: 18),
                  const SizedBox(width: AppSpacing.sm),
                  Text(
                    PhoneMask.of(phone),
                    style: theme.textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.xs),
              Text(
                WithdrawText.destinationNote,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        PayCard(
          child: AppTextField(
            controller: password,
            label: WithdrawText.passwordLabel,
            hint: WithdrawText.passwordHint,
            icon: AppIcons.lock,
            obscure: true,
            validator: Validators.password,
            textInputAction: TextInputAction.done,
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        SummaryCard(
          title: PayText.summaryTitle,
          lines: [
            SummaryLine(WithdrawText.fromWallet, shown),
            SummaryLine(WithdrawText.toReceive, shown, emphasis: true),
          ],
        ),
        const SizedBox(height: AppSpacing.lg),
        const SecureNote(),
      ],
    );
  }
}
