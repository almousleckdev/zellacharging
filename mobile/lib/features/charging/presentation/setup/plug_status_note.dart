import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_icons.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../stations/domain/station_connector.dart';

class PlugStatusNote extends StatelessWidget {
  const PlugStatusNote({super.key, required this.connector});

  final StationConnector connector;

  @override
  Widget build(BuildContext context) {
    final plugged = connector.pluggedIn;
    final color = plugged ? AppColors.success : AppColors.warning;
    return Padding(
      padding: const EdgeInsets.only(top: AppSpacing.md),
      child: Row(
        children: [
          Icon(
            plugged ? AppIcons.checkCircle : AppIcons.warning,
            size: 18,
            color: color,
          ),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: Text(
              plugged ? SetupText.cablePlugged : SetupText.plugInFirst,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: color,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
