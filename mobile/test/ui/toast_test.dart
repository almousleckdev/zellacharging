import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/theme/app_theme.dart';
import 'package:ndzela/shared/app_toast.dart';

import 'test_fonts.dart';

const _message = 'Top up your wallet to start charging';

Future<void> _open(WidgetTester tester) async {
  await tester.pumpWidget(
    MaterialApp(
      theme: AppTheme.light(),
      home: Builder(
        builder: (context) => Scaffold(
          body: Center(
            child: TextButton(
              onPressed: () => AppToast.error(context, _message),
              child: const Text('go'),
            ),
          ),
        ),
      ),
    ),
  );
  await tester.tap(find.text('go'));
  await tester.pump(const Duration(milliseconds: 400));
}

void main() {
  setUpAll(loadTestFonts);

  testWidgets('shows the message', (tester) async {
    await _open(tester);

    expect(find.text(_message), findsOneWidget);
    await tester.pump(const Duration(seconds: 5));
    await tester.pump(const Duration(milliseconds: 400));
  });

  testWidgets('disappears by itself', (tester) async {
    await _open(tester);
    await tester.pump(const Duration(seconds: 5));
    await tester.pump(const Duration(milliseconds: 400));

    expect(find.text(_message), findsNothing);
  });

  testWidgets('a new toast replaces the previous one', (tester) async {
    await _open(tester);
    await tester.tap(find.text('go'));
    await tester.pump(const Duration(milliseconds: 400));

    expect(find.text(_message), findsOneWidget);
    await tester.pump(const Duration(seconds: 5));
    await tester.pump(const Duration(milliseconds: 400));
  });
}
