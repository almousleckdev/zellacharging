import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/section_title.dart';
import '../../charging/presentation/session_history_card.dart';
import '../../charging/presentation/session_providers.dart';

class RecentSessions extends ConsumerWidget {
  const RecentSessions({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessions = ref.watch(historyProvider).asData?.value;
    if (sessions == null || sessions.isEmpty) return const SizedBox.shrink();
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const SizedBox(height: AppSpacing.lg),
        SectionTitle(
          title: HistoryText.recentSessions,
          onViewAll: () => context.go(Routes.history),
        ),
        const SizedBox(height: AppSpacing.md),
        for (final session in sessions.take(3)) ...[
          SessionHistoryCard(
            session: session,
            onTap: () => context.push(Routes.chargingPath(session.id)),
          ),
          const SizedBox(height: AppSpacing.sm),
        ],
      ],
    );
  }
}
