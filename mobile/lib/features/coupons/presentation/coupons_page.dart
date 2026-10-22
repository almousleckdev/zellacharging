import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/empty_state.dart';
import 'coupon_card.dart';
import 'coupon_providers.dart';

class CouponsPage extends ConsumerWidget {
  const CouponsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final coupons = ref.watch(couponsProvider);
    return Scaffold(
      backgroundColor: AppColors.surfaceMuted,
      appBar: AppBar(title: Text(ProfileText.myCoupons)),
      body: AsyncBody(
        value: coupons,
        onRetry: () => ref.invalidate(couponsProvider),
        builder: (items) => items.isEmpty
            ? EmptyState(
                icon: AppIcons.ticket,
                message: ProfileText.couponsEmpty,
              )
            : ListView.separated(
                padding: const EdgeInsets.all(AppSpacing.md),
                itemCount: items.length,
                separatorBuilder: (_, _) =>
                    const SizedBox(height: AppSpacing.sm),
                itemBuilder: (_, index) => CouponCard(coupon: items[index]),
              ),
      ),
    );
  }
}
