import 'package:flutter/material.dart';

import '../../../../core/format/money_format.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/validation/validators.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../../../wallet/presentation/amount_picker.dart';
import '../../../../core/theme/app_icons.dart';

class AmountSection extends StatelessWidget {
  const AmountSection({
    super.key,
    required this.controller,
    required this.currency,
    required this.onChanged,
  });

  final TextEditingController controller;
  final String currency;
  final VoidCallback onChanged;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        AmountPicker(
          amounts: ChargeText.chargeAmounts,
          selected: int.tryParse(controller.text) ?? 0,
          currency: currency,
          onSelected: (value) {
            controller.text = '$value';
            onChanged();
          },
        ),
        const SizedBox(height: 16),
        AppTextField(
          controller: controller,
          label: '${ChargeText.amountLabel} (${MoneyFormat.symbol(currency)})',
          hint: ChargeText.amountHint,
          icon: AppIcons.coins,
          validator: Validators.chargeAmount,
          keyboardType: TextInputType.number,
          textInputAction: TextInputAction.done,
        ),
      ],
    );
  }
}
