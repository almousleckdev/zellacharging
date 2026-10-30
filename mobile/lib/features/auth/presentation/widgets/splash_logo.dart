import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import 'zella_logo.dart';

class SplashLogo extends StatelessWidget {
  const SplashLogo({super.key, required this.progress});

  final Animation<double> progress;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: progress,
      builder: (context, child) {
        final appear = Curves.easeOutBack.transform(
          (progress.value * 2.2).clamp(0.0, 1.0),
        );
        final sweep = ((progress.value - 0.35) / 0.5).clamp(0.0, 1.0);
        return Opacity(
          opacity: (progress.value * 3).clamp(0.0, 1.0),
          child: Transform.scale(
            scale: 0.82 + 0.18 * appear,
            child: DecoratedBox(
              decoration: BoxDecoration(),
              child: ShaderMask(
                blendMode: BlendMode.srcATop,
                shaderCallback: (bounds) => LinearGradient(
                  begin: Alignment(-1.6 + 3.2 * sweep, -0.4),
                  end: Alignment(-0.8 + 3.2 * sweep, 0.4),
                  colors: [
                    AppColors.onPrimary.withValues(alpha: 0),
                    AppColors.onPrimary.withValues(alpha: 0.6),
                    AppColors.onPrimary.withValues(alpha: 0),
                  ],
                ).createShader(bounds),
                child: child,
              ),
            ),
          ),
        );
      },
      child: const ColorFiltered(
        colorFilter: ColorFilter.mode(AppColors.onPrimary, BlendMode.srcIn),
        child: ZellaLogo(height: 40),
      ),
    );
  }
}
