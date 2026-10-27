import 'package:flutter/widgets.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/pay/action_bar.dart';
import '../../domain/charge_plan.dart';

class ChargeActionBar extends StatelessWidget {
  const ChargeActionBar({
    super.key,
    required this.plan,
    required this.connectorChosen,
    required this.onPressed,
  });

  final ChargePlan plan;
  final bool connectorChosen;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return ActionBar(
      label: plan.needsPayment
          ? PayText.payAndStart(
              MoneyFormat.format(
                plan.payNowMinor,
                plan.currency,
                plan.decimals,
              ),
            )
          : PayText.startCharging,
      hint: connectorChosen ? null : PayText.selectConnectorFirst,
      onPressed: onPressed,
    );
  }
}
