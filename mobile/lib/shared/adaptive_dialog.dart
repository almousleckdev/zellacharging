import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../core/l10n/strings.dart';
import '../core/theme/app_colors.dart';

bool _isApple(BuildContext context) {
  final platform = Theme.of(context).platform;
  return platform == TargetPlatform.iOS || platform == TargetPlatform.macOS;
}

Future<bool> showConfirmDialog(
  BuildContext context, {
  required String title,
  required String message,
  required String confirmLabel,
  bool destructive = false,
}) async {
  final result = await showAdaptiveDialog<bool>(
    context: context,
    builder: (dialogContext) => AlertDialog.adaptive(
      title: Text(title),
      content: Text(message),
      actions: [
        _DialogAction(
          label: Strings.cancel,
          onPressed: () => Navigator.pop(dialogContext, false),
        ),
        _DialogAction(
          label: confirmLabel,
          emphasized: true,
          destructive: destructive,
          onPressed: () => Navigator.pop(dialogContext, true),
        ),
      ],
    ),
  );
  return result ?? false;
}

class _DialogAction extends StatelessWidget {
  const _DialogAction({
    required this.label,
    required this.onPressed,
    this.emphasized = false,
    this.destructive = false,
  });

  final String label;
  final VoidCallback onPressed;
  final bool emphasized;
  final bool destructive;

  @override
  Widget build(BuildContext context) {
    if (_isApple(context)) {
      return CupertinoDialogAction(
        isDefaultAction: emphasized,
        isDestructiveAction: destructive,
        onPressed: onPressed,
        child: Text(label),
      );
    }
    return TextButton(
      onPressed: onPressed,
      style: TextButton.styleFrom(
        foregroundColor: destructive ? AppColors.danger : AppColors.primary,
      ),
      child: Text(
        label,
        style: TextStyle(
          fontWeight: emphasized ? FontWeight.w700 : FontWeight.w500,
        ),
      ),
    );
  }
}
