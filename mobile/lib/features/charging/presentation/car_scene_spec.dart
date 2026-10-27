import 'package:flutter/painting.dart';

class CarSceneSpec {
  const CarSceneSpec({
    required this.asset,
    required this.width,
    required this.height,
    required this.slot,
  });

  final String asset;
  final double width;
  final double height;
  final Rect slot;

  double get aspect => width / height;

  static const current = CarSceneSpec(
    asset: 'assets/images/charging_scene.png',
    width: 1910,
    height: 720,
    slot: Rect.fromLTWH(0.3016, 0.5444, 0.2764, 0.1278),
  );
}
