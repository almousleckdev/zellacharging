import 'package:flutter/widgets.dart';

import '../core/l10n/strings.dart';
import '../core/network/api_error.dart';
import 'app_toast.dart';

mixin SubmitMixin<T extends StatefulWidget> on State<T> {
  bool loading = false;

  void showError(String message) {
    if (mounted) AppToast.error(context, message);
  }

  Future<void> submit(Future<void> Function() action) async {
    setState(() => loading = true);
    try {
      await action();
    } on ApiError catch (error) {
      showError(error.message);
    } catch (_) {
      showError(Strings.unknownError);
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }
}
