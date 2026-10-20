import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../domain/charging_session.dart';
import '../domain/session_state.dart';
import 'car_fill_painter.dart';
import 'car_silhouette.dart';
import 'session_formatting.dart';

class ChargeCarGraphic extends StatefulWidget {
  const ChargeCarGraphic({super.key, required this.session});

  final ChargingSession session;

  @override
  State<ChargeCarGraphic> createState() => _ChargeCarGraphicState();
}

class _ChargeCarGraphicState extends State<ChargeCarGraphic>
    with SingleTickerProviderStateMixin {
  late final AnimationController _wave = AnimationController(
    vsync: this,
    duration: const Duration(seconds: 3),
  );

  @override
  void initState() {
    super.initState();
    _syncWave();
  }

  @override
  void didUpdateWidget(ChargeCarGraphic old) {
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
    final theme = Theme.of(context);
    final session = widget.session;
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(session.batteryText, style: theme.textTheme.displaySmall),
            const Padding(
              padding: EdgeInsets.only(bottom: AppSpacing.sm),
              child: Icon(Icons.bolt_rounded, color: AppColors.primary),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.md),
        AspectRatio(
          aspectRatio: CarSilhouette.aspectRatio,
          child: TweenAnimationBuilder<double>(
            tween: Tween(end: session.chargeLevel),
            duration: const Duration(milliseconds: 900),
            curve: Curves.easeOutCubic,
            builder: (context, level, _) => AnimatedBuilder(
              animation: _wave,
              builder: (context, _) => CustomPaint(
                painter: CarFillPainter(
                  level: level,
                  phase: _wave.value * 2 * math.pi,
                ),
              ),
            ),
          ),
        ),
        const SizedBox(height: AppSpacing.sm),
        Text(
          session.state == SessionState.active
              ? Strings.chargingStatus
              : session.stateLabel,
          style: theme.textTheme.bodyMedium?.copyWith(
            color: AppColors.textSecondary,
          ),
        ),
      ],
    );
  }
}
