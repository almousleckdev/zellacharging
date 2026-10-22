import 'package:flutter/material.dart';

import '../../../core/format/duration_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../domain/charging_session.dart';
import '../../../core/theme/app_icons.dart';

class BatteryFreshness extends StatelessWidget {
  const BatteryFreshness({super.key, required this.session});

  final ChargingSession session;

  static const _staleAfter = Duration(seconds: 90);

  @override
  Widget build(BuildContext context) {
    final sampled = session.metering?.sampledAt;
    if (!session.state.isLive || sampled == null) {
      return const SizedBox.shrink();
    }
    final age = DateTime.now().difference(sampled);
    final stale = age > _staleAfter;
    final ageText = DurationFormat.compact(age.inSeconds.clamp(0, 86400));
    final color = stale ? AppColors.primary : AppColors.chargeDeep;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(AppIcons.dot, size: 8, color: color),
        const SizedBox(width: AppSpacing.xs + 2),
        Text(
          stale
              ? ChargeText.lastReadingAgo(ageText)
              : ChargeText.updatedAgo(ageText),
          style: Theme.of(context).textTheme.bodySmall?.copyWith(color: color),
        ),
      ],
    );
  }
}
