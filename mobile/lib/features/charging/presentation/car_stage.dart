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
    this.underAppBar = true,
    this.startColor = AppColors.primarySoft,
    this.fadeTo = AppColors.surface,
  });

  final double level;
  final bool charging;
  final Widget? header;
  final bool underAppBar;
  final Color startColor;
  final Color fadeTo;

  @override
  Widget build(BuildContext context) {
    final top = underAppBar
        ? MediaQuery.paddingOf(context).top + kToolbarHeight
        : 0.0;
    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [startColor, fadeTo],
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
