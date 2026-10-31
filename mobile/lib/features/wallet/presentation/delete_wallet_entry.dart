import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/network/api_error.dart';
import '../../../shared/adaptive_dialog.dart';
import '../../../shared/app_toast.dart';
import 'wallet_providers.dart';

Future<bool> confirmAndDeleteWalletEntry(
  BuildContext context,
  WidgetRef ref,
  String entryId,
) async {
  final confirmed = await showConfirmDialog(
    context,
    title: WalletHistoryText.deleteTitle,
    message: WalletHistoryText.deleteBody,
    confirmLabel: WalletHistoryText.delete,
    destructive: true,
  );
  if (!confirmed || !context.mounted) return false;
  try {
    await ref.read(walletRepositoryProvider).hideEntry(entryId);
  } on ApiError catch (error) {
    if (context.mounted) AppToast.error(context, error.message);
    return false;
  }
  ref.invalidate(walletEntriesProvider);
  if (context.mounted) AppToast.success(context, WalletHistoryText.deleted);
  return true;
}
