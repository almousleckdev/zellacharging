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

  String get costText => costMinor == null || currency == null
      ? '--'
      : MoneyFormat.format(costMinor!, currency!, currencyDecimals ?? 0);

  String get stateLabel => Strings.sessionStates[state.name] ?? state.name;
}
