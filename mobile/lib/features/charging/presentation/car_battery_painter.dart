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

  static const _nubShare = 0.035;
  static const _bars = 10;
  static const _gap = 3.0;
  static const _glass = Color(0xFF14234F);

  Color get _accent => charging ? AppColors.chargeGreen : AppColors.chargeGlow;

  @override
  void paint(Canvas canvas, Size size) {
    final nubWidth = size.width * _nubShare;
    final body = RRect.fromLTRBR(
      0,
      0,
      size.width - nubWidth,
      size.height,
      Radius.circular(size.height * 0.2),
    );
    _drawGlow(canvas, body);
    canvas.drawRRect(body, Paint()..color = _glass);
    _drawBars(canvas, body.deflate(5).outerRect);
    _drawBorder(canvas, body);
    _drawNub(canvas, size, nubWidth);
  }

  void _drawGlow(Canvas canvas, RRect body) {
    canvas.drawRRect(
      body,
      Paint()
        ..color = _accent.withValues(alpha: 0.5)
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 8),
    );
  }

  void _drawBars(Canvas canvas, Rect inner) {
    final barWidth = (inner.width - _gap * (_bars - 1)) / _bars;
    final solid = Paint()
      ..shader = const LinearGradient(
        colors: [AppColors.chargeDeep, AppColors.chargeGreen],
      ).createShader(inner);
    final exact = level * _bars;
    final full = exact.floor();
    final partial = exact - full;
    final next = partial > 0 ? full + 1 : full;
    final chasing = charging ? _chasingBars(next) : 0;
    for (var i = 0; i < _bars; i++) {
      final rect = Rect.fromLTWH(
        inner.left + i * (barWidth + _gap),
        inner.top,
        barWidth,
        inner.height,
      );
      final fill = i < full ? 1.0 : (i == full && partial > 0 ? partial : 0.0);
      final idle = Paint()..color = _idleColor(i - next, chasing);
      _drawBar(canvas, rect, idle);
      if (fill > 0) {
        _drawBar(
          canvas,
          Rect.fromLTWH(rect.left, rect.top, rect.width * fill, rect.height),
          solid,
        );
      }
    }
  }

  void _drawBar(Canvas canvas, Rect rect, Paint paint) => canvas.drawRRect(
    RRect.fromRectAndRadius(rect, const Radius.circular(3)),
    paint,
  );

  int _chasingBars(int next) {
    final remaining = _bars - next;
    return (progress * (remaining + 1)).floor().clamp(0, remaining);
  }

  Color _idleColor(int offset, int chasing) => offset >= 0 && offset < chasing
      ? AppColors.chargeGreen.withValues(alpha: 0.85)
      : Colors.white.withValues(alpha: 0.08);

  void _drawBorder(Canvas canvas, RRect body) {
    canvas.drawRRect(
      body.deflate(1),
      Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2.5
        ..color = _accent,
    );
  }

  void _drawNub(Canvas canvas, Size size, double nubWidth) {
    final nub = RRect.fromLTRBR(
      size.width - nubWidth - 1,
      size.height * 0.3,
      size.width,
      size.height * 0.7,
      Radius.circular(nubWidth * 0.4),
    );
    canvas.drawRRect(nub, Paint()..color = _accent);
  }

  @override
  bool shouldRepaint(CarBatteryPainter old) =>
      old.level != level ||
      old.progress != progress ||
      old.charging != charging;
}
