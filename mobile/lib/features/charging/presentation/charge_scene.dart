import 'package:flutter/material.dart';

import '../../../core/theme/app_spacing.dart';
import '../domain/charging_session.dart';
import '../domain/session_state.dart';
import 'car_battery_painter.dart';
import 'charge_hero.dart';
import 'session_formatting.dart';

const _imageAspect = 955 / 360;
const _slot = Rect.fromLTWH(0.3173, 0.4333, 0.2691, 0.2139);

class ChargeScene extends StatefulWidget {
  const ChargeScene({super.key, required this.session});

  final ChargingSession session;

  @override
  State<ChargeScene> createState() => _ChargeSceneState();
}

class _ChargeSceneState extends State<ChargeScene>
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
  void didUpdateWidget(ChargeScene old) {
    super.didUpdateWidget(old);
    _syncWave();
  }

  void _syncWave() {
    if (widget.session.state == SessionState.active) {
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
    final session = widget.session;
    return Column(
      children: [
        ChargeHero(session: session),
        const SizedBox(height: AppSpacing.xs),
        FractionallySizedBox(
          widthFactor: 0.82,
          child: AspectRatio(
            aspectRatio: _imageAspect,
            child: LayoutBuilder(
              builder: (context, box) => _scene(box, session),
            ),
          ),
        ),
      ],
    );
  }

  Widget _scene(BoxConstraints box, ChargingSession session) {
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
            tween: Tween(end: session.chargeLevel),
            duration: const Duration(milliseconds: 900),
            curve: Curves.easeOutCubic,
            builder: (context, level, _) => AnimatedBuilder(
              animation: _wave,
              builder: (context, _) => CustomPaint(
                painter: CarBatteryPainter(
                  level: level,
                  progress: _wave.value,
                  charging: session.state == SessionState.active,
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
