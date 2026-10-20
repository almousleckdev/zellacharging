import '../../../core/format/duration_format.dart';
import '../../../core/format/money_format.dart';
import 'package:flutter/painting.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../domain/charging_session.dart';
import '../domain/session_state.dart';

extension SessionFormatting on ChargingSession {
  String get energyText => shownEnergyKwh == null
      ? '--'
      : '${shownEnergyKwh!.toStringAsFixed(2)} ${Strings.kwhUnit}';

  String get powerText => metering?.powerKw == null
      ? '--'
      : '${metering!.powerKw!.toStringAsFixed(1)} ${Strings.powerUnit}';

  String get batteryText =>
      batteryPercent == null ? '--' : '${batteryPercent!.toStringAsFixed(0)} %';

  int? get shownDurationSeconds {
    final metered = metering?.durationSeconds;
    if (metered != null) return metered;
    if (startedAt == null || stoppedAt == null) return null;
    return stoppedAt!.difference(startedAt!).inSeconds;
  }

  String get durationText => shownDurationSeconds == null
      ? '--'
      : DurationFormat.compact(shownDurationSeconds!);

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

  double get chargeLevel => ((batteryPercent ?? 0) / 100).clamp(0.0, 1.0);

  String get stateLabel => Strings.sessionStates[state.name] ?? state.name;

  Color get stateColor => switch (state) {
    SessionState.completed => AppColors.success,
    SessionState.failed || SessionState.abandoned => AppColors.danger,
    SessionState.active => AppColors.chargeDeep,
    _ => AppColors.primary,
  };

  String? get statusMessage => switch (state) {
    SessionState.requested ||
    SessionState.startAccepted => Strings.waitingForPlug,
    SessionState.stopping => Strings.finishing,
    SessionState.failed => Strings.sessionFailed,
    _ => null,
  };
}
