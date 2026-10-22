import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/features/charging/domain/charging_session.dart';
import 'package:ndzela/features/charging/domain/metering.dart';
import 'package:ndzela/features/charging/domain/session_state.dart';
import 'package:ndzela/features/charging/presentation/battery_freshness.dart';

ChargingSession _session(SessionState state, Duration? age) => ChargingSession(
  id: 'a',
  stationId: 'CP-1',
  connectorId: 1,
  state: state,
  createdAt: DateTime(2026),
  metering: Metering(
    sampledAt: age == null ? null : DateTime.now().subtract(age),
  ),
);

Future<void> _show(WidgetTester tester, ChargingSession session) =>
    tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: BatteryFreshness(session: session)),
      ),
    );

String _prefix(String Function(String) text) => text('|').split('|').first;

void main() {
  testWidgets('a recent reading is shown as updated', (tester) async {
    await _show(
      tester,
      _session(SessionState.active, const Duration(seconds: 8)),
    );

    expect(find.textContaining(_prefix(ChargeText.updatedAgo)), findsOneWidget);
    expect(
      find.textContaining(_prefix(ChargeText.lastReadingAgo)),
      findsNothing,
    );
  });

  testWidgets('an old reading is flagged as the last reading', (tester) async {
    await _show(
      tester,
      _session(SessionState.active, const Duration(minutes: 5)),
    );

    expect(
      find.textContaining(_prefix(ChargeText.lastReadingAgo)),
      findsOneWidget,
    );
    expect(find.textContaining('5m'), findsOneWidget);
  });

  testWidgets('nothing is shown without a reading time or when finished', (
    tester,
  ) async {
    await _show(tester, _session(SessionState.active, null));
    expect(find.byType(Text), findsNothing);

    await _show(
      tester,
      _session(SessionState.completed, const Duration(seconds: 3)),
    );
    expect(find.byType(Text), findsNothing);
  });
}
