import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/refresh/on_resume.dart';
import '../../../core/theme/app_colors.dart';
import '../../../shared/widgets/async_body.dart';
import '../../free_sunday/presentation/free_sunday_providers.dart';
import 'coupon_providers.dart';
import 'coupons_list.dart';

class CouponsPage extends ConsumerWidget {
  const CouponsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final coupons = ref.watch(couponsProvider);
    void refreshOffer() => ref.invalidate(freeSundayProvider);
    return Scaffold(
      backgroundColor: AppColors.surfaceMuted,
      appBar: AppBar(title: Text(ProfileText.myCoupons)),
      body: OnResume(
        onResume: refreshOffer,
        child: RefreshIndicator.adaptive(
          onRefresh: () async {
            refreshOffer();
            ref.invalidate(couponsProvider);
            await ref.read(couponsProvider.future);
          },
          child: AsyncBody(
            value: coupons,
            onRetry: () => ref.invalidate(couponsProvider),
            builder: (items) => CouponsList(coupons: items),
          ),
        ),
      ),
    );
  }
}
