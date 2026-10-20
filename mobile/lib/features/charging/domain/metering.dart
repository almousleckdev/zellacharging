class Metering {
  const Metering({
    this.totalKwh,
    this.powerKw,
    this.stateOfChargePercent,
    this.durationSeconds,
    this.voltage,
    this.currentAmpere,
  });

  final double? totalKwh;
  final double? powerKw;
  final double? stateOfChargePercent;
  final int? durationSeconds;
  final double? voltage;
  final double? currentAmpere;
}
