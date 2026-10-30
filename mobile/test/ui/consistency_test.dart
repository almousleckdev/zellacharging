import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/core/theme/app_spacing.dart';
import 'package:zella/features/charging/presentation/setup/charge_setup_page.dart';
import 'package:zella/features/home/presentation/home_page.dart';
import 'package:zella/features/notifications/presentation/notification_groups.dart';
import 'package:zella/features/stations/presentation/locations_page.dart';
import 'package:zella/features/wallet/presentation/topup_page.dart';
import 'package:zella/features/wallet/presentation/withdraw_page.dart';
import 'package:zella/features/wallet/domain/mobile_operator.dart';
import 'package:zella/shared/widgets/pay/amount_card.dart';
import 'package:zella/shared/widgets/pay/mobile_money_card.dart';
import 'package:zella/shared/widgets/pay/operator_tiles.dart';
import 'package:zella/shared/widgets/primary_button.dart';
import 'package:zella/shared/widgets/search_field.dart';
import 'package:zella/shared/widgets/secondary_button.dart';

import 'fake_notifications.dart';
import 'screens.dart';
import 'test_app.dart';
import 'test_fonts.dart';

void main() {
  setUpAll(loadTestFonts);

  for (final entry in {
    'top-up': const TopupPage(),
    'withdraw': const WithdrawPage(),
    'charge setup': const ChargeSetupPage(
      stationId: 'x',
      initialConnectorId: 1,
    ),
  }.entries) {
    testWidgets('${entry.key} enters its amount with the shared card', (
      tester,
    ) async {
      await pumpApp(tester, entry.value, overrides: overrides());

      expect(find.byType(AmountCard), findsOneWidget);
      await closeApp(tester);
    });
  }

  for (final entry in {
    'top-up': const TopupPage(),
    'withdraw': const WithdrawPage(),
  }.entries) {
    testWidgets(
      '${entry.key} picks the number and operator with the shared card',
      (tester) async {
        await pumpApp(tester, entry.value, overrides: overrides());
        await tester.pump(const Duration(milliseconds: 300));

        expect(find.byType(MobileMoneyCard), findsOneWidget);
        expect(find.byType(OperatorTiles), findsOneWidget);
        final logos = tester
            .widgetList<Image>(find.byType(Image))
            .map((image) => image.image)
            .whereType<AssetImage>()
            .map((image) => image.assetName);
        expect(
          logos,
          containsAll([MobileOperator.airtel.logo, MobileOperator.moov.logo]),
        );
        await closeApp(tester);
      },
    );
  }

  testWidgets('primary and secondary buttons share one height', (tester) async {
    await pumpApp(
      tester,
      Scaffold(
        body: Column(
          children: [
            PrimaryButton(label: 'one', onPressed: () {}),
            SecondaryButton(label: 'two', onPressed: () {}),
            SecondaryButton(
              label: 'three',
              destructive: true,
              onPressed: () {},
            ),
          ],
        ),
      ),
      overrides: overrides(),
    );

    for (final type in [PrimaryButton, SecondaryButton]) {
      for (final element in find.byType(type).evaluate()) {
        expect(
          tester.getSize(find.byWidget(element.widget)).height,
          AppSpacing.buttonHeight,
        );
      }
    }
    await closeApp(tester);
  });

  testWidgets('the home screen has no search input', (tester) async {
    await pumpApp(
      tester,
      const Scaffold(body: HomePage()),
      overrides: overrides(),
    );

    expect(find.byType(TextField), findsNothing);
    expect(find.byType(SearchField), findsNothing);
    await closeApp(tester);
  });

  testWidgets('the stations screen filters by availability', (tester) async {
    await pumpApp(
      tester,
      const Scaffold(body: LocationsPage()),
      overrides: overrides(),
    );
    expect(find.text(StationText.count(2)), findsOneWidget);

    await tester.tap(find.text(StationText.filterAvailable));
    await settle(tester);

    expect(find.text(StationText.count(1)), findsOneWidget);
    expect(find.text('CP-SITE-001'), findsNothing);
    await closeApp(tester);
  });

  test('notifications are grouped by day with friendly labels', () {
    final now = DateTime(2026, 10, 30, 12);
    final groups = NotificationGroups.of([
      ...sampleNotifications().take(1),
      sampleNotifications().last,
    ], now);

    expect(groups.map((group) => group.label), [
      NotificationText.today,
      NotificationText.yesterday,
    ]);
    expect(groups.every((group) => group.items.length == 1), isTrue);
  });
}
