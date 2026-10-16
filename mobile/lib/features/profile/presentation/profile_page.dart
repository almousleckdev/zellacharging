import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/session/session_controller.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/menu_tile.dart';
import '../../../shared/widgets/page_header.dart';
import '../../../shared/widgets/soft_card.dart';
import '../domain/profile.dart';
import 'profile_header.dart';
import 'profile_providers.dart';

class ProfilePage extends ConsumerWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profile = ref.watch(profileProvider);
    return SafeArea(
      child: Column(
        children: [
          const PageHeader(title: Strings.profile),
          Expanded(
            child: AsyncBody(
              value: profile,
              onRetry: () => ref.invalidate(profileProvider),
              builder: (data) => ListView(
                padding: const EdgeInsets.all(AppSpacing.md),
                children: [
                  ProfileHeader(profile: data),
                  const SizedBox(height: AppSpacing.lg),
                  _card(_accountTiles(data)),
                  const SizedBox(height: AppSpacing.md),
                  _card(const [
                    MenuTile(
                      icon: Icons.language,
                      label: Strings.language,
                      trailingText: Strings.english,
                    ),
                    MenuTile(
                      icon: Icons.headset_mic_outlined,
                      label: Strings.contactUs,
                    ),
                    MenuTile(
                      icon: Icons.description_outlined,
                      label: Strings.agreement,
                    ),
                  ]),
                  const SizedBox(height: AppSpacing.md),
                  _card([
                    MenuTile(
                      icon: Icons.logout,
                      label: Strings.logOut,
                      color: AppColors.danger,
                      onTap: () => ref
                          .read(sessionControllerProvider.notifier)
                          .signOut(),
                    ),
                  ]),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  List<Widget> _accountTiles(Profile profile) => [
    if (profile.email != null)
      MenuTile(
        icon: Icons.mail_outline,
        label: profile.email!,
        trailing: const SizedBox.shrink(),
      ),
    if (profile.phone != null)
      MenuTile(
        icon: Icons.phone_outlined,
        label: profile.phone!,
        trailing: const SizedBox.shrink(),
      ),
  ];

  Widget _card(List<Widget> children) => SoftCard(
    padding: const EdgeInsets.symmetric(vertical: AppSpacing.xs),
    child: Column(children: children),
  );
}
