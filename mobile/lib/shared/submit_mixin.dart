import 'package:flutter/widgets.dart';

import '../core/l10n/strings.dart';
import '../core/network/api_error.dart';

mixin SubmitMixin<T extends StatefulWidget> on State<T> {
  bool loading = false;
  String? errorMessage;

  Future<void> submit(Future<void> Function() action) async {
    setState(() {
      loading = true;
      errorMessage = null;
    });
    try {
      await action();
    } on ApiError catch (error) {
      if (mounted) setState(() => errorMessage = error.message);
    } catch (_) {
      if (mounted) setState(() => errorMessage = Strings.unknownError);
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }
}
