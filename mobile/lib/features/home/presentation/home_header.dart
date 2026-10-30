import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../notifications/presentation/notification_bell.dart';
import '../../profile/domain/profile.dart';

class HomeHeader extends StatelessWidget {
  const HomeHeader({super.key, this.profile});

  final Profile? profile;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final name = profile?.firstName;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Expanded(
              child: Text(
                name == null ? Strings.appName : '${Strings.greeting}, $name',
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            const NotificationBell(),
          ],
        ),
        Text(
          HomeText.greetingSubtitle,
          style: theme.textTheme.bodySmall?.copyWith(
            color: AppColors.textSecondary,
          ),
        ),
      ],
    );
  }
}
