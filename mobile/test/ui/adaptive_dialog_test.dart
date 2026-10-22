import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/l10n/strings.dart';
import 'package:ndzela/core/theme/app_theme.dart';
import 'package:ndzela/shared/adaptive_dialog.dart';

import 'test_fonts.dart';

Future<void> _open(WidgetTester tester, TargetPlatform platform) async {
  await tester.pumpWidget(
    MaterialApp(
      theme: AppTheme.light().copyWith(platform: platform),
      home: Builder(
        builder: (context) => Scaffold(
          body: TextButton(
            onPressed: () => showConfirmDialog(
              context,
              title: 'Titre',
              message: 'Message',
              confirmLabel: 'Confirmer',
              destructive: true,
            ),
            child: const Text('open'),
          ),
        ),
      ),
    ),
  );
  await tester.tap(find.text('open'));
  await tester.pump(const Duration(milliseconds: 500));
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('iOS gets a Cupertino dialog with Cupertino actions', (
    tester,
  ) async {
    await _open(tester, TargetPlatform.iOS);

    expect(find.byType(CupertinoAlertDialog), findsOneWidget);
    expect(find.byType(CupertinoDialogAction), findsNWidgets(2));
    expect(find.byType(TextButton), findsOneWidget);
    expect(find.text(Strings.cancel), findsOneWidget);
  });

  testWidgets('Android gets a Material dialog with Material actions', (
    tester,
  ) async {
    await _open(tester, TargetPlatform.android);

    expect(find.byType(CupertinoAlertDialog), findsNothing);
    expect(find.byType(CupertinoDialogAction), findsNothing);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.byType(TextButton), findsNWidgets(3));
  });
}
