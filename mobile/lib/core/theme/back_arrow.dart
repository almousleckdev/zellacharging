import 'package:flutter/material.dart';

import 'app_colors.dart';
import 'app_icons.dart';

class BackArrow extends StatelessWidget {
  const BackArrow({super.key});

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: AppColors.surface,
        shape: BoxShape.circle,
        boxShadow: [
          BoxShadow(
            color: AppColors.textPrimary.withValues(alpha: 0.12),
            blurRadius: 10,
            offset: const Offset(0, 3),
          ),
        ],
      ),
      child: const SizedBox(
        width: 38,
        height: 38,
        child: Icon(AppIcons.arrowLeft, size: 20, color: AppColors.primary),
      ),
    );
  }
}
