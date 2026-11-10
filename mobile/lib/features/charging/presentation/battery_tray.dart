import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';

class BatteryTray {
  const BatteryTray._();

  static void glow(Canvas canvas, RRect tray, Color accent, bool charging) {
    canvas.drawRRect(
      tray,
      Paint()
        ..color = accent.withValues(alpha: charging ? 0.32 : 0.2)
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 7),
    );
  }

  static void body(Canvas canvas, RRect tray, Size size) {
    canvas.drawRRect(
      tray,
      Paint()
        ..shader = LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            AppColors.textPrimary.withValues(alpha: 0.88),
            AppColors.textPrimary,
          ],
        ).createShader(Offset.zero & size),
    );
  }

  static void gloss(Canvas canvas, RRect tray, Size size) {
    final upper = Rect.fromLTWH(0, 0, size.width, size.height * 0.45);
    canvas.save();
    canvas.clipRRect(tray);
    canvas.drawRect(
      upper,
      Paint()
        ..shader = LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            AppColors.onPrimary.withValues(alpha: 0.16),
            AppColors.onPrimary.withValues(alpha: 0),
          ],
        ).createShader(upper),
    );
    canvas.restore();
  }

  static void border(Canvas canvas, RRect tray, Color accent) {
    canvas.drawRRect(
      tray.deflate(0.7),
      Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.4
        ..color = accent,
    );
  }
}
