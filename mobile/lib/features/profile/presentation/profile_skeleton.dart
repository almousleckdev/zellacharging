import 'package:flutter/material.dart';

import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/skeleton_box.dart';
import '../../../shared/widgets/skeleton_card.dart';

class ProfileSkeleton extends StatelessWidget {
  const ProfileSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      physics: const NeverScrollableScrollPhysics(),
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        const SkeletonCard(
          child: Row(
            children: [
              SkeletonBox.circle(size: 64),
              SizedBox(width: AppSpacing.md),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    SkeletonBox(width: 150, height: 16),
                    SizedBox(height: AppSpacing.sm),
                    SkeletonBox(width: 190, height: 10),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        for (var i = 0; i < 5; i++) ...[
          const _MenuRowSkeleton(),
          const SizedBox(height: AppSpacing.sm),
        ],
      ],
    );
  }
}

class _MenuRowSkeleton extends StatelessWidget {
  const _MenuRowSkeleton();

  @override
  Widget build(BuildContext context) {
    return const SkeletonCard(
      child: Row(
        children: [
          SkeletonBox(width: 36, height: 36, radius: AppSpacing.radiusSmall),
          SizedBox(width: AppSpacing.md),
          SkeletonBox(width: 160, height: 12),
        ],
      ),
    );
  }
}
