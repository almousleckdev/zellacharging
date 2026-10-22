import 'package:flutter/material.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/primary_button.dart';
import '../../domain/charge_quote.dart';

class StartButton extends StatelessWidget {
  const StartButton({
    super.key,
    required this.quote,
    required this.currency,
    required this.loading,
    required this.onPressed,
  });

  final ChargeQuote quote;
  final String currency;
  final bool loading;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return PrimaryButton(
      label: quote.needsPayment
          ? '${ChargeText.startAndPay} · ${MoneyFormat.format(quote.amountMinor, currency, 0)}'
          : ChargeText.startOnly,
      loading: loading,
      onPressed: onPressed,
    );
  }
}
