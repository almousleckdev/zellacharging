import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import 'car_silhouette.dart';

class CarFillPainter extends CustomPainter {
  const CarFillPainter({required this.level, required this.phase});

  final double level;
  final double phase;

  @override
  void paint(Canvas canvas, Size size) {
    final body = CarSilhouette.body(size);
    canvas.drawPath(body, Paint()..color = AppColors.primarySoft);
    canvas.save();
    canvas.clipPath(body);
    _drawWave(canvas, size, phase + math.pi, 0.35);
    _drawWave(canvas, size, phase, 1);
    canvas.restore();
    _drawOutline(canvas, body);
    _drawWheels(canvas, size);
  }

  void _drawWave(Canvas canvas, Size size, double shift, double opacity) {
    final top = size.height * (1 - level);
    final amplitude = size.height * 0.035 * math.sin(level * math.pi);
    final path = Path()..moveTo(0, size.height);
    for (var x = 0.0; x <= size.width; x += 4) {
      final y = top + amplitude * math.sin(shift + x / size.width * 5);
      path.lineTo(x, y);
    }
    path
      ..lineTo(size.width, size.height)
      ..close();
    final paint = Paint()
      ..shader = const LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [AppColors.primaryLight, AppColors.primary],
      ).createShader(Offset.zero & size);
    canvas.drawPath(
      path,
      paint..color = Colors.white.withValues(alpha: opacity),
    );
  }

  void _drawOutline(Canvas canvas, Path body) {
    canvas.drawPath(
      body,
      Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = 7
        ..color = AppColors.primary.withValues(alpha: 0.35)
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 6),
    );
    canvas.drawPath(
      body,
      Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2.5
        ..strokeJoin = StrokeJoin.round
        ..color = AppColors.primary,
    );
  }

  void _drawWheels(Canvas canvas, Size size) {
    final radius = CarSilhouette.wheelRadius(size);
    for (final center in CarSilhouette.wheelCenters(size)) {
      canvas.drawCircle(center, radius, Paint()..color = AppColors.textPrimary);
      canvas.drawCircle(center, radius * 0.45, Paint()..color = Colors.white);
    }
  }

  @override
  bool shouldRepaint(CarFillPainter old) =>
      old.level != level || old.phase != phase;
}
