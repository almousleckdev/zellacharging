import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/status_chip.dart';
import '../domain/station_connector.dart';
import 'connector_status_style.dart';

class ConnectorTile extends StatelessWidget {
  const ConnectorTile({
    super.key,
    required this.connector,
    this.highlighted = false,
    this.onStart,
  });

  final StationConnector connector;
  final bool highlighted;
  final VoidCallback? onStart;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final power = connector.maxPowerKw;
    return Container(
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppSpacing.radius),
        border: Border.all(
          color: highlighted ? AppColors.primary : AppColors.border,
          width: highlighted ? 2 : 1,
        ),
      ),
      child: Row(
        children: [
          CircleAvatar(
            backgroundColor: AppColors.primarySoft,
            child: Text(
              '${connector.connectorId}',
              style: const TextStyle(color: AppColors.primary),
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  connector.connectorType ?? StationText.connector,
                  style: theme.textTheme.titleSmall,
                ),
                if (power != null)
                  Text(
                    '${StationText.maxPower}: ${power.toStringAsFixed(0)} ${Strings.powerUnit}',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: AppColors.textSecondary,
                    ),
                  ),
              ],
            ),
          ),
          if (connector.available && onStart != null)
            FilledButton(
              onPressed: onStart,
              child: Text(StationText.startCharging),
            )
          else
            StatusChip(
              label: ConnectorStatusStyle.label(connector.status),
              color: ConnectorStatusStyle.color(connector.status),
            ),
        ],
      ),
    );
  }
}
