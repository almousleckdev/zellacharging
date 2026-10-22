import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/app_language.dart';
import '../../../core/l10n/language_controller.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/links/link_providers.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/adaptive_picker.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/picker_option.dart';
import '../../../shared/widgets/menu_tile.dart';
import '../../../shared/widgets/soft_card.dart';
import '../../support/presentation/support_providers.dart';

class ProfileMenu extends ConsumerWidget {
  const ProfileMenu({super.key});

  Future<void> _pickLanguage(BuildContext context, WidgetRef ref) async {
    final current = ref.read(languageProvider);
    final picked = await showOptionPicker<AppLanguage>(
      context,
      title: ProfileText.chooseLanguage,
      selected: current,
      options: [
        for (final language in AppLanguage.values)
          PickerOption(label: language.label, value: language),
      ],
    );
    if (picked == null) return;
    await ref.read(languageProvider.notifier).select(picked);
  }

  Future<void> _openAgreement(BuildContext context, WidgetRef ref) async {
    final link = (await ref.read(supportProvider.future)).agreementUrl;
    final opened =
        link != null &&
        await ref.read(linkLauncherProvider).open(Uri.parse(link));
    if (!opened && context.mounted) {
      AppToast.info(context, ProfileText.agreementUnavailable);
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final language = ref.watch(languageProvider);
    return SoftCard(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.xs),
      child: Column(
        children: [
          MenuTile(
            icon: AppIcons.headset,
            label: ProfileText.contactUs,
            onTap: () => context.push(Routes.contact),
          ),
          MenuTile(
            icon: AppIcons.globe,
            label: ProfileText.language,
            trailingText: language.label,
            onTap: () => _pickLanguage(context, ref),
          ),
          MenuTile(
            icon: AppIcons.document,
            label: ProfileText.agreement,
            onTap: () => _openAgreement(context, ref),
          ),
          MenuTile(
            icon: AppIcons.sliders,
            label: ProfileText.settingsTitle,
            onTap: () => context.push(Routes.settings),
          ),
        ],
      ),
    );
  }
}
