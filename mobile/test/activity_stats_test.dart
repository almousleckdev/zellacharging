import 'package:flutter_test/flutter_test.dart';
import 'package:zella/features/charging/domain/charging_session.dart';
import 'package:zella/features/charging/domain/session_state.dart';
import 'package:zella/features/history/domain/activity_stats.dart';
import 'package:zella/features/history/domain/stat_metric.dart';
import 'package:zella/features/history/domain/stat_period.dart';

final _now = DateTime(2026, 10, 27, 15, 30);

ChargingSession _session(
  DateTime at, {
  SessionState state = SessionState.completed,
  double? kwh = 10,
  int? cost = 2000,
}) => ChargingSession(
  id: at.toIso8601String(),
  stationId: 'CP-1',
  connectorId: 1,
  state: state,
  createdAt: at,
  startedAt: at,
  energyKwh: kwh,
  costMinor: cost,
  currency: 'XAF',
  currencyDecimals: 0,
);

void main() {
  final sessions = [
    _session(DateTime(2026, 10, 27, 9)),
    _session(DateTime(2026, 10, 27, 14), kwh: 5, cost: 1000),
    _session(DateTime(2026, 10, 24, 12)),
    _session(DateTime(2026, 10, 10, 12)),
    _session(DateTime(2026, 9, 15, 12)),
    _session(
      DateTime(2026, 10, 27, 11),
      state: SessionState.failed,
      kwh: null,
      cost: null,
    ),
  ];

  test('the day view keeps only today, in 3 hour buckets', () {
    final report = ActivityStats.report(
      sessions,
      StatMetric.charges,
      StatPeriod.day,
      _now,
    );
    expect(report.buckets, hasLength(8));
    expect(report.charges, 3);
    expect(report.buckets[3].value, 2);
    expect(report.buckets[4].value, 1);
  });

  test('the week view spans the last seven days', () {
    final report = ActivityStats.report(
      sessions,
      StatMetric.spent,
      StatPeriod.week,
      _now,
    );
    expect(report.buckets, hasLength(7));
    expect(report.charges, 4);
    expect(report.spentMinor, 5000);
    expect(report.buckets.last.value, 3000);
    expect(report.buckets[3].value, 2000);
  });

  test('the month view covers the whole current month', () {
    final report = ActivityStats.report(
      sessions,
      StatMetric.energy,
      StatPeriod.month,
      _now,
    );
    expect(report.buckets, hasLength(31));
    expect(report.charges, 5);
    expect(report.energyKwh, 35);
  });

  test('the all view groups by month and includes everything', () {
    final report = ActivityStats.report(
      sessions,
      StatMetric.charges,
      StatPeriod.all,
      _now,
    );
    expect(report.buckets, hasLength(2));
    expect(report.buckets.first.value, 1);
    expect(report.buckets.last.value, 5);
    expect(report.charges, 6);
  });

  test('failed sessions count as charges but never as energy or money', () {
    final report = ActivityStats.report(
      sessions,
      StatMetric.energy,
      StatPeriod.day,
      _now,
    );
    expect(report.charges, 3);
    expect(report.energyKwh, 15);
    expect(report.spentMinor, 3000);
  });

  test('no sessions gives an empty report with flat buckets', () {
    final report = ActivityStats.report(
      const [],
      StatMetric.spent,
      StatPeriod.all,
      _now,
    );
    expect(report.isEmpty, isTrue);
    expect(report.peak, 0);
    expect(report.currency, isNull);
  });

  test('sessions are listed newest first', () {
    final report = ActivityStats.report(
      sessions,
      StatMetric.charges,
      StatPeriod.week,
      _now,
    );
    final times = report.sessions.map((s) => s.createdAt).toList();
    expect([...times]..sort((a, b) => b.compareTo(a)), times);
  });
}
