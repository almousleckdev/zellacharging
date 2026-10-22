import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/format/money_format.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/core/theme/app_theme.dart';
import 'package:ndzela/features/charging/domain/charging_session.dart';
import 'package:ndzela/features/charging/domain/session_fees.dart';
import 'package:ndzela/features/charging/domain/session_state.dart';
import 'package:ndzela/features/charging/presentation/charging_page.dart';
import 'package:ndzela/features/charging/presentation/session_providers.dart';

import 'screens.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

class _Sessions extends FakeSessions {
  _Sessions() : super(SessionState.completed);

  @override
  Future<ChargingSession> get(String id) async => ChargingSession(
    id: id,
    stationId: 'CP-1',
    connectorId: 1,
    state: SessionState.completed,
    createdAt: DateTime(2026, 10, 16, 14, 30),
    startedAt: DateTime(2026, 10, 16, 14, 31),
    stoppedAt: DateTime(2026, 10, 16, 15, 46),
    energyKwh: 18.5,
    batteryPercent: 64,
    lastVoltage: 401,
    fees: const SessionFees(
      electricMinor: 1000,
      vatMinor: 180,
      serviceMinor: 50,
      totalMinor: 1230,
      currency: 'XAF',
      decimals: 0,
    ),
  );
}

Future<_Sessions> _open(WidgetTester tester) async {
  tester.view.physicalSize = const Size(780, 3000);
  tester.view.devicePixelRatio = 2;
  addTearDown(tester.view.reset);
  final sessions = _Sessions();
  await tester.pumpWidget(
    ProviderScope(
      overrides: [
        ...overrides().skip(2),
        sessionRepositoryProvider.overrideWithValue(sessions),
        ...overrides().take(1),
      ],
      child: MaterialApp(
        theme: AppTheme.light(),
        home: const ChargingPage(sessionId: 'a'),
      ),
    ),
  );
  await tester.pump(const Duration(milliseconds: 600));
  return sessions;
}

Future<void> _close(WidgetTester tester) async {
  await tester.pumpWidget(const SizedBox());
  await tester.pump(const Duration(seconds: 6));
}

String _money(int minor) => MoneyFormat.format(minor, 'XAF', 0);

void main() {
  setUpAll(loadTestFonts);

  testWidgets('shows the details sections in the requested order', (
    tester,
  ) async {
    await _open(tester);

    final order = [
      HistoryText.detailsTitle,
      HistoryText.totalDuration,
      HistoryText.locationTitle,
      HistoryText.feeDetails,
      HistoryText.deviceInfo,
    ];
    final tops = [
      for (final title in order) tester.getTopLeft(find.text(title).first).dy,
    ];
    expect(tops, orderedEquals([...tops]..sort()));
    await _close(tester);
  });

  testWidgets('shows the totals and the fee breakdown', (tester) async {
    await _open(tester);

    expect(find.text('1h 15m'), findsOneWidget);
    expect(find.text('18.50 kWh'), findsOneWidget);
    expect(find.text(_money(1000)), findsOneWidget);
    expect(find.text(_money(180)), findsOneWidget);
    expect(find.text(_money(50)), findsOneWidget);
    expect(find.text(_money(1230)), findsWidgets);
    await _close(tester);
  });

  testWidgets('shows the device information', (tester) async {
    await _open(tester);

    expect(find.text(HistoryText.deviceNo), findsOneWidget);
    expect(find.text('401 ${Strings.voltageUnit}'), findsOneWidget);
    expect(find.text(HistoryText.startTime), findsOneWidget);
    expect(find.text(HistoryText.endTime), findsOneWidget);
    await _close(tester);
  });

  testWidgets('deleting asks for confirmation first', (tester) async {
    final sessions = await _open(tester);

    await tester.tap(find.byTooltip(HistoryText.deleteCharge));
    await tester.pump(const Duration(milliseconds: 400));
    expect(find.text(HistoryText.deleteTitle), findsOneWidget);
    expect(sessions.deleted, isEmpty);

    await tester.tap(find.text(Strings.cancel));
    await tester.pump(const Duration(milliseconds: 400));
    expect(sessions.deleted, isEmpty);
    await _close(tester);
  });
}
