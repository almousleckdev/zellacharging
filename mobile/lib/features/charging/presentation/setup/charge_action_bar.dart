import 'package:flutter/widgets.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/pay/action_bar.dart';
import '../../domain/charge_plan.dart';
import '../../domain/charge_readiness.dart';

class ChargeActionBar extends StatelessWidget {
  const ChargeActionBar({
    super.key,
    required this.plan,
    required this.readiness,
    required this.onPressed,
  });

  final ChargePlan plan;
  final ChargeReadiness readiness;
  final VoidCallback onPressed;

  String get _label {
    if (readiness == ChargeReadiness.walletUnknown) {
      return PayText.refreshWallet;
    }
    if (readiness == ChargeReadiness.needsTopup) return PayText.rechargeAccount;
    if (plan.freeCharging || !readiness.canStart) return PayText.startCharging;
    return SetupText.startFor(
      MoneyFormat.format(plan.amountMinor, plan.currency, plan.decimals),
    );
  }

  String? get _hint => switch (readiness) {
    ChargeReadiness.chooseConnector => PayText.selectConnectorFirst,
    ChargeReadiness.plugIn => SetupText.plugInFirst,
    ChargeReadiness.enterAmount => SetupText.enterAmountFirst,
    _ => null,
  };

  bool get _enabled =>
      readiness.canStart ||
      readiness == ChargeReadiness.needsTopup ||
      readiness == ChargeReadiness.walletUnknown;

  @override
  Widget build(BuildContext context) {
    return ActionBar(
      label: _label,
      hint: _hint,
      onPressed: _enabled ? onPressed : null,
    );
  }
}
