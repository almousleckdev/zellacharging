import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/stat_tile.dart';
import '../../coupons/presentation/coupon_providers.dart';
import '../../wallet/presentation/wallet_formatting.dart';
import '../../wallet/presentation/wallet_providers.dart';

class ProfileTiles extends ConsumerWidget {
  const ProfileTiles({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final coupons = ref.watch(couponsProvider).asData?.value;
    final wallet = ref.watch(walletProvider).value;
    return Row(
      children: [
        Expanded(
          child: StatTile(
            title: ProfileText.myCoupons,
            value: coupons == null ? '--' : '${coupons.length}',
            icon: AppIcons.ticket,
            onTap: () => context.push(Routes.coupons),
          ),
        ),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: StatTile(
            title: ProfileText.myWallet,
            value: wallet?.availableText ?? '--',
            icon: AppIcons.wallet,
            onTap: () => context.push(Routes.wallet),
          ),
        ),
      ],
    );
  }
}
