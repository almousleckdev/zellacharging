import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../core/l10n/strings.dart';
import '../core/theme/app_colors.dart';
import '../core/theme/app_icons.dart';
import 'picker_option.dart';

bool _isApple(BuildContext context) {
  final platform = Theme.of(context).platform;
  return platform == TargetPlatform.iOS || platform == TargetPlatform.macOS;
}

Future<T?> showOptionPicker<T>(
  BuildContext context, {
  required String title,
  required List<PickerOption<T>> options,
  T? selected,
}) {
  if (_isApple(context)) return _actionSheet(context, title, options);
  return showDialog<T>(
    context: context,
    builder: (dialogContext) => SimpleDialog(
      title: Text(title),
      children: [
        for (final option in options)
          SimpleDialogOption(
            onPressed: () => Navigator.pop(dialogContext, option.value),
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    option.label,
                    style: TextStyle(
                      color: option.destructive
                          ? AppColors.danger
                          : AppColors.textPrimary,
                    ),
                  ),
                ),
                if (option.value == selected)
                  Icon(AppIcons.checkCircle, color: AppColors.primary),
              ],
            ),
          ),
      ],
    ),
  );
}

Future<T?> _actionSheet<T>(
  BuildContext context,
  String title,
  List<PickerOption<T>> options,
) => showCupertinoModalPopup<T>(
  context: context,
  builder: (sheetContext) => CupertinoActionSheet(
    title: Text(title),
    actions: [
      for (final option in options)
        CupertinoActionSheetAction(
          isDestructiveAction: option.destructive,
          onPressed: () => Navigator.pop(sheetContext, option.value),
          child: Text(option.label),
        ),
    ],
    cancelButton: CupertinoActionSheetAction(
      onPressed: () => Navigator.pop(sheetContext),
      child: Text(Strings.cancel),
    ),
  ),
);
