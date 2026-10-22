import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/icon_badge.dart';
import 'notifications_controller.dart';

class NotificationsTile extends ConsumerWidget {
  const NotificationsTile({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final enabled = ref.watch(notificationsProvider).asData?.value ?? true;
    return Padding(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.sm,
      ),
      child: Row(
        children: [
          const IconBadge(icon: AppIcons.bell, size: 36),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(ProfileText.notificationsTitle),
                Text(
                  ProfileText.notificationsBody,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
          Switch.adaptive(
            value: enabled,
            activeTrackColor: AppColors.primary,
            onChanged: (value) =>
                ref.read(notificationsProvider.notifier).set(value),
          ),
        ],
      ),
    );
  }
}
