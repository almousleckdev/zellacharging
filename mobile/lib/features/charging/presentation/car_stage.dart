import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import 'car_scene.dart';

class CarStage extends StatelessWidget {
  const CarStage({super.key, required this.level, required this.charging});

  final double level;
  final bool charging;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.sm,
        vertical: AppSpacing.lg,
      ),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(AppSpacing.radiusLarge),
        gradient: const LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [AppColors.surface, AppColors.surfaceMuted],
        ),
        border: Border.all(color: AppColors.border),
      ),
      child: CarScene(level: level, charging: charging),
    );
  }
}
