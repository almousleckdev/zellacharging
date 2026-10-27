import '../../../../core/format/amount_input.dart';
import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../core/validation/validators.dart';
import '../../domain/charge_plan.dart';

abstract final class AmountRules {
  static String? validate(String? value, ChargePlan plan) {
    final base = Validators.chargeAmount(value);
    final maximum = plan.maximumMinor;
    final amount = AmountInput.parse(value) ?? 0;
    if (base != null || maximum == null || amount <= maximum) return base;
    return PayText.range(
      MoneyFormat.format(plan.minimumMinor ?? 0, plan.currency, plan.decimals),
      MoneyFormat.format(maximum, plan.currency, plan.decimals),
    );
  }

  static String helper(ChargePlan plan) {
    final energy = plan.energyKwh;
    return energy == null
        ? PayText.budgetHelper
        : '${PayText.estimatedEnergy(energy.toStringAsFixed(1))}\n${PayText.budgetHelper}';
  }
}
