import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import 'battery_sweep.dart';
import 'battery_tray.dart';

class CarBatteryPainter extends CustomPainter {
  const CarBatteryPainter({
    required this.level,
    required this.progress,
    required this.charging,
    required this.pixelRatio,
  });

  final double level;
  final double progress;
  final bool charging;
  final double pixelRatio;

  static const _cells = 12;
  static const _gap = 1.6;
  static const _inset = 4.0;
  static const _cellRadius = Radius.circular(2);

  Color get _accent => charging ? AppColors.chargeGreen : AppColors.chargeGlow;

  @override
  void paint(Canvas canvas, Size size) {
    final tray = RRect.fromRectAndRadius(
      Offset.zero & size,
      Radius.circular(size.height * 0.3),
    );
    BatteryTray.glow(canvas, tray, _accent, charging);
    BatteryTray.body(canvas, tray, size);
    final inner = tray.deflate(_inset).outerRect;
    final cells = _layout(inner);
    final exact = level.clamp(0.0, 1.0) * _cells;
    _drawCells(canvas, inner, cells, exact);
    if (charging) {
      BatterySweep.ahead(canvas, cells, _cellRadius, exact, progress);
      BatterySweep.filled(canvas, cells, _cellRadius, exact, progress);
    }
    BatteryTray.gloss(canvas, tray, size);
    BatteryTray.border(canvas, tray, _accent);
  }

  List<Rect> _layout(Rect inner) {
    final total = (inner.width * pixelRatio).floor();
    final gap = math.max(2, (_gap * pixelRatio).round());
    final cell = (total - gap * (_cells - 1)) ~/ _cells;
    final used = cell * _cells + gap * (_cells - 1);
    final start = (inner.left * pixelRatio + (total - used) / 2)
        .roundToDouble();
    final top = (inner.top * pixelRatio).roundToDouble();
    final height = (inner.height * pixelRatio).floorToDouble();
    return [
      for (var i = 0; i < _cells; i++)
        Rect.fromLTWH(
          (start + i * (cell + gap)) / pixelRatio,
          top / pixelRatio,
          cell / pixelRatio,
          height / pixelRatio,
        ),
    ];
  }

  void _drawCells(Canvas canvas, Rect inner, List<Rect> cells, double exact) {
    final full = exact.floor();
    final partial = exact - full;
    final solid = Paint()
      ..shader = const LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [AppColors.chargeGreen, AppColors.chargeDeep],
      ).createShader(inner);
    final pulse = 0.5 - 0.5 * math.cos(progress * 4 * math.pi);
    for (var i = 0; i < _cells; i++) {
      final cell = RRect.fromRectAndRadius(cells[i], _cellRadius);
      canvas.drawRRect(
        cell,
        Paint()..color = AppColors.onPrimary.withValues(alpha: 0.08),
      );
      if (charging && i == full) {
        canvas.drawRRect(
          cell,
          Paint()
            ..color = AppColors.chargeGreen.withValues(
              alpha: 0.12 + 0.3 * pulse,
            ),
        );
      }
      final fill = i < full ? 1.0 : (i == full ? partial : 0.0);
      if (fill > 0) {
        canvas.save();
        canvas.clipRRect(cell);
        canvas.drawRect(
          Rect.fromLTWH(
            cells[i].left,
            cells[i].top,
            cells[i].width * fill,
            cells[i].height,
          ),
          solid,
        );
        canvas.restore();
      }
    }
  }

  @override
  bool shouldRepaint(CarBatteryPainter old) =>
      old.level != level ||
      old.progress != progress ||
      old.charging != charging ||
      old.pixelRatio != pixelRatio;
}
