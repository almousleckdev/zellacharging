import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_icons.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/status_chip.dart';
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
            onTap: connector.startable
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

  String get _details {
    final power = connector.maxPowerKw;
    return [
      if (power != null) '${power.toStringAsFixed(0)} ${Strings.powerUnit}',
      if (connector.connectorType != null) connector.connectorType!,
    ].join(' · ');
  }

  IconData get _icon {
    if (!connector.startable) return AppIcons.lock;
    return active ? AppIcons.checkCircle : AppIcons.circle;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final enabled = onTap != null;
    return Semantics(
      button: true,
      enabled: enabled,
      selected: active,
      child: Opacity(
        opacity: enabled ? 1 : 0.65,
        child: InkWell(
          borderRadius: BorderRadius.circular(AppSpacing.radius),
          onTap: onTap,
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 160),
            padding: const EdgeInsets.all(AppSpacing.md),
            decoration: BoxDecoration(
              color: active ? AppColors.primarySoft : AppColors.surface,
              borderRadius: BorderRadius.circular(AppSpacing.radius),
              border: Border.all(
                color: active ? AppColors.primary : AppColors.border,
                width: active ? 2 : 1,
              ),
            ),
            child: Row(
              children: [
                Icon(
                  _icon,
                  color: active ? AppColors.primary : AppColors.neutral,
                ),
                const SizedBox(width: AppSpacing.md),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '${StationText.connector} ${connector.connectorId}',
                        style: theme.textTheme.titleSmall?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      if (_details.isNotEmpty)
                        Text(
                          _details,
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: AppColors.textSecondary,
                          ),
                        ),
                    ],
                  ),
                ),
                const SizedBox(width: AppSpacing.sm),
                StatusChip(
                  label: ConnectorStatusStyle.label(connector.status),
                  color: ConnectorStatusStyle.color(connector.status),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
