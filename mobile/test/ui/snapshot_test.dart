import 'dart:io';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ndzela/core/theme/app_colors.dart';
import 'package:ndzela/core/theme/app_theme.dart';

import 'package:ndzela/features/charging/domain/session_state.dart';
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
          overrides: overrides(screenStates[entry.key] ?? SessionState.active),
          child: RepaintBoundary(
            key: key,
            child: MaterialApp(
              debugShowCheckedModeBanner: false,
              theme: AppTheme.light(),
              home: Scaffold(
                backgroundColor:
                    const {'home', 'history', 'settings'}.contains(entry.key)
                    ? AppColors.surfaceMuted
                    : null,
                body: entry.value(),
              ),
            ),
          ),
        ),
      );
      await tester.runAsync(() async {
        final context = tester.element(find.byType(Scaffold).first);
        for (final name in const [
          'charging_scene',
          'onboarding_find',
          'onboarding_scan',
          'track_every_charge',
        ]) {
          await precacheImage(AssetImage('assets/images/$name.png'), context);
        }
        await precacheImage(
          const AssetImage('assets/images/auth_background.jpg'),
          context,
        );
      });
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
