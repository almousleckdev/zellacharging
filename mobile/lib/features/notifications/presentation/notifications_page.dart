import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/notifications/push_message.dart';
import '../../../core/notifications/push_route.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/empty_state.dart';
import '../../../shared/widgets/section_label.dart';
import '../domain/app_notification.dart';
import 'notification_groups.dart';
import 'notification_providers.dart';
import 'notification_tile.dart';

class NotificationsPage extends ConsumerWidget {
  const NotificationsPage({super.key});

  void _reload(WidgetRef ref) {
    ref.invalidate(notificationFeedProvider);
    ref.invalidate(unreadCountProvider);
  }

  Future<void> _markAllRead(WidgetRef ref) async {
    await ref.read(notificationRepositoryProvider).markAllRead();
    _reload(ref);
  }

  Future<void> _open(
    BuildContext context,
    WidgetRef ref,
    AppNotification notification,
  ) async {
    final route = routeFor(
      PushMessage(
        title: notification.title,
        body: notification.body,
        data: {
          'screen': notification.screen ?? '',
          'id': notification.entityId ?? '',
        },
      ),
    );
    if (!notification.read) {
      await ref.read(notificationRepositoryProvider).markRead(notification.id);
      _reload(ref);
    }
    if (context.mounted && route != null) context.push(route);
  }

  Widget _groupCard(
    BuildContext context,
    WidgetRef ref,
    NotificationGroup group,
  ) {
    return DecoratedBox(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(AppSpacing.radius),
        border: Border.all(color: AppColors.border),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(AppSpacing.radius),
        child: Column(
          children: [
            for (var i = 0; i < group.items.length; i++) ...[
              if (i > 0) const Divider(height: 1, color: AppColors.border),
              NotificationTile(
                notification: group.items[i],
                onTap: () => _open(context, ref, group.items[i]),
              ),
            ],
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final feed = ref.watch(notificationFeedProvider);
    final hasUnread = (feed.value?.unread ?? 0) > 0;
    return Scaffold(
      appBar: AppBar(
        title: Text(NotificationText.title),
        actions: [
          if (hasUnread)
            TextButton(
              onPressed: () => _markAllRead(ref),
              child: Text(NotificationText.markAllRead),
            ),
        ],
      ),
      body: AsyncBody(
        value: feed,
        onRetry: () => _reload(ref),
        builder: (data) => RefreshIndicator.adaptive(
          onRefresh: () async => _reload(ref),
          child: data.items.isEmpty
              ? ListView(
                  children: [
                    const SizedBox(height: AppSpacing.xl),
                    EmptyState(
                      icon: AppIcons.bell,
                      message: NotificationText.empty,
                    ),
                  ],
                )
              : ListView(
                  padding: const EdgeInsets.fromLTRB(
                    AppSpacing.md,
                    0,
                    AppSpacing.md,
                    AppSpacing.lg,
                  ),
                  children: [
                    for (final group in NotificationGroups.of(
                      data.items,
                      DateTime.now(),
                    )) ...[
                      SectionLabel(group.label),
                      _groupCard(context, ref, group),
                    ],
                  ],
                ),
        ),
      ),
    );
  }
}
