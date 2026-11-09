import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_icons.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/status_chip.dart';
import '../../../stations/domain/station_connector.dart';
import '../../../stations/presentation/connector_status_style.dart';
import '../../../../core/format/connector_label.dart';

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
    return LayoutBuilder(
      builder: (context, box) {
        final width = (box.maxWidth - AppSpacing.sm) / 2;
        return Wrap(
          spacing: AppSpacing.sm,
          runSpacing: AppSpacing.sm,
          children: [
            for (final connector in connectors)
              SizedBox(
                width: width,
                child: _Option(
                  connector: connector,
                  active: connector.connectorId == selected,
                  onTap: connector.startable
                      ? () => onSelected(connector.connectorId)
                      : null,
                ),
              ),
          ],
        );
      },
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
      ?connector.connectorType,
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
            padding: const EdgeInsets.all(AppSpacing.sm + 2),
            decoration: BoxDecoration(
              color: active ? AppColors.primarySoft : AppColors.surface,
              borderRadius: BorderRadius.circular(AppSpacing.radius),
              border: Border.all(
                color: active ? AppColors.primary : AppColors.border,
                width: active ? 2 : 1,
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        '${StationText.connector} ${ConnectorLabel.of(connector.connectorId)}',
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                    Icon(
                      _icon,
                      size: 18,
                      color: active ? AppColors.primary : AppColors.neutral,
                    ),
                  ],
                ),
                if (_details.isNotEmpty)
                  Text(
                    _details,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: AppColors.textSecondary,
                    ),
                  ),
                const SizedBox(height: AppSpacing.xs + 2),
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
