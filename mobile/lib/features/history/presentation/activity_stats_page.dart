import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/empty_state.dart';
import '../../../shared/widgets/filter_chips.dart';
import '../../../shared/widgets/section_title.dart';
import '../../../shared/widgets/soft_card.dart';
import '../../charging/domain/charging_session.dart';
import '../../charging/presentation/session_history_card.dart';
import '../../charging/presentation/session_providers.dart';
import '../domain/activity_stats.dart';
import '../domain/stat_metric.dart';
import '../domain/stat_period.dart';
import '../domain/stat_report.dart';
import 'stat_bar_chart.dart';
import 'stat_summary_tiles.dart';
import 'stat_value_format.dart';

class ActivityStatsPage extends ConsumerStatefulWidget {
  const ActivityStatsPage({super.key, required this.metric});

  final StatMetric metric;

  @override
  ConsumerState<ActivityStatsPage> createState() => _ActivityStatsPageState();
}

class _ActivityStatsPageState extends ConsumerState<ActivityStatsPage> {
  StatPeriod _period = StatPeriod.week;

  String get _title => switch (widget.metric) {
    StatMetric.charges => StatText.chargesTitle,
    StatMetric.energy => StatText.energyTitle,
    StatMetric.spent => StatText.spentTitle,
  };

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(_title)),
      body: AsyncBody(
        value: ref.watch(historyProvider),
        onRetry: () => ref.invalidate(historyProvider),
        builder: _content,
      ),
    );
  }

  Widget _content(List<ChargingSession> sessions) {
    final report = ActivityStats.report(
      sessions,
      widget.metric,
      _period,
      DateTime.now(),
    );
    return ListView(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
      children: [
        FilterChips(
          labels: StatText.periods,
          selected: _period.index,
          onSelected: (index) =>
              setState(() => _period = StatPeriod.values[index]),
        ),
        Padding(
          padding: const EdgeInsets.all(AppSpacing.md),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _hero(report),
              const SizedBox(height: AppSpacing.md),
              SoftCard(child: StatBarChart(report: report)),
              const SizedBox(height: AppSpacing.md),
              StatSummaryTiles(report: report),
              const SizedBox(height: AppSpacing.sm),
              Text(
                StatText.failedNote,
                style: Theme.of(
                  context,
                ).textTheme.bodySmall?.copyWith(color: AppColors.textSecondary),
              ),
              const SizedBox(height: AppSpacing.lg),
              SectionTitle(title: StatText.chargesInPeriod),
              const SizedBox(height: AppSpacing.sm),
              if (report.isEmpty)
                EmptyState(icon: AppIcons.receipt, message: StatText.empty)
              else
                for (final session in report.sessions)
                  Padding(
                    padding: const EdgeInsets.only(bottom: AppSpacing.sm),
                    child: SessionHistoryCard(
                      session: session,
                      onTap: () =>
                          context.push(Routes.chargingPath(session.id)),
                    ),
                  ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _hero(StatReport report) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          StatText.periodCaptions[_period.index],
          style: theme.textTheme.bodyMedium?.copyWith(
            color: AppColors.textSecondary,
          ),
        ),
        const SizedBox(height: AppSpacing.xs),
        Text(
          StatValueFormat.of(report, report.total),
          style: theme.textTheme.headlineMedium?.copyWith(
            fontWeight: FontWeight.w800,
          ),
        ),
      ],
    );
  }
}
