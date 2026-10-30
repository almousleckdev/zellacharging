import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/core/theme/app_colors.dart';
import 'package:zella/core/theme/app_icons.dart';
import 'package:zella/features/auth/presentation/social_buttons.dart';
import 'package:zella/features/wallet/domain/mobile_operator.dart';
import 'package:zella/features/wallet/presentation/wallet_entry_page.dart';
import 'package:zella/shared/widgets/info_row.dart';
import 'package:zella/shared/widgets/pay/operator_tiles.dart';

import 'test_app.dart';
import 'test_fonts.dart';
import 'screens.dart';

bool _hasAsset(WidgetTester tester, String asset) => tester
    .widgetList<Image>(find.byType(Image))
    .any(
      (image) =>
          image.image is AssetImage &&
          (image.image as AssetImage).assetName == asset,
    );

void main() {
  setUpAll(loadTestFonts);

  testWidgets('the operator tiles show the logos instead of the names', (
    tester,
  ) async {
    await pumpApp(
      tester,
      Scaffold(
        body: OperatorTiles(
          selected: MobileOperator.airtel,
          onSelected: (_) {},
        ),
      ),
      overrides: overrides(),
    );

    expect(_hasAsset(tester, MobileOperator.airtel.logo), isTrue);
    expect(_hasAsset(tester, MobileOperator.moov.logo), isTrue);
    expect(find.text('Airtel Money'), findsNothing);
    expect(find.text('Moov Money'), findsNothing);
    await closeApp(tester);
  });

  testWidgets('the operation detail shows the logo of the operator used', (
    tester,
  ) async {
    await pumpApp(
      tester,
      const WalletEntryPage(entryId: '1'),
      overrides: overrides(),
    );
    await tester.pump(const Duration(milliseconds: 500));

    expect(_hasAsset(tester, MobileOperator.moov.logo), isTrue);
    expect(find.byIcon(AppIcons.wallet), findsNothing);
    await closeApp(tester);
  });

  testWidgets('the values of an info row sit on the right edge', (
    tester,
  ) async {
    await pumpApp(
      tester,
      const Scaffold(
        body: Padding(
          padding: EdgeInsets.all(16),
          child: InfoRow(label: 'Type', value: 'Recharge'),
        ),
      ),
      overrides: overrides(),
    );

    final row = tester.getRect(find.byType(InfoRow));
    final value = tester.getRect(find.text('Recharge'));
    expect(value.right, closeTo(row.right, 0.5));
    await closeApp(tester);
  });

  for (final entry in {
    TargetPlatform.android: false,
    TargetPlatform.iOS: true,
  }.entries) {
    testWidgets('Apple sign-in is ${entry.value ? 'shown' : 'hidden'} on '
        '${entry.key.name}', (tester) async {
      await pumpApp(
        tester,
        const Scaffold(body: SocialButtons()),
        overrides: overrides(),
        platform: entry.key,
      );

      expect(find.text(AuthText.google), findsOneWidget);
      expect(find.text(AuthText.apple), findsNWidgets(entry.value ? 1 : 0));
      expect(_hasAsset(tester, 'assets/icons/google.png'), isTrue);
      await closeApp(tester);
    });
  }

  testWidgets('the back arrow is the orange circle from the design', (
    tester,
  ) async {
    await pumpApp(
      tester,
      Builder(
        builder: (context) => Scaffold(
          body: TextButton(
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute<void>(
                builder: (_) =>
                    Scaffold(appBar: AppBar(title: const Text('x'))),
              ),
            ),
            child: const Text('go'),
          ),
        ),
      ),
      overrides: overrides(),
    );
    await tester.tap(find.text('go'));
    await settle(tester);

    final arrow = tester.widget<Icon>(find.byIcon(AppIcons.arrowLeft));
    expect(arrow.color, AppColors.primary);
    await closeApp(tester);
  });
}
