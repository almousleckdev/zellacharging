import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import 'quick_action_tile.dart';

class QuickActions extends StatelessWidget {
  const QuickActions({super.key});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        QuickActionTile(
          icon: AppIcons.scan,
          label: HomeText.scanQr,
          onTap: () => context.push(Routes.scan),
        ),
        const SizedBox(width: AppSpacing.sm),
        QuickActionTile(
          icon: AppIcons.mapPin,
          label: HomeText.map,
          onTap: () => context.go(Routes.locations),
        ),
        const SizedBox(width: AppSpacing.sm),
        QuickActionTile(
          icon: AppIcons.chart,
          label: HomeText.activity,
          onTap: () => context.go(Routes.history),
        ),
      ],
    );
  }
}
