import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import 'free_sunday_providers.dart';
import 'free_sunday_time.dart';

class FreeSundayClosingNote extends ConsumerWidget {
  const FreeSundayClosingNote({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final offer = ref.watch(freeSundayProvider).value;
    final now = ref.watch(freeSundayClockProvider)();
    if (offer == null || !offer.closesSoon(now)) {
      return const SizedBox.shrink();
    }
    return Padding(
      padding: const EdgeInsets.only(top: 2),
      child: Text(
        FreeSundayText.closingSoon(freeSundayEndTime(offer)!),
        style: Theme.of(context).textTheme.bodySmall?.copyWith(
          color: AppColors.warning,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
