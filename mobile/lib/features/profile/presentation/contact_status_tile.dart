import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/status_chip.dart';

class ContactStatusTile extends StatelessWidget {
  const ContactStatusTile({
    super.key,
    required this.icon,
    required this.label,
    required this.value,
    required this.verified,
  });

  final IconData icon;
  final String label;
  final String? value;
  final bool verified;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = verified ? AppColors.success : AppColors.warning;
    final status = verified
        ? ContactGateText.verified
        : value == null
        ? ContactGateText.missing
        : ContactGateText.toVerify;
    return Container(
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppSpacing.radius),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        children: [
          Icon(verified ? AppIcons.checkCircle : icon, color: color),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                if (value != null)
                  Text(
                    value!,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: AppColors.textSecondary,
                    ),
                  ),
              ],
            ),
          ),
          StatusChip(label: status, color: color),
        ],
      ),
    );
  }
}
