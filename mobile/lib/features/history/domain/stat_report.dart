import '../../charging/domain/charging_session.dart';
import 'stat_bucket.dart';
import 'stat_metric.dart';
import 'stat_period.dart';

class StatReport {
  const StatReport({
    required this.metric,
    required this.period,
    required this.buckets,
    required this.sessions,
    required this.charges,
    required this.energyKwh,
    required this.spentMinor,
    required this.pricedCharges,
    required this.currency,
    required this.currencyDecimals,
  });

  final StatMetric metric;
  final StatPeriod period;
  final List<StatBucket> buckets;
  final List<ChargingSession> sessions;
  final int charges;
  final double energyKwh;
  final int spentMinor;
  final int pricedCharges;
  final String? currency;
  final int currencyDecimals;

  double get total => switch (metric) {
    StatMetric.charges => charges.toDouble(),
    StatMetric.energy => energyKwh,
    StatMetric.spent => spentMinor.toDouble(),
  };

  double get averageSpentMinor =>
      pricedCharges == 0 ? 0 : spentMinor / pricedCharges;

  double get peak =>
      buckets.fold(0.0, (top, b) => b.value > top ? b.value : top);

  bool get isEmpty => sessions.isEmpty;
}
