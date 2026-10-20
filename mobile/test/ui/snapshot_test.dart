import 'dart:io';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/theme/app_theme.dart';

import 'screens.dart';
import 'test_fonts.dart';

void main() {
  setUpAll(loadTestFonts);
  final out = Platform.environment['SNAPSHOT_DIR'];
  for (final entry in screens.entries) {
    testWidgets('snapshot ${entry.key}', (tester) async {
      if (out == null) return;
      addTearDown(tester.view.reset);
      tester.view.physicalSize = const Size(780, 1688);
      tester.view.devicePixelRatio = 2;
      final key = GlobalKey();
      await tester.pumpWidget(
        ProviderScope(
          overrides: overrides(),
          child: RepaintBoundary(
            key: key,
            child: MaterialApp(
              debugShowCheckedModeBanner: false,
              theme: AppTheme.light(),
              home: Scaffold(body: entry.value()),
            ),
          ),
        ),
      );
      await tester.runAsync(
        () => precacheImage(
          const AssetImage('assets/images/charging_scene.png'),
          tester.element(find.byType(Scaffold).first),
        ),
      );
      for (var i = 0; i < 8; i++) {
        await tester.pump(const Duration(milliseconds: 150));
      }
      await tester.runAsync(() async {
        final boundary =
            key.currentContext!.findRenderObject() as RenderRepaintBoundary;
        final image = await boundary.toImage(pixelRatio: 2);
        final bytes = await image.toByteData(format: ui.ImageByteFormat.png);
        File(
          '$out/${entry.key}.png',
        ).writeAsBytesSync(bytes!.buffer.asUint8List());
      });
      await tester.pumpWidget(const SizedBox());
      await tester.pump(const Duration(seconds: 6));
    });
  }
}
