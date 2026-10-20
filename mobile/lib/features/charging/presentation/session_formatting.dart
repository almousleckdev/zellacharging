import '../../../core/format/duration_format.dart';
import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../domain/charging_session.dart';

extension SessionFormatting on ChargingSession {
  String get energyText => shownEnergyKwh == null
      ? '--'
      : '${shownEnergyKwh!.toStringAsFixed(2)} ${Strings.kwhUnit}';

  String get powerText => metering?.powerKw == null
      ? '--'
      : '${metering!.powerKw!.toStringAsFixed(1)} ${Strings.powerUnit}';

  String get batteryText => metering?.stateOfChargePercent == null
      ? '--'
      : '${metering!.stateOfChargePercent!.toStringAsFixed(0)} %';

  String get durationText => metering?.durationSeconds == null
      ? '--'
      : DurationFormat.compact(metering!.durationSeconds!);

  String get voltageText => metering?.voltage == null
      ? '--'
      : '${metering!.voltage!.toStringAsFixed(0)} ${Strings.voltageUnit}';

  String get currentText => metering?.currentAmpere == null
      ? '--'
      : '${metering!.currentAmpere!.toStringAsFixed(1)} ${Strings.currentUnit}';

  String get costText => costMinor == null || currency == null
      ? '--'
      : MoneyFormat.format(costMinor!, currency!, currencyDecimals ?? 0);

  String get priceText {
    final estimate = estimatedCost;
    if (costMinor != null || estimate == null) return costText;
    return MoneyFormat.format(
      estimate.minor,
      estimate.currency,
      estimate.decimals,
    );
  }

  bool get priceIsEstimate => costMinor == null && estimatedCost != null;

  double get chargeLevel {
    final percent = metering?.stateOfChargePercent;
    if (percent != null) return (percent / 100).clamp(0.0, 1.0);
    return state.isLive ? 0 : 1;
  }

  String get stateLabel => Strings.sessionStates[state.name] ?? state.name;
}
