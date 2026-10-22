import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/empty_state.dart';
import '../../../shared/widgets/filter_chips.dart';
import '../../../shared/widgets/page_header.dart';
import '../../../shared/widgets/section_title.dart';
import '../../charging/domain/charging_session.dart';
import '../../charging/domain/history_summary.dart';
import '../../charging/domain/session_state.dart';
import '../../charging/presentation/history_summary_row.dart';
import '../../charging/presentation/delete_session.dart';
import '../../charging/presentation/session_history_card.dart';
import '../../charging/presentation/session_providers.dart';
import '../../../core/theme/app_icons.dart';

class HistoryPage extends ConsumerStatefulWidget {
  const HistoryPage({super.key});

  @override
  ConsumerState<HistoryPage> createState() => _HistoryPageState();
}

class _HistoryPageState extends ConsumerState<HistoryPage> {
  int _filter = 0;
  final _removed = <String>{};

  bool _matches(ChargingSession session) => switch (_filter) {
    1 => session.state.isLive,
    2 => session.state == SessionState.completed,
    3 =>
      session.state == SessionState.failed ||
          session.state == SessionState.abandoned,
    _ => true,
  };

  @override
  Widget build(BuildContext context) {
    final history = ref.watch(historyProvider);
    return SafeArea(
      child: Column(
        children: [
          PageHeader(title: Strings.history),
          Expanded(
            child: RefreshIndicator(
              onRefresh: () => ref.refresh(historyProvider.future),
              child: AsyncBody(
                value: history,
                onRetry: () => ref.invalidate(historyProvider),
                builder: _content,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _content(List<ChargingSession> all) {
    final visible = all.where((s) => !_removed.contains(s.id)).toList();
    final items = visible.where(_matches).toList();
    return ListView(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.sm),
      children: [
        Padding(
          padding: EdgeInsets.symmetric(horizontal: AppSpacing.md),
          child: SectionTitle(title: HistoryText.chargingHistory),
        ),
        const SizedBox(height: AppSpacing.md),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md),
          child: HistorySummaryRow(summary: HistorySummary.of(visible)),
        ),
        const SizedBox(height: AppSpacing.md),
        FilterChips(
          labels: HistoryText.filters,
          selected: _filter,
          onSelected: (index) => setState(() => _filter = index),
        ),
        const SizedBox(height: AppSpacing.md),
        if (items.isEmpty)
          EmptyState(icon: AppIcons.receipt, message: HistoryText.historyEmpty)
        else
          for (final session in items) _item(session),
      ],
    );
  }

  Widget _item(ChargingSession session) {
    final card = SessionHistoryCard(
      session: session,
      onTap: () => context.push(Routes.chargingPath(session.id)),
    );
    final padding = const EdgeInsets.fromLTRB(
      AppSpacing.md,
      0,
      AppSpacing.md,
      AppSpacing.sm,
    );
    if (session.state.isLive) return Padding(padding: padding, child: card);
    return Padding(
      padding: padding,
      child: Dismissible(
        key: ValueKey(session.id),
        direction: DismissDirection.endToStart,
        confirmDismiss: (_) =>
            confirmAndDeleteSession(context, ref, session.id),
        onDismissed: (_) => setState(() => _removed.add(session.id)),
        background: Container(
          alignment: Alignment.centerRight,
          padding: const EdgeInsets.only(right: AppSpacing.lg),
          decoration: BoxDecoration(
            color: AppColors.danger,
            borderRadius: BorderRadius.circular(AppSpacing.radius),
          ),
          child: Icon(AppIcons.trash, color: AppColors.onPrimary),
        ),
        child: card,
      ),
    );
  }
}
