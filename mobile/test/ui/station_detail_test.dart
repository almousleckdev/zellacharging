import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/stations/presentation/detail/directions_link.dart';
import 'package:zella/features/stations/presentation/station_detail_page.dart';

import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';
import 'ui_fakes.dart';
import 'package:zella/features/stations/domain/station.dart';

Future<void> _scrollTo(WidgetTester tester, Finder target) async {
  await tester.scrollUntilVisible(
    target,
    200,
    scrollable: find.byType(Scrollable).first,
  );
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('shows the Gabon photo, the hardware and the availability', (
    tester,
  ) async {
    await pumpApp(
      tester,
      StationDetailPage(stationId: stations.first.id),
      overrides: overrides(),
    );

    final images = tester.widgetList<Image>(find.byType(Image));
    expect(
      images.any(
        (image) =>
            image.image is AssetImage &&
            (image.image as AssetImage).assetName ==
                'assets/images/stationgabon.png',
      ),
      isTrue,
    );
    expect(find.text('JC.DW · V1.1'), findsOneWidget);
    expect(find.text(StationDetailText.availableOf(1, 2)), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('lists the information, the coordinates and the connectors', (
    tester,
  ) async {
    await pumpApp(
      tester,
      StationDetailPage(stationId: stations.first.id),
      overrides: overrides(),
    );
    await _scrollTo(tester, find.text(StationDetailText.coordinates));

    expect(find.text('Gabon'), findsOneWidget);
    expect(find.text('0.41620, 9.46730'), findsOneWidget);
    expect(find.text(StationDetailText.manufacturer), findsOneWidget);
    await closeApp(tester);
  });

  testWidgets('the directions button opens the maps app on the station', (
    tester,
  ) async {
    final links = FakeLinks();
    await pumpApp(
      tester,
      StationDetailPage(stationId: stations.first.id),
      overrides: overridesWith(links: links),
    );
    await _scrollTo(tester, find.text(StationDetailText.directions));

    await tester.ensureVisible(find.text(StationDetailText.directions));
    await tester.pump(const Duration(milliseconds: 200));
    await tester.tap(find.text(StationDetailText.directions));
    await tester.pump(const Duration(milliseconds: 200));

    expect(links.opened.single.queryParameters['destination'], '0.4162,9.4673');
    await closeApp(tester);
  });

  test('directions use Apple Maps on iOS and Google Maps on Android', () {
    final ios = DirectionsLink.to(0.4162, 9.4673, TargetPlatform.iOS);
    final android = DirectionsLink.to(0.4162, 9.4673, TargetPlatform.android);

    expect(ios.host, 'maps.apple.com');
    expect(ios.queryParameters['daddr'], '0.4162,9.4673');
    expect(android.host, 'www.google.com');
    expect(android.queryParameters['destination'], '0.4162,9.4673');
  });

  testWidgets(
    'a station the server sent without a country still shows the photo',
    (tester) async {
      await pumpApp(
        tester,
        StationDetailPage(stationId: stations.last.id),
        overrides: overridesWith(stationRepo: _Unlocated()),
      );

      final assets = tester
          .widgetList<Image>(find.byType(Image))
          .map((image) => image.image)
          .whereType<AssetImage>()
          .map((image) => image.assetName);
      expect(assets, contains('assets/images/stationgabon.png'));
      expect(assets, isNot(contains('assets/images/bornes.png')));
      await closeApp(tester);
    },
  );

  testWidgets('a station without coordinates has no map', (tester) async {
    await pumpApp(
      tester,
      StationDetailPage(stationId: stations.last.id),
      overrides: overridesWith(stationRepo: _Unlocated()),
    );

    expect(find.text(StationDetailText.directions), findsNothing);
    await closeApp(tester);
  });
}

class _Unlocated extends FakeStations {
  @override
  Future<Station> byId(String id) async => stations.last;
}
