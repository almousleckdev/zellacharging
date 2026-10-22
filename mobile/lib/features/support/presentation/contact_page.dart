import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/links/link_providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/empty_state.dart';
import '../../../shared/widgets/menu_tile.dart';
import '../../../shared/widgets/soft_card.dart';
import '../domain/support_info.dart';
import 'support_providers.dart';

class ContactPage extends ConsumerWidget {
  const ContactPage({super.key});

  Future<void> _open(BuildContext context, WidgetRef ref, Uri link) async {
    final opened = await ref.read(linkLauncherProvider).open(link);
    if (!opened && context.mounted) {
      AppToast.error(context, Strings.unknownError);
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final support = ref.watch(supportProvider);
    return Scaffold(
      backgroundColor: AppColors.surfaceMuted,
      appBar: AppBar(title: Text(ProfileText.contactTitle)),
      body: AsyncBody(
        value: support,
        onRetry: () => ref.invalidate(supportProvider),
        builder: (info) => info.hasContact
            ? _contacts(context, ref, info)
            : EmptyState(
                icon: AppIcons.headset,
                message: ProfileText.contactNotConfigured,
              ),
      ),
    );
  }

  Widget _contacts(BuildContext context, WidgetRef ref, SupportInfo info) {
    return ListView(
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        SoftCard(
          padding: const EdgeInsets.symmetric(vertical: AppSpacing.xs),
          child: Column(
            children: [
              if (info.email != null)
                MenuTile(
                  icon: AppIcons.envelope,
                  label: info.email!,
                  onTap: () => _open(
                    context,
                    ref,
                    Uri(scheme: 'mailto', path: info.email),
                  ),
                ),
              if (info.phone != null)
                MenuTile(
                  icon: AppIcons.phone,
                  label: info.phone!,
                  onTap: () =>
                      _open(context, ref, Uri(scheme: 'tel', path: info.phone)),
                ),
              if (info.whatsapp != null)
                MenuTile(
                  icon: AppIcons.chat,
                  label: '${ProfileText.whatsapp} · ${info.whatsapp}',
                  onTap: () => _open(
                    context,
                    ref,
                    Uri.https(
                      'wa.me',
                      '/${info.whatsapp!.replaceAll(RegExp(r'\D'), '')}',
                    ),
                  ),
                ),
            ],
          ),
        ),
      ],
    );
  }
}
