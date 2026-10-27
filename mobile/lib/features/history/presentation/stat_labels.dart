import '../../../core/l10n/strings.dart';
import '../domain/stat_period.dart';

abstract final class StatLabels {
  static String bucket(StatPeriod period, DateTime start) => switch (period) {
    StatPeriod.day => '${start.hour.toString().padLeft(2, '0')}h',
    StatPeriod.week => StatText.weekdays[start.weekday - 1],
    StatPeriod.month => '${start.day}',
    StatPeriod.all => StatText.months[start.month - 1],
  };

  static String caption(StatPeriod period, DateTime start) => switch (period) {
    StatPeriod.day =>
      '${bucket(period, start)} – ${(start.hour + 3).toString().padLeft(2, '0')}h',
    StatPeriod.week ||
    StatPeriod.month => '${start.day} ${StatText.months[start.month - 1]}',
    StatPeriod.all => '${StatText.months[start.month - 1]} ${start.year}',
  };

  static int step(StatPeriod period) => period == StatPeriod.month ? 5 : 1;
}
