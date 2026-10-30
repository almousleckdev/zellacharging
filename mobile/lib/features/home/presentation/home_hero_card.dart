import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';

class HomeHeroCard extends StatelessWidget {
  const HomeHeroCard({super.key, required this.onFindStation});

  final VoidCallback onFindStation;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return ClipRRect(
      borderRadius: BorderRadius.circular(AppSpacing.radius),
      child: AspectRatio(
        aspectRatio: 1024 / 454,
        child: Stack(
          fit: StackFit.expand,
          children: [
            Image.asset('assets/images/mainscreencard.png', fit: BoxFit.cover),
            Padding(
              padding: const EdgeInsets.all(AppSpacing.md),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Flexible(
                    child: FractionallySizedBox(
                      widthFactor: 0.55,
                      child: Text(
                        HomeText.heroTitle,
                        maxLines: 3,
                        overflow: TextOverflow.ellipsis,
                        style: theme.textTheme.titleSmall?.copyWith(
                          color: AppColors.onPrimary,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ),
                  SizedBox(
                    height: 32,
                    child: FilledButton(
                      onPressed: onFindStation,
                      style: FilledButton.styleFrom(
                        padding: const EdgeInsets.symmetric(
                          horizontal: AppSpacing.md,
                        ),
                        textStyle: const TextStyle(fontSize: 12),
                      ),
                      child: Text(HomeText.findStation),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
