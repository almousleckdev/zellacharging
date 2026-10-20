import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/theme/app_theme.dart';

import 'test_fonts.dart';
import 'screens.dart';

const _sizes = [Size(320, 568), Size(375, 667), Size(390, 844), Size(430, 932)];
const _scales = [1.0, 1.3, 1.6];

Future<void> _show(
  WidgetTester tester,
  Widget screen,
  Size size,
  double scale,
) async {
  tester.view.physicalSize = size;
  tester.view.devicePixelRatio = 1;
  await tester.pumpWidget(
    ProviderScope(
      overrides: overrides(),
      child: MaterialApp(
        theme: AppTheme.light(),
        builder: (context, child) => MediaQuery(
          data: MediaQuery.of(
            context,
          ).copyWith(textScaler: TextScaler.linear(scale)),
          child: child!,
        ),
        home: Scaffold(body: screen),
      ),
    ),
  );
  for (var i = 0; i < 6; i++) {
    await tester.pump(const Duration(milliseconds: 150));
  }
}

void main() {
  setUpAll(loadTestFonts);

  for (final entry in screens.entries) {
    for (final size in _sizes) {
      for (final scale in _scales) {
        testWidgets(
          '${entry.key} ${size.width.toInt()}x${size.height.toInt()} x$scale',
          (tester) async {
            addTearDown(tester.view.reset);
            await _show(tester, entry.value(), size, scale);
            final error = tester.takeException();
            await tester.pumpWidget(const SizedBox());
            await tester.pump(const Duration(seconds: 6));
            expect(error, isNull, reason: '${entry.key} overflowed');
          },
        );
      }
    }
  }
}
