import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';

abstract final class ConnectorStatusStyle {
  static String label(String status) =>
      StationText.statusLabels[status] ?? status;

  static Color color(String status) => switch (status) {
    'Available' => AppColors.success,
    'Faulted' => AppColors.danger,
    'Unavailable' => AppColors.neutral,
    _ => AppColors.primary,
  };
}
