import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/format/date_format.dart';
import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../domain/entry_day_group.dart';
import '../domain/wallet.dart';
import '../domain/wallet_entry.dart';
import 'delete_wallet_entry.dart';
import 'wallet_entry_tile.dart';

class WalletHistoryCard extends ConsumerStatefulWidget {
  const WalletHistoryCard({
    super.key,
    required this.wallet,
    required this.entries,
  });

  final Wallet wallet;
  final List<WalletEntry> entries;

  @override
  ConsumerState<WalletHistoryCard> createState() => _WalletHistoryCardState();
}

class _WalletHistoryCardState extends ConsumerState<WalletHistoryCard> {
  final _removed = <String>{};

  String _heading(DateTime day) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    if (day == today) return WalletHistoryText.today;
    if (day == DateTime(today.year, today.month, today.day - 1)) {
      return WalletHistoryText.yesterday;
    }
    return DateFormatter.date(day);
  }

  @override
  Widget build(BuildContext context) {
    final groups = EntryDayGroup.of([
      for (final entry in widget.entries)
        if (!_removed.contains(entry.id)) entry,
    ]);
    if (groups.isEmpty) {
      return Padding(
        padding: const EdgeInsets.all(AppSpacing.lg),
        child: Text(
          WithdrawText.emptyHistory,
          style: const TextStyle(color: AppColors.textSecondary),
        ),
      );
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        for (final group in groups) ...[
          Padding(
            padding: const EdgeInsets.fromLTRB(
              AppSpacing.xs,
              AppSpacing.md,
              0,
              AppSpacing.sm,
            ),
            child: Text(
              _heading(group.day),
              style: Theme.of(context).textTheme.labelLarge?.copyWith(
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
          for (final entry in group.entries) _row(entry),
        ],
      ],
    );
  }

  Widget _row(WalletEntry entry) => Padding(
    padding: const EdgeInsets.only(bottom: AppSpacing.sm),
    child: Dismissible(
      key: ValueKey(entry.id),
      direction: DismissDirection.endToStart,
      confirmDismiss: (_) =>
          confirmAndDeleteWalletEntry(context, ref, entry.id),
      onDismissed: (_) => setState(() => _removed.add(entry.id)),
      background: Container(
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: AppSpacing.lg),
        decoration: BoxDecoration(
          color: AppColors.danger,
          borderRadius: BorderRadius.circular(AppSpacing.radius),
        ),
        child: Icon(AppIcons.trash, color: AppColors.onPrimary),
      ),
      child: WalletEntryTile(
        entry: entry,
        currency: widget.wallet.currency,
        decimals: widget.wallet.decimals,
        onTap: () => context.push(Routes.walletEntryPath(entry.id)),
      ),
    ),
  );
}
