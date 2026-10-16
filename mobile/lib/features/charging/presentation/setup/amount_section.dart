import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/validation/validators.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../../../wallet/presentation/amount_picker.dart';

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
          amounts: Strings.chargeAmounts,
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
          label: '${Strings.amountLabel} ($currency)',
          hint: Strings.amountHint,
          icon: Icons.payments_outlined,
          validator: Validators.chargeAmount,
          keyboardType: TextInputType.number,
          textInputAction: TextInputAction.done,
        ),
      ],
    );
  }
}
