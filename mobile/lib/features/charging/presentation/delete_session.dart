import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/network/api_error.dart';
import '../../../shared/adaptive_dialog.dart';
import '../../../shared/app_toast.dart';
import 'session_providers.dart';

Future<bool> confirmAndDeleteSession(
  BuildContext context,
  WidgetRef ref,
  String sessionId,
) async {
  final confirmed = await showConfirmDialog(
    context,
    title: HistoryText.deleteTitle,
    message: HistoryText.deleteBody,
    confirmLabel: HistoryText.deleteCharge,
    destructive: true,
  );
  if (!confirmed || !context.mounted) return false;
  try {
    await ref.read(sessionRepositoryProvider).delete(sessionId);
  } on ApiError catch (error) {
    if (context.mounted) AppToast.error(context, error.message);
    return false;
  }
  ref.invalidate(historyProvider);
  ref.invalidate(activeSessionProvider);
  if (context.mounted) AppToast.info(context, HistoryText.chargeDeleted);
  return true;
}
