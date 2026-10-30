import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';

abstract final class NotificationStyle {
  static IconData icon(String type) => switch (type.split('_').first) {
    'TOPUP' => AppIcons.wallet,
    'WITHDRAWAL' => AppIcons.signOut,
    'CHARGE' => AppIcons.lightningLine,
    'PASSWORD' => AppIcons.key,
    _ => AppIcons.bell,
  };

  static Color color(String type) {
    if (type.endsWith('FAILED') || type.endsWith('INTERRUPTED')) {
      return AppColors.danger;
    }
    if (type.endsWith('SUCCEEDED') || type.endsWith('COMPLETED')) {
      return AppColors.success;
    }
    return AppColors.primary;
  }
}
