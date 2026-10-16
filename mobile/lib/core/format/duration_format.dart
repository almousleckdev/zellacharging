abstract final class DurationFormat {
  static String compact(int seconds) {
    final hours = seconds ~/ 3600;
    final minutes = (seconds % 3600) ~/ 60;
    return hours > 0 ? '${hours}h ${minutes}m' : '${minutes}m ${seconds % 60}s';
  }
}
