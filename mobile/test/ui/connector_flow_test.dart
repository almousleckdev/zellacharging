import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/core/theme/app_icons.dart';
import 'package:ndzela/core/theme/app_theme.dart';
import 'package:ndzela/features/charging/presentation/setup/charge_setup_page.dart';
import 'package:ndzela/features/stations/domain/station.dart';
import 'package:ndzela/features/stations/domain/station_connector.dart';
import 'package:ndzela/features/stations/presentation/station_providers.dart';

import 'screens.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';

const _busyFirst = Station(
  id: 'CP-1',
  online: true,
  availableConnectors: 1,
  totalConnectors: 2,
  connectors: [
    StationConnector(connectorId: 1, status: 'Charging', available: false),
    StationConnector(connectorId: 2, status: 'Available', available: true),
  ],
);

class _OneStation extends FakeStations {
  @override
  Future<Station> byId(String id) async => _busyFirst;
}

Future<void> _openSetup(WidgetTester tester, int scanned) async {
  tester.view.physicalSize = const Size(780, 1688);
  tester.view.devicePixelRatio = 2;
  addTearDown(tester.view.reset);
  await tester.pumpWidget(
    ProviderScope(
      overrides: [
        stationRepositoryProvider.overrideWithValue(_OneStation()),
        ...overrides().skip(1),
      ],
      child: MaterialApp(
        theme: AppTheme.light(),
        home: ChargeSetupPage(stationId: 'CP-1', initialConnectorId: scanned),
      ),
    ),
  );
  await tester.pump(const Duration(milliseconds: 600));
}

Future<void> _close(WidgetTester tester) async {
  await tester.pumpWidget(const SizedBox());
  await tester.pump(const Duration(seconds: 6));
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('a scanned connector that is in use is not selected', (
    tester,
  ) async {
    await _openSetup(tester, 1);

    expect(find.text(StationText.connectorBusy), findsOneWidget);
    expect(find.byIcon(AppIcons.lock), findsOneWidget);
    expect(find.byIcon(AppIcons.checkCircle), findsNothing);
    await _close(tester);
  });

  testWidgets('the free connector can be chosen', (tester) async {
    await _openSetup(tester, 1);
    await tester.tap(find.text('${StationText.connector} 2'));
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.byIcon(AppIcons.checkCircle), findsOneWidget);
    await _close(tester);
  });
}
