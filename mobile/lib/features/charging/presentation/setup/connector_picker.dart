import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../stations/domain/station_connector.dart';
import '../../../stations/presentation/connector_status_style.dart';

class ConnectorPicker extends StatelessWidget {
  const ConnectorPicker({
    super.key,
    required this.connectors,
    required this.selected,
    required this.onSelected,
  });

  final List<StationConnector> connectors;
  final int? selected;
  final ValueChanged<int> onSelected;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        for (final connector in connectors) ...[
          _Option(
            connector: connector,
            active: connector.connectorId == selected,
            onTap: connector.available
                ? () => onSelected(connector.connectorId)
                : null,
          ),
          const SizedBox(height: AppSpacing.sm),
        ],
      ],
    );
  }
}

class _Option extends StatelessWidget {
  const _Option({required this.connector, required this.active, this.onTap});

  final StationConnector connector;
  final bool active;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final power = connector.maxPowerKw;
    final enabled = onTap != null;
    return Opacity(
      opacity: enabled ? 1 : 0.55,
      child: InkWell(
        borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.all(AppSpacing.md),
          decoration: BoxDecoration(
            color: active ? AppColors.primarySoft : AppColors.surface,
            borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
            border: Border.all(
              color: active ? AppColors.primary : AppColors.border,
              width: active ? 2 : 1,
            ),
          ),
          child: Row(
            children: [
              Icon(
                active ? Icons.radio_button_checked : Icons.radio_button_off,
                color: active ? AppColors.primary : AppColors.neutral,
              ),
              const SizedBox(width: AppSpacing.md),
              Expanded(
                child: Text(
                  '${Strings.connector} ${connector.connectorId}'
                  '${power == null ? '' : ' · ${power.toStringAsFixed(0)} ${Strings.powerUnit}'}',
                  style: const TextStyle(),
                ),
              ),
              Text(
                ConnectorStatusStyle.label(connector.status),
                style: TextStyle(
                  color: ConnectorStatusStyle.color(connector.status),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
