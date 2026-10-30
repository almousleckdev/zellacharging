import 'package:flutter/material.dart';

import '../../../core/format/date_format.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/icon_badge.dart';
import '../../../shared/widgets/soft_card.dart';
import '../domain/app_notification.dart';
import 'notification_style.dart';

class NotificationTile extends StatelessWidget {
  const NotificationTile({
    super.key,
    required this.notification,
    required this.onTap,
  });

  final AppNotification notification;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SoftCard(
      onTap: onTap,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          IconBadge(
            icon: NotificationStyle.icon(notification.type),
            color: NotificationStyle.color(notification.type),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  notification.title,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    fontWeight: notification.read
                        ? FontWeight.w500
                        : FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  notification.body,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  DateFormatter.dateTime(notification.createdAt),
                  style: theme.textTheme.labelSmall?.copyWith(
                    color: AppColors.neutral,
                  ),
                ),
              ],
            ),
          ),
          if (!notification.read)
            const Padding(
              padding: EdgeInsets.only(left: AppSpacing.sm, top: 4),
              child: Icon(AppIcons.dot, size: 10, color: AppColors.primary),
            ),
        ],
      ),
    );
  }
}
