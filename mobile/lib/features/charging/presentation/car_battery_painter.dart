import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';

class CarBatteryPainter extends CustomPainter {
  const CarBatteryPainter({
    required this.level,
    required this.progress,
    required this.charging,
  });

  final double level;
  final double progress;
  final bool charging;

  static const _cells = 14;
  static const _gap = 2.4;
  static const _inset = 4.0;

  Color get _accent => charging ? AppColors.chargeGreen : AppColors.chargeGlow;

  @override
  void paint(Canvas canvas, Size size) {
    final tray = RRect.fromRectAndRadius(
      Offset.zero & size,
      Radius.circular(size.height * 0.3),
    );
    _drawGlow(canvas, tray);
    _drawTray(canvas, tray, size);
    canvas.save();
    canvas.clipRRect(tray.deflate(_inset - 1));
    _drawCells(canvas, tray.deflate(_inset).outerRect);
    canvas.restore();
    _drawGloss(canvas, tray, size);
    _drawBorder(canvas, tray);
  }

  void _drawGlow(Canvas canvas, RRect tray) {
    canvas.drawRRect(
      tray,
      Paint()
        ..color = _accent.withValues(alpha: charging ? 0.55 : 0.35)
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 9),
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

  void _drawCells(Canvas canvas, Rect inner) {
    final width = (inner.width - _gap * (_cells - 1)) / _cells;
    final solid = Paint()
      ..shader = const LinearGradient(
        colors: [AppColors.chargeDeep, AppColors.chargeGreen],
      ).createShader(inner);
    final exact = level * _cells;
    final full = exact.floor();
    final partial = exact - full;
    final next = partial > 0 ? full + 1 : full;
    final chasing = charging ? _chasingCells(next) : 0;
    for (var i = 0; i < _cells; i++) {
      final rect = Rect.fromLTWH(
        inner.left + i * (width + _gap),
        inner.top,
        width,
        inner.height,
      );
      final cell = RRect.fromRectAndRadius(rect, const Radius.circular(2.5));
      canvas.drawRRect(cell, Paint()..color = _idle(i - next, chasing));
      final fill = i < full ? 1.0 : (i == full && partial > 0 ? partial : 0.0);
      if (fill > 0) {
        canvas.save();
        canvas.clipRect(
          Rect.fromLTWH(rect.left, rect.top, rect.width * fill, rect.height),
        );
        canvas.drawRRect(cell, solid);
        canvas.restore();
      }
    }
  }

  int _chasingCells(int next) {
    final remaining = _cells - next;
    return (progress * (remaining + 1)).floor().clamp(0, remaining);
  }

  Color _idle(int offset, int chasing) => offset >= 0 && offset < chasing
      ? AppColors.chargeGreen.withValues(alpha: 0.85)
      : AppColors.onPrimary.withValues(alpha: 0.08);

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
            AppColors.onPrimary.withValues(alpha: 0.22),
            AppColors.onPrimary.withValues(alpha: 0),
          ],
        ).createShader(upper),
    );
    canvas.restore();
  }

  void _drawBorder(Canvas canvas, RRect tray) {
    canvas.drawRRect(
      tray.deflate(0.9),
      Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.8
        ..color = _accent,
    );
  }

  @override
  bool shouldRepaint(CarBatteryPainter old) =>
      old.level != level ||
      old.progress != progress ||
      old.charging != charging;
}
