import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/primary_button.dart';
import '../../../shared/widgets/secondary_button.dart';
import '../../../shared/widgets/section_title.dart';
import '../../charging/presentation/active_session_banner.dart';
import '../../profile/presentation/profile_providers.dart';
import '../../stations/presentation/station_list.dart';
import '../../stations/presentation/station_providers.dart';
import '../../wallet/presentation/wallet_card.dart';
import 'hero_card.dart';
import 'home_header.dart';
import 'recent_sessions.dart';

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stations = ref.watch(nearbyStationsProvider);
    final profile = ref.watch(profileProvider).asData?.value;
    return SafeArea(
      child: ListView(
        padding: const EdgeInsets.all(AppSpacing.md),
        children: [
          HomeHeader(profile: profile),
          const SizedBox(height: AppSpacing.lg),
          const WalletCard(),
          const SizedBox(height: AppSpacing.md),
          const ActiveSessionBanner(),
          const HeroCard(),
          const SizedBox(height: AppSpacing.md),
          Row(
            children: [
              Expanded(
                child: SecondaryButton(
                  label: Strings.findStation,
                  onPressed: () => context.go(Routes.locations),
                ),
              ),
              const SizedBox(width: AppSpacing.sm),
              Expanded(
                child: PrimaryButton(
                  label: Strings.scanToCharge,
                  onPressed: () => context.push(Routes.scan),
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.lg),
          SectionTitle(
            title: Strings.nearbyStations,
            onViewAll: () => context.go(Routes.locations),
          ),
          const SizedBox(height: AppSpacing.md),
          AsyncBody(
            value: stations,
            onRetry: () => ref.invalidate(nearbyStationsProvider),
            shrinkWrap: true,
            builder: (data) => StationList(stations: data.take(3).toList()),
          ),
          const RecentSessions(),
        ],
      ),
    );
  }
}
