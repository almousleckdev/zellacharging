class Metering {
  const Metering({
    this.totalKwh,
    this.powerKw,
    this.durationSeconds,
    this.voltage,
    this.currentAmpere,
    this.sampledAt,
  });

  final double? totalKwh;
  final double? powerKw;
  final int? durationSeconds;
  final double? voltage;
  final double? currentAmpere;
  final DateTime? sampledAt;
}
