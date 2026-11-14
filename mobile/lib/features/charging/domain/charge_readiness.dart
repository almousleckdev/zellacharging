import '../../stations/domain/station_connector.dart';
import 'charge_plan.dart';

enum ChargeReadiness {
  ready,
  needsTopup,
  chooseConnector,
  plugIn,
  enterAmount,
  amountTooLow,
  amountTooHigh,
  exceedsBalance;

  static ChargeReadiness of({
    required ChargePlan plan,
    required StationConnector? connector,
  }) {
    if (plan.balanceTooLow) return needsTopup;
    if (connector == null) return chooseConnector;
    if (!connector.pluggedIn) return plugIn;
    if (plan.amountMinor <= 0) return enterAmount;
    if (plan.belowMinimum) return amountTooLow;
    if (plan.aboveMaximum) return amountTooHigh;
    if (plan.exceedsBalance) return exceedsBalance;
    return ready;
  }

  bool get canStart => this == ready;

  bool get isAmountProblem =>
      this == amountTooLow || this == amountTooHigh || this == exceedsBalance;
}
