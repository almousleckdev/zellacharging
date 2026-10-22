import 'package:flutter/material.dart';

import 'car_battery_painter.dart';

const _imageAspect = 955 / 360;
const _slot = Rect.fromLTWH(0.3173, 0.4333, 0.2691, 0.2139);

class CarScene extends StatefulWidget {
  const CarScene({super.key, required this.level, required this.charging});

  final double level;
  final bool charging;

  @override
  State<CarScene> createState() => _CarSceneState();
}

class _CarSceneState extends State<CarScene>
    with SingleTickerProviderStateMixin {
  late final AnimationController _wave = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 2600),
  );

  @override
  void initState() {
    super.initState();
    _syncWave();
  }

  @override
  void didUpdateWidget(CarScene old) {
    super.didUpdateWidget(old);
    _syncWave();
  }

  void _syncWave() {
    if (widget.charging) {
      if (!_wave.isAnimating) _wave.repeat();
    } else {
      _wave.stop();
    }
  }

  @override
  void dispose() {
    _wave.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AspectRatio(
      aspectRatio: _imageAspect,
      child: LayoutBuilder(builder: (context, box) => _scene(box)),
    );
  }

  Widget _scene(BoxConstraints box) {
    final slot = Rect.fromLTWH(
      box.maxWidth * _slot.left,
      box.maxHeight * _slot.top,
      box.maxWidth * _slot.width,
      box.maxHeight * _slot.height,
    );
    return Stack(
      children: [
        Positioned.fill(
          child: Image.asset(
            'assets/images/charging_scene.png',
            fit: BoxFit.fill,
          ),
        ),
        Positioned.fromRect(
          rect: slot,
          child: TweenAnimationBuilder<double>(
            tween: Tween(end: widget.level),
            duration: const Duration(milliseconds: 900),
            curve: Curves.easeOutCubic,
            builder: (context, level, _) => AnimatedBuilder(
              animation: _wave,
              builder: (context, _) => CustomPaint(
                painter: CarBatteryPainter(
                  level: level,
                  progress: _wave.value,
                  charging: widget.charging,
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
