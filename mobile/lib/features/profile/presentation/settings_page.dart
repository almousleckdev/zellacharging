import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/menu_tile.dart';
import '../../../shared/widgets/section_label.dart';
import '../../../shared/widgets/soft_card.dart';
import 'notifications_tile.dart';

class SettingsPage extends ConsumerWidget {
  const SettingsPage({super.key});

  Widget _card(List<Widget> children) => SoftCard(
    padding: const EdgeInsets.symmetric(vertical: AppSpacing.xs),
    child: Column(children: children),
  );

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      backgroundColor: AppColors.surfaceMuted,
      appBar: AppBar(title: Text(ProfileText.settingsTitle)),
      body: ListView(
        padding: const EdgeInsets.all(AppSpacing.md),
        children: [
          SectionLabel(ProfileText.notificationsTitle),
          _card(const [NotificationsTile()]),
          SectionLabel(ProfileText.securitySection),
          _card([
            MenuTile(
              icon: AppIcons.key,
              label: ProfileText.changePassword,
              onTap: () => context.push(Routes.changePassword),
            ),
          ]),
          SectionLabel(ProfileText.dangerSection),
          _card([
            MenuTile(
              icon: AppIcons.trash,
              label: ProfileText.deleteAccount,
              color: AppColors.danger,
              onTap: () => context.push(Routes.deleteAccount),
            ),
          ]),
        ],
      ),
    );
  }
}
