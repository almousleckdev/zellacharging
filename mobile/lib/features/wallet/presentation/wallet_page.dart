import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/pay/pay_card.dart';
import '../../../shared/widgets/pay/wallet_balance_card.dart';
import '../../../shared/widgets/primary_button.dart';
import '../../../shared/widgets/secondary_button.dart';
import '../domain/wallet.dart';
import 'wallet_entry_tile.dart';
import 'wallet_providers.dart';

class WalletPage extends ConsumerWidget {
  const WalletPage({super.key});

  Future<void> _refresh(WidgetRef ref) async {
    ref.invalidate(walletProvider);
    ref.invalidate(walletEntriesProvider);
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: Text(WithdrawText.hubTitle)),
      body: AsyncBody(
        value: ref.watch(walletProvider),
        onRetry: () => _refresh(ref),
        builder: (wallet) => RefreshIndicator.adaptive(
          onRefresh: () => _refresh(ref),
          child: ListView(
            padding: const EdgeInsets.all(AppSpacing.lg),
            children: [
              WalletBalanceCard(wallet: wallet),
              const SizedBox(height: AppSpacing.lg),
              _actions(context, wallet),
              const SizedBox(height: AppSpacing.lg),
              _history(context, ref, wallet),
            ],
          ),
        ),
      ),
    );
  }

  Widget _actions(BuildContext context, Wallet wallet) {
    final topUp = PrimaryButton(
      label: WalletText.topUp,
      icon: AppIcons.wallet,
      onPressed: () => context.push(Routes.topup),
    );
    if (!wallet.withdrawalsEnabled) return topUp;
    return Row(
      children: [
        Expanded(child: topUp),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: SecondaryButton(
            label: WithdrawText.action,
            icon: AppIcons.signOut,
            onPressed: () => context.push(Routes.withdraw),
          ),
        ),
      ],
    );
  }

  Widget _history(BuildContext context, WidgetRef ref, Wallet wallet) {
    final entries = ref.watch(walletEntriesProvider);
    return PayCard(
      title: WithdrawText.historyTitle,
      child: entries.when(
        loading: () => const Padding(
          padding: EdgeInsets.all(AppSpacing.md),
          child: Center(child: CircularProgressIndicator.adaptive()),
        ),
        error: (_, _) => Text(
          Strings.networkError,
          style: const TextStyle(color: AppColors.textSecondary),
        ),
        data: (items) {
          final visible = items.where((entry) => entry.visible).toList();
          if (visible.isEmpty) {
            return Text(
              WithdrawText.emptyHistory,
              style: const TextStyle(color: AppColors.textSecondary),
            );
          }
          return Column(
            children: [
              for (final entry in visible)
                WalletEntryTile(
                  entry: entry,
                  currency: wallet.currency,
                  decimals: wallet.decimals,
                ),
            ],
          );
        },
      ),
    );
  }
}
