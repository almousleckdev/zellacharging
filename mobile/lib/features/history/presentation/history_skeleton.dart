import 'package:flutter/material.dart';

import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/skeleton_box.dart';
import '../../../shared/widgets/skeleton_card.dart';

class HistorySkeleton extends StatelessWidget {
  const HistorySkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        const Row(
          children: [
            Expanded(child: _MetricSkeleton()),
            SizedBox(width: AppSpacing.sm),
            Expanded(child: _MetricSkeleton()),
            SizedBox(width: AppSpacing.sm),
            Expanded(child: _MetricSkeleton()),
          ],
        ),
        const SizedBox(height: AppSpacing.md),
        for (var i = 0; i < 4; i++) ...[
          const _SessionSkeleton(),
          const SizedBox(height: AppSpacing.sm),
        ],
      ],
    );
  }
}

class _MetricSkeleton extends StatelessWidget {
  const _MetricSkeleton();

  @override
  Widget build(BuildContext context) {
    return const SkeletonCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SkeletonBox(width: 24, height: 24),
          SizedBox(height: AppSpacing.sm),
          SkeletonBox(height: 14),
          SizedBox(height: AppSpacing.xs),
          SkeletonBox(width: 40, height: 10),
        ],
      ),
    );
  }
}

class _SessionSkeleton extends StatelessWidget {
  const _SessionSkeleton();

  @override
  Widget build(BuildContext context) {
    return const SkeletonCard(
      child: Row(
        children: [
          SkeletonBox(width: 44, height: 44, radius: AppSpacing.radius),
          SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                SkeletonBox(width: 140, height: 14),
                SizedBox(height: AppSpacing.sm),
                SkeletonBox(width: 100, height: 10),
              ],
            ),
          ),
          SkeletonBox(width: 56, height: 14),
        ],
      ),
    );
  }
}
