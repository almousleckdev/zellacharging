import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/empty_state.dart';
import '../../free_sunday/presentation/free_sunday_card.dart';
import '../../free_sunday/presentation/free_sunday_providers.dart';
import '../domain/coupon.dart';
import 'coupon_card.dart';

class CouponsList extends ConsumerWidget {
  const CouponsList({super.key, required this.coupons});

  final List<Coupon> coupons;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final offer = ref.watch(freeSundayProvider).value;
    final hasOffer = offer != null && offer.inProgram;
    if (coupons.isEmpty && !hasOffer) {
      return EmptyState(
        icon: AppIcons.ticket,
        message: ProfileText.couponsEmpty,
      );
    }
    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        const FreeSundayCard(),
        for (final coupon in coupons) ...[
          CouponCard(coupon: coupon),
          const SizedBox(height: AppSpacing.sm),
        ],
      ],
    );
  }
}
