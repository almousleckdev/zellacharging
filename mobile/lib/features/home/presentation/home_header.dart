import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../profile/presentation/user_avatar.dart';
import '../../profile/domain/profile.dart';

class HomeHeader extends StatelessWidget {
  const HomeHeader({super.key, this.profile});

  final Profile? profile;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      children: [
        UserAvatar(initials: profile?.initials ?? '', size: 46),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                profile == null
                    ? Strings.appName
                    : '${Strings.greeting}, ${profile!.firstName}',
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w800,
                ),
              ),
              Text(
                Strings.tagline,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
