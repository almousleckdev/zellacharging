import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/soft_card.dart';
import 'free_sunday_closing_note.dart';
import 'free_sunday_providers.dart';
import 'free_sunday_time.dart';

class FreeSundayBadge extends ConsumerWidget {
  const FreeSundayBadge({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final offer = ref.watch(freeSundayProvider).value;
    if (offer == null || !offer.available) return const SizedBox.shrink();
    final time = freeSundayEndTime(offer);
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.md),
      child: SoftCard(
        child: Row(
          children: [
            const Icon(AppIcons.lightning, color: AppColors.success),
            const SizedBox(width: AppSpacing.sm),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    time == null
                        ? FreeSundayText.badgeNoTime
                        : FreeSundayText.badge(time),
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: AppColors.success,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const FreeSundayClosingNote(),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
