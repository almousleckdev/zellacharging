import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';

const _barWidth = 140.0;
const _flashPeriod = Duration(milliseconds: 1100);

class SplashProgress extends StatefulWidget {
  const SplashProgress({super.key, required this.progress});

  final Animation<double> progress;

  @override
  State<SplashProgress> createState() => _SplashProgressState();
}

class _SplashProgressState extends State<SplashProgress>
    with SingleTickerProviderStateMixin {
  late final AnimationController _flash = AnimationController(
    vsync: this,
    duration: _flashPeriod,
  )..repeat();

  @override
  void dispose() {
    _flash.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: Listenable.merge([widget.progress, _flash]),
      builder: (context, _) {
        final value = Curves.easeInOut.transform(widget.progress.value);
        final glow = 0.5 + 0.5 * math.sin(_flash.value * 2 * math.pi);
        return Opacity(
          opacity: (widget.progress.value * 4).clamp(0.0, 1.0),
          child: SizedBox(
            width: _barWidth,
            height: 6,
            child: DecoratedBox(
              decoration: BoxDecoration(
                color: AppColors.onPrimary.withValues(alpha: 0.3),
                borderRadius: BorderRadius.circular(6),
              ),
              child: Align(
                alignment: Alignment.centerLeft,
                child: FractionallySizedBox(
                  widthFactor: value,
                  heightFactor: 1,
                  child: DecoratedBox(
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(6),
                      gradient: _sweep(_flash.value),
                      boxShadow: [
                        BoxShadow(
                          color: AppColors.onPrimary.withValues(
                            alpha: 0.35 + 0.35 * glow,
                          ),
                          blurRadius: 4 + 8 * glow,
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  LinearGradient _sweep(double t) {
    final center = -0.3 + 1.6 * t;
    return LinearGradient(
      colors: [
        AppColors.onPrimary.withValues(alpha: 0.7),
        AppColors.onPrimary,
        AppColors.onPrimary.withValues(alpha: 0.7),
      ],
      stops: [
        (center - 0.3).clamp(0.0, 1.0),
        center.clamp(0.0, 1.0),
        (center + 0.3).clamp(0.0, 1.0),
      ],
    );
  }
}
