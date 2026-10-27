import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/secondary_button.dart';
import '../../charging/presentation/details/fee_details_card.dart';
import '../../charging/presentation/details/location_card.dart';
import '../../charging/presentation/session_providers.dart';
import '../domain/wallet_entry_detail.dart';
import 'delete_wallet_entry.dart';
import 'wallet_entry_cards.dart';
import 'wallet_entry_hero.dart';
import 'wallet_providers.dart';

class WalletEntryPage extends ConsumerWidget {
  const WalletEntryPage({super.key, required this.entryId});

  final String entryId;

  Future<void> _delete(BuildContext context, WidgetRef ref) async {
    final deleted = await confirmAndDeleteWalletEntry(context, ref, entryId);
    if (deleted && context.mounted) Navigator.of(context).maybePop();
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detail = ref.watch(walletEntryDetailProvider(entryId));
    final wallet = ref.watch(walletProvider).asData?.value;
    return Scaffold(
      appBar: AppBar(
        title: Text(WalletHistoryText.detailsTitle),
        actions: [
          IconButton(
            tooltip: WalletHistoryText.delete,
            icon: Icon(AppIcons.trash, color: AppColors.danger),
            onPressed: () => _delete(context, ref),
          ),
        ],
      ),
      body: AsyncBody(
        value: detail,
        onRetry: () => ref.invalidate(walletEntryDetailProvider(entryId)),
        builder: (data) => _body(
          context,
          ref,
          data,
          wallet?.currency ?? 'XAF',
          wallet?.decimals ?? 0,
        ),
      ),
    );
  }

  Widget _body(
    BuildContext context,
    WidgetRef ref,
    WalletEntryDetail detail,
    String currency,
    int decimals,
  ) {
    final sessionId = detail.sessionId;
    final session = sessionId == null
        ? null
        : ref.watch(sessionByIdProvider(sessionId)).asData?.value;
    return ListView(
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        WalletEntryHero(detail: detail, currency: currency, decimals: decimals),
        const SizedBox(height: AppSpacing.md),
        WalletEntryCards(
          detail: detail,
          currency: currency,
          decimals: decimals,
        ),
        if (session != null) ...[
          const SizedBox(height: AppSpacing.md),
          LocationCard(session: session),
          const SizedBox(height: AppSpacing.md),
          FeeDetailsCard(session: session),
        ],
        if (sessionId != null) ...[
          const SizedBox(height: AppSpacing.lg),
          SecondaryButton(
            label: WalletHistoryText.viewCharge,
            icon: AppIcons.lightningLine,
            onPressed: () => context.push(Routes.chargingPath(sessionId)),
          ),
        ],
      ],
    );
  }
}
