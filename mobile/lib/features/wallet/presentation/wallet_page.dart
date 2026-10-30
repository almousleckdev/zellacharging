import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/app_toast.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/section_title.dart';
import '../../../shared/widgets/pay/wallet_balance_card.dart';
import '../../../shared/widgets/primary_button.dart';
import '../../../shared/widgets/secondary_button.dart';
import '../domain/wallet.dart';
import 'wallet_history_card.dart';
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
    return Row(
      children: [
        Expanded(child: topUp),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: SecondaryButton(
            label: WithdrawText.action,
            icon: AppIcons.signOut,
            onPressed: () => wallet.withdrawalsEnabled
                ? context.push(Routes.withdraw)
                : AppToast.info(context, WithdrawText.unavailable),
          ),
        ),
      ],
    );
  }

  Widget _history(BuildContext context, WidgetRef ref, Wallet wallet) {
    final entries = ref.watch(walletEntriesProvider);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionTitle(title: WithdrawText.historyTitle),
        entries.when(
          loading: () => const Padding(
            padding: EdgeInsets.all(AppSpacing.lg),
            child: Center(child: CircularProgressIndicator.adaptive()),
          ),
          error: (_, _) => Padding(
            padding: const EdgeInsets.all(AppSpacing.md),
            child: Text(
              Strings.networkError,
              style: const TextStyle(color: AppColors.textSecondary),
            ),
          ),
          data: (items) => WalletHistoryCard(wallet: wallet, entries: items),
        ),
      ],
    );
  }
}
