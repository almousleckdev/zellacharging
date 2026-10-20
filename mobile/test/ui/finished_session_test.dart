import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/core/theme/app_theme.dart';
import 'package:ndzela/features/charging/domain/charging_session.dart';
import 'package:ndzela/features/charging/domain/session_state.dart';
import 'package:ndzela/features/charging/presentation/charging_page.dart';
import 'package:ndzela/features/charging/presentation/session_providers.dart';

import 'screens.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

class _FinishedSessions extends FakeSessions {
  _FinishedSessions() : super(SessionState.completed);

  @override
  Future<ChargingSession> get(String id) async => ChargingSession(
    id: id,
    stationId: 'CP-1',
    connectorId: 1,
    state: SessionState.completed,
    createdAt: DateTime(2026, 10, 16, 14, 30),
    startedAt: DateTime(2026, 10, 16, 14, 31),
    stoppedAt: DateTime(2026, 10, 16, 15, 46),
    batteryPercent: 64,
    energyKwh: 18.5,
    costMinor: 2775,
    currency: 'XAF',
    currencyDecimals: 0,
  );
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('finished charge shows only what is known', (tester) async {
    tester.view.physicalSize = const Size(780, 1688);
    tester.view.devicePixelRatio = 2;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          ...overrides().take(1),
          sessionRepositoryProvider.overrideWithValue(_FinishedSessions()),
          ...overrides().skip(2),
        ],
        child: MaterialApp(
          theme: AppTheme.light(),
          home: const ChargingPage(sessionId: 'a'),
        ),
      ),
    );
    await tester.pump(const Duration(milliseconds: 300));
    expect(find.textContaining('64', findRichText: true), findsWidgets);
    expect(find.text('1h 15m'), findsOneWidget);
    expect(find.text('2775 XAF'), findsOneWidget);
    expect(find.text(Strings.voltage), findsNothing);
    expect(find.text(Strings.current), findsNothing);
    expect(find.text('--'), findsNothing);
    await tester.pumpWidget(const SizedBox());
    await tester.pump(const Duration(seconds: 6));
  });
}
