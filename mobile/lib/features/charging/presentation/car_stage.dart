import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import 'car_scene.dart';

class CarStage extends StatelessWidget {
  const CarStage({
    super.key,
    required this.level,
    required this.charging,
    this.header,
    this.fadeTo = AppColors.surface,
  });

  final double level;
  final bool charging;
  final Widget? header;
  final Color fadeTo;

  @override
  Widget build(BuildContext context) {
    final top = MediaQuery.paddingOf(context).top + kToolbarHeight;
    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [AppColors.primarySoft, fadeTo],
        ),
      ),
      child: Padding(
        padding: EdgeInsets.only(top: top, bottom: AppSpacing.lg),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ?header,
            CarScene(level: level, charging: charging),
          ],
        ),
      ),
    );
  }
}
