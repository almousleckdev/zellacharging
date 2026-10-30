import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import 'notification_providers.dart';

class NotificationBell extends ConsumerWidget {
  const NotificationBell({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final unread = ref.watch(unreadCountProvider).value ?? 0;
    return Semantics(
      label: unread > 0
          ? '${NotificationText.title}, ${NotificationText.unread(unread)}'
          : NotificationText.title,
      child: IconButton(
        tooltip: NotificationText.title,
        onPressed: () => context.push(Routes.notifications),
        icon: Badge(
          isLabelVisible: unread > 0,
          label: Text(unread > 99 ? '99+' : '$unread'),
          backgroundColor: AppColors.primary,
          textColor: AppColors.onPrimary,
          child: const Icon(
            AppIcons.bell,
            color: AppColors.textPrimary,
            size: 22,
          ),
        ),
      ),
    );
  }
}
