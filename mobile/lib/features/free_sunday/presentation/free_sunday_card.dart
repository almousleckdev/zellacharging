import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/icon_badge.dart';
import '../../../shared/widgets/soft_card.dart';
import '../domain/free_sunday_offer.dart';
import 'free_sunday_closing_note.dart';
import 'free_sunday_providers.dart';
import 'free_sunday_time.dart';

class FreeSundayCard extends ConsumerWidget {
  const FreeSundayCard({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final offer = ref.watch(freeSundayProvider).value;
    if (offer == null || !offer.inProgram) return const SizedBox.shrink();
    final theme = Theme.of(context);
    final muted = theme.textTheme.bodySmall?.copyWith(
      color: AppColors.textSecondary,
    );
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.sm),
      child: SoftCard(
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            IconBadge(
              icon: AppIcons.lightning,
              color: offer.available ? AppColors.success : AppColors.neutral,
              size: 46,
            ),
            const SizedBox(width: AppSpacing.md),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    FreeSundayText.title,
                    style: theme.textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  Text(
                    _state(offer),
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: offer.available
                          ? AppColors.success
                          : AppColors.textSecondary,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const FreeSundayClosingNote(),
                  const SizedBox(height: AppSpacing.xs),
                  Text(
                    FreeSundayText.explanationOf(offer.window),
                    style: muted,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _state(FreeSundayOffer offer) {
    if (!offer.available) {
      return FreeSundayText.reasonOf(offer.reason, window: offer.window);
    }
    final time = freeSundayEndTime(offer);
    return time == null
        ? FreeSundayText.availableNow
        : FreeSundayText.availableUntil(time);
  }
}
