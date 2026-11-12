import 'package:flutter/widgets.dart';

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
    final low = plan.needsTopup;
    return ActionBar(
      label: low ? PayText.rechargeAccount : PayText.startCharging,
      hint: low || connectorChosen ? null : PayText.selectConnectorFirst,
      onPressed: onPressed,
    );
  }
}
