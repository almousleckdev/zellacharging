import 'package:flutter/material.dart';

abstract final class CarSilhouette {
  static const aspectRatio = 2.0;
  static const _wheelRadius = 0.1;
  static const _rearWheelX = 0.25;
  static const _frontWheelX = 0.77;
  static const _baseline = 0.7;

  static List<Offset> wheelCenters(Size size) => [
    Offset(_rearWheelX * size.width, _baseline * size.height),
    Offset(_frontWheelX * size.width, _baseline * size.height),
  ];

  static double wheelRadius(Size size) => _wheelRadius * size.width * 0.78;

  static Path body(Size size) {
    final w = size.width;
    final h = size.height;
    final arch = Radius.circular(_wheelRadius * w);
    return Path()
      ..moveTo(0.03 * w, _baseline * h)
      ..lineTo(0.03 * w, 0.56 * h)
      ..quadraticBezierTo(0.03 * w, 0.48 * h, 0.1 * w, 0.46 * h)
      ..lineTo(0.22 * w, 0.44 * h)
      ..cubicTo(0.28 * w, 0.2 * h, 0.36 * w, 0.12 * h, 0.46 * w, 0.12 * h)
      ..lineTo(0.6 * w, 0.12 * h)
      ..cubicTo(0.7 * w, 0.12 * h, 0.78 * w, 0.3 * h, 0.84 * w, 0.42 * h)
      ..lineTo(0.93 * w, 0.46 * h)
      ..quadraticBezierTo(0.98 * w, 0.48 * h, 0.98 * w, 0.56 * h)
      ..lineTo(0.98 * w, _baseline * h)
      ..lineTo((_frontWheelX + _wheelRadius) * w, _baseline * h)
      ..arcToPoint(
        Offset((_frontWheelX - _wheelRadius) * w, _baseline * h),
        radius: arch,
        clockwise: false,
      )
      ..lineTo((_rearWheelX + _wheelRadius) * w, _baseline * h)
      ..arcToPoint(
        Offset((_rearWheelX - _wheelRadius) * w, _baseline * h),
        radius: arch,
        clockwise: false,
      )
      ..close();
  }
}
