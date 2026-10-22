import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/soft_card.dart';
import 'session_providers.dart';
import '../../../core/theme/app_icons.dart';

class ActiveSessionBanner extends ConsumerWidget {
  const ActiveSessionBanner({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final session = ref.watch(activeSessionProvider).asData?.value;
    if (session == null) return const SizedBox.shrink();
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.md),
      child: SoftCard(
        onTap: () => context.push(Routes.chargingPath(session.id)),
        child: Row(
          children: [
            const Icon(AppIcons.lightning, color: AppColors.primary),
            const SizedBox(width: AppSpacing.sm),
            Expanded(
              child: Text(
                '${ChargeText.activeSession} · ${session.stationId}',
                style: const TextStyle(),
              ),
            ),
            Text(
              ChargeText.viewSession,
              style: TextStyle(color: AppColors.primary),
            ),
          ],
        ),
      ),
    );
  }
}
