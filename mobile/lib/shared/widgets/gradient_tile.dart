import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';
import '../../core/theme/app_icons.dart';
import '../../core/theme/app_spacing.dart';
import 'gradient_card.dart';

class GradientTile extends StatelessWidget {
  const GradientTile({
    super.key,
    required this.title,
    required this.value,
    required this.icon,
    required this.colors,
    required this.onTap,
  });

  final String title;
  final String value;
  final IconData icon;
  final List<Color> colors;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return GestureDetector(
      onTap: onTap,
      child: GradientCard(
        colors: colors,
        padding: const EdgeInsets.all(AppSpacing.md),
        child: Stack(
          children: [
            Positioned(
              right: 0,
              bottom: 0,
              child: Icon(
                icon,
                size: 40,
                color: AppColors.onPrimary.withValues(alpha: 0.35),
              ),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: AppColors.onPrimary,
                        ),
                      ),
                    ),
                    Icon(
                      AppIcons.caretRight,
                      size: 16,
                      color: AppColors.onPrimary,
                    ),
                  ],
                ),
                const SizedBox(height: AppSpacing.sm),
                SizedBox(
                  height: 32,
                  child: FittedBox(
                    fit: BoxFit.scaleDown,
                    alignment: Alignment.centerLeft,
                    child: Text(
                      value,
                      style: theme.textTheme.headlineSmall?.copyWith(
                        color: AppColors.onPrimary,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: AppSpacing.md),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
