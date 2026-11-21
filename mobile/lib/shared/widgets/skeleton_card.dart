import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import 'shimmer.dart';

class SkeletonCard extends StatelessWidget {
  const SkeletonCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(AppSpacing.md),
  });

  final Widget child;
  final EdgeInsetsGeometry padding;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppSpacing.radius),
        border: Border.all(color: AppColors.border),
      ),
      child: Padding(
        padding: padding,
        child: Shimmer(child: child),
      ),
    );
  }
}
