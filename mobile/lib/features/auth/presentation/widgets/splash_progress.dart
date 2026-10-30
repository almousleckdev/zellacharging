import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';

class SplashProgress extends StatelessWidget {
  const SplashProgress({super.key, required this.progress});

  final Animation<double> progress;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: progress,
      builder: (context, _) {
        final value = Curves.easeInOut.transform(progress.value);
        return Opacity(
          opacity: (progress.value * 4).clamp(0.0, 1.0),
          child: SizedBox(
            width: 140,
            height: 4,
            child: DecoratedBox(
              decoration: BoxDecoration(
                color: AppColors.onPrimary.withValues(alpha: 0.3),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Align(
                alignment: Alignment.centerLeft,
                child: FractionallySizedBox(
                  widthFactor: value,
                  child: DecoratedBox(
                    decoration: BoxDecoration(
                      color: AppColors.onPrimary,
                      borderRadius: BorderRadius.circular(4),
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
}
