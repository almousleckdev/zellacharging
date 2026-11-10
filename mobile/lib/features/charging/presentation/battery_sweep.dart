import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';

class BatterySweep {
  const BatterySweep._();

  static const _passes = 2;

  static void filled(
    Canvas canvas,
    List<Rect> cells,
    Radius radius,
    double exact,
    double progress,
  ) {
    final full = exact.floor();
    final partial = exact - full;
    if (full == 0 && partial == 0) return;
    final path = Path();
    for (var i = 0; i < math.min(full, cells.length); i++) {
      path.addRRect(RRect.fromRectAndRadius(cells[i], radius));
    }
    if (full < cells.length && partial > 0) {
      final c = cells[full];
      path.addRect(Rect.fromLTWH(c.left, c.top, c.width * partial, c.height));
    }
    final right = full >= cells.length
        ? cells.last.right
        : cells[full].left + cells[full].width * partial;
    final band = cells.first.width * 3;
    canvas.save();
    canvas.clipPath(path);
    for (var pass = 0; pass < _passes; pass++) {
      final phase = (progress * _passes + pass / _passes) % 1.0;
      final centre =
          cells.first.left -
          band +
          (right - cells.first.left + 2 * band) * phase;
      final stripe = Rect.fromLTRB(
        centre - band,
        cells.first.top,
        centre + band,
        cells.first.bottom,
      );
      canvas.drawRect(
        stripe,
        Paint()
          ..shader = LinearGradient(
            colors: [
              AppColors.onPrimary.withValues(alpha: 0),
              AppColors.onPrimary.withValues(alpha: 0.42),
              AppColors.onPrimary.withValues(alpha: 0),
            ],
          ).createShader(stripe),
      );
    }
    canvas.restore();
  }

  static void ahead(
    Canvas canvas,
    List<Rect> cells,
    Radius radius,
    double exact,
    double progress,
  ) {
    final head = progress * (cells.length + 4) - 2;
    for (var i = exact.floor() + 1; i < cells.length; i++) {
      final glow = math.max(0.0, 1 - (i - head).abs() / 2.5) * 0.3;
      if (glow <= 0) continue;
      canvas.drawRRect(
        RRect.fromRectAndRadius(cells[i], radius),
        Paint()..color = AppColors.chargeGreen.withValues(alpha: glow),
      );
    }
  }
}
