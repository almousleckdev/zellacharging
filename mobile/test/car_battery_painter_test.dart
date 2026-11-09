import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zella/features/charging/presentation/car_battery_painter.dart';

void main() {
  const levels = [0.0, 0.04, 0.5, 0.86, 0.95, 0.999, 1.0];

  testWidgets('the battery draws at every level and every animation step', (
    tester,
  ) async {
    for (final charging in [true, false]) {
      for (final level in levels) {
        for (final progress in [0.0, 0.33, 0.66, 1.0]) {
          await tester.pumpWidget(
            Directionality(
              textDirection: TextDirection.ltr,
              child: SizedBox(
                width: 108,
                height: 19,
                child: CustomPaint(
                  painter: CarBatteryPainter(
                    level: level,
                    progress: progress,
                    charging: charging,
                    pixelRatio: 3,
                  ),
                ),
              ),
            ),
          );
          expect(tester.takeException(), isNull);
        }
      }
    }
  });

  test(
    'the painter repaints when the animation or the screen density changes',
    () {
      const base = CarBatteryPainter(
        level: 0.9,
        progress: 0.2,
        charging: true,
        pixelRatio: 3,
      );
      const moved = CarBatteryPainter(
        level: 0.9,
        progress: 0.4,
        charging: true,
        pixelRatio: 3,
      );
      const sharper = CarBatteryPainter(
        level: 0.9,
        progress: 0.2,
        charging: true,
        pixelRatio: 2,
      );

      expect(base.shouldRepaint(moved), isTrue);
      expect(base.shouldRepaint(sharper), isTrue);
      expect(base.shouldRepaint(base), isFalse);
    },
  );
}
