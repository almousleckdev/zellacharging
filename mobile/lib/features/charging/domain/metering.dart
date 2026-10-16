class Metering {
  const Metering({
    this.totalKwh,
    this.powerKw,
    this.stateOfChargePercent,
    this.durationSeconds,
  });

  final double? totalKwh;
  final double? powerKw;
  final double? stateOfChargePercent;
  final int? durationSeconds;
}
