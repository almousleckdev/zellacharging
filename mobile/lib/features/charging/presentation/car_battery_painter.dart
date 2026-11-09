import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';

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
    _drawGlow(canvas, tray);
    _drawTray(canvas, tray, size);
    final inner = tray.deflate(_inset).outerRect;
    final cells = _layout(inner);
    final exact = level.clamp(0.0, 1.0) * _cells;
    _drawCells(canvas, inner, cells, exact);
    if (charging) _drawSweep(canvas, cells, exact);
    _drawGloss(canvas, tray, size);
    _drawBorder(canvas, tray);
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
    final pulse = 0.5 - 0.5 * math.cos(progress * 2 * math.pi);
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

  void _drawSweep(Canvas canvas, List<Rect> cells, double exact) {
    final full = exact.floor();
    final partial = exact - full;
    final filled = Path();
    for (var i = 0; i < math.min(full, _cells); i++) {
      filled.addRRect(RRect.fromRectAndRadius(cells[i], _cellRadius));
    }
    if (full < _cells && partial > 0) {
      final c = cells[full];
      filled.addRect(Rect.fromLTWH(c.left, c.top, c.width * partial, c.height));
    }
    if (full == 0 && partial == 0) return;
    final right = full >= _cells
        ? cells.last.right
        : cells[full].left + cells[full].width * partial;
    final band = cells.first.width * 3;
    final centre =
        cells.first.left -
        band +
        (right - cells.first.left + 2 * band) * progress;
    final stripe = Rect.fromLTRB(
      centre - band,
      cells.first.top,
      centre + band,
      cells.first.bottom,
    );
    canvas.save();
    canvas.clipPath(filled);
    canvas.drawRect(
      stripe,
      Paint()
        ..shader = LinearGradient(
          colors: [
            AppColors.onPrimary.withValues(alpha: 0),
            AppColors.onPrimary.withValues(alpha: 0.3),
            AppColors.onPrimary.withValues(alpha: 0),
          ],
        ).createShader(stripe),
    );
    canvas.restore();
  }

  void _drawGlow(Canvas canvas, RRect tray) {
    canvas.drawRRect(
      tray,
      Paint()
        ..color = _accent.withValues(alpha: charging ? 0.32 : 0.2)
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 7),
    );
  }

  void _drawTray(Canvas canvas, RRect tray, Size size) {
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

  void _drawGloss(Canvas canvas, RRect tray, Size size) {
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

  void _drawBorder(Canvas canvas, RRect tray) {
    canvas.drawRRect(
      tray.deflate(0.7),
      Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.4
        ..color = _accent,
    );
  }

  @override
  bool shouldRepaint(CarBatteryPainter old) =>
      old.level != level ||
      old.progress != progress ||
      old.charging != charging ||
      old.pixelRatio != pixelRatio;
}
