import '../../../core/format/money_format.dart';
import '../../../core/l10n/strings.dart';
import '../domain/stat_metric.dart';
import '../domain/stat_report.dart';

abstract final class StatValueFormat {
  static String of(StatReport report, double value) => switch (report.metric) {
    StatMetric.charges => value.round().toString(),
    StatMetric.energy => '${value.toStringAsFixed(1)} ${Strings.kwhUnit}',
    StatMetric.spent => money(report, value),
  };

  static String money(StatReport report, double value) =>
      report.currency == null
      ? '0'
      : MoneyFormat.format(
          value.round(),
          report.currency!,
          report.currencyDecimals,
        );

  static String energy(double kwh) =>
      '${kwh.toStringAsFixed(1)} ${Strings.kwhUnit}';
}
