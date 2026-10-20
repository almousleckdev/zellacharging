import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import 'icon_badge.dart';

class MenuTile extends StatelessWidget {
  const MenuTile({
    super.key,
    required this.icon,
    required this.label,
    this.trailingText,
    this.trailing,
    this.onTap,
    this.color = AppColors.textPrimary,
  });

  final IconData icon;
  final String label;
  final String? trailingText;
  final Widget? trailing;
  final VoidCallback? onTap;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.md,
          vertical: AppSpacing.sm + 2,
        ),
        child: Row(
          children: [
            IconBadge(
              icon: icon,
              size: 36,
              color: color == AppColors.textPrimary ? AppColors.primary : color,
            ),
            const SizedBox(width: AppSpacing.md),
            Expanded(
              child: Text(label, style: TextStyle(color: color)),
            ),
            if (trailingText != null)
              Text(
                trailingText!,
                style: const TextStyle(color: AppColors.textSecondary),
              ),
            if (trailing != null)
              trailing!
            else
              const Icon(Icons.chevron_right, color: AppColors.neutral),
          ],
        ),
      ),
    );
  }
}
