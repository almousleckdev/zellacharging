import 'package:flutter/material.dart';

import '../../../core/format/date_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/icon_badge.dart';
import '../../../shared/widgets/soft_card.dart';
import '../domain/coupon.dart';

class CouponCard extends StatelessWidget {
  const CouponCard({super.key, required this.coupon});

  final Coupon coupon;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final muted = theme.textTheme.bodySmall?.copyWith(
      color: AppColors.textSecondary,
    );
    return SoftCard(
      child: Row(
        children: [
          const IconBadge(icon: AppIcons.ticket, size: 46),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  coupon.title,
                  style: theme.textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.w800,
                  ),
                ),
                if (coupon.description != null)
                  Text(coupon.description!, style: muted),
                if (coupon.expiresAt != null)
                  Padding(
                    padding: const EdgeInsets.only(top: AppSpacing.xs),
                    child: Text(
                      ProfileText.couponValidUntil(
                        DateFormatter.date(coupon.expiresAt!),
                      ),
                      style: muted,
                    ),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
