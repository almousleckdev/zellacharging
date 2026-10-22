import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';

class GradientCard extends StatelessWidget {
  const GradientCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(AppSpacing.lg - 4),
    this.colors = const [AppColors.primary, AppColors.primaryLight],
  });

  final Widget child;
  final EdgeInsetsGeometry padding;
  final List<Color> colors;

  @override
  Widget build(BuildContext context) {
    final radius = BorderRadius.circular(AppSpacing.radiusLarge);
    return Container(
      decoration: BoxDecoration(
        borderRadius: radius,
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: colors,
        ),
        boxShadow: [
          BoxShadow(
            color: colors.first.withValues(alpha: 0.28),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: radius,
        child: Stack(
          children: [
            Positioned(right: -30, top: -40, child: _bubble(130, 0.12)),
            Positioned(right: 40, bottom: -50, child: _bubble(90, 0.1)),
            Padding(padding: padding, child: child),
          ],
        ),
      ),
    );
  }

  Widget _bubble(double size, double opacity) => Container(
    width: size,
    height: size,
    decoration: BoxDecoration(
      shape: BoxShape.circle,
      color: AppColors.onPrimary.withValues(alpha: opacity),
    ),
  );
}
