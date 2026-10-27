import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../domain/stat_report.dart';
import 'stat_labels.dart';
import 'stat_value_format.dart';

class StatBarChart extends StatefulWidget {
  const StatBarChart({super.key, required this.report});

  final StatReport report;

  @override
  State<StatBarChart> createState() => _StatBarChartState();
}

class _StatBarChartState extends State<StatBarChart> {
  static const _height = 150.0;
  int? _selected;

  @override
  void didUpdateWidget(StatBarChart old) {
    super.didUpdateWidget(old);
    if (old.report.period != widget.report.period ||
        old.report.metric != widget.report.metric) {
      _selected = null;
    }
  }

  @override
  Widget build(BuildContext context) {
    final report = widget.report;
    final theme = Theme.of(context);
    final peak = report.peak;
    final shown = _selected;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        SizedBox(
          height: 36,
          child: shown == null
              ? null
              : Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      StatValueFormat.of(report, report.buckets[shown].value),
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    Text(
                      StatLabels.caption(
                        report.period,
                        report.buckets[shown].start,
                      ),
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
        ),
        SizedBox(
          height: _height,
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              for (var i = 0; i < report.buckets.length; i++)
                Expanded(child: _bar(i, peak)),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.xs),
        Row(
          children: [
            for (var i = 0; i < report.buckets.length; i++)
              Expanded(
                child: Text(
                  i % StatLabels.step(report.period) == 0
                      ? StatLabels.bucket(
                          report.period,
                          report.buckets[i].start,
                        )
                      : '',
                  textAlign: TextAlign.center,
                  maxLines: 1,
                  overflow: TextOverflow.visible,
                  softWrap: false,
                  style: theme.textTheme.labelSmall?.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
              ),
          ],
        ),
      ],
    );
  }

  Widget _bar(int index, double peak) {
    final value = widget.report.buckets[index].value;
    final active = _selected == null
        ? value == peak && peak > 0
        : _selected == index;
    final fraction = peak == 0 ? 0.0 : value / peak;
    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () =>
          setState(() => _selected = _selected == index ? null : index),
      child: Align(
        alignment: Alignment.bottomCenter,
        child: FractionallySizedBox(
          widthFactor: 0.6,
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 220),
            height: value == 0 ? 3 : (_height * fraction).clamp(6.0, _height),
            decoration: BoxDecoration(
              color: active
                  ? AppColors.primary
                  : AppColors.primary.withValues(
                      alpha: value == 0 ? 0.12 : 0.35,
                    ),
              borderRadius: BorderRadius.circular(4),
            ),
          ),
        ),
      ),
    );
  }
}
