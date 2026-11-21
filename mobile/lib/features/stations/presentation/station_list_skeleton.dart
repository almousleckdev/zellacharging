import 'package:flutter/material.dart';

import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/skeleton_box.dart';
import '../../../shared/widgets/skeleton_card.dart';

class StationListSkeleton extends StatelessWidget {
  const StationListSkeleton({super.key, this.count = 3});

  final int count;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        for (var i = 0; i < count; i++) ...[
          const _StationCardSkeleton(),
          const SizedBox(height: AppSpacing.sm),
        ],
      ],
    );
  }
}

class _StationCardSkeleton extends StatelessWidget {
  const _StationCardSkeleton();

  @override
  Widget build(BuildContext context) {
    return const SkeletonCard(
      child: Row(
        children: [
          SkeletonBox(width: 48, height: 48, radius: AppSpacing.radius),
          SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                SkeletonBox(width: 150, height: 14),
                SizedBox(height: AppSpacing.sm),
                SkeletonBox(height: 10),
                SizedBox(height: AppSpacing.sm),
                SkeletonBox(width: 90, height: 10),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
