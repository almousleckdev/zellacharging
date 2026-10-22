import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/page_header.dart';
import 'logout_button.dart';
import 'profile_menu.dart';
import 'profile_providers.dart';
import 'profile_summary.dart';
import 'profile_tiles.dart';

class ProfilePage extends ConsumerWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profile = ref.watch(profileProvider);
    return SafeArea(
      child: Column(
        children: [
          PageHeader(title: Strings.profile),
          Expanded(
            child: AsyncBody(
              value: profile,
              onRetry: () => ref.invalidate(profileProvider),
              builder: (data) => ListView(
                padding: const EdgeInsets.all(AppSpacing.md),
                children: [
                  ProfileSummary(profile: data),
                  const SizedBox(height: AppSpacing.lg),
                  const ProfileTiles(),
                  const SizedBox(height: AppSpacing.lg),
                  const ProfileMenu(),
                  const SizedBox(height: AppSpacing.lg),
                  const LogoutButton(),
                  const SizedBox(height: AppSpacing.lg),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
