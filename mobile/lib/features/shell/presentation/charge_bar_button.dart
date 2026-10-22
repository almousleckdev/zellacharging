import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';

class ChargeBarButton extends StatelessWidget {
  const ChargeBarButton({super.key, required this.onTap});

  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: InkWell(
        onTap: onTap,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Transform.translate(
              offset: const Offset(0, -14),
              child: Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  color: AppColors.primary,
                  shape: BoxShape.circle,
                  border: Border.all(color: AppColors.onPrimary, width: 4),
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.primary.withValues(alpha: 0.4),
                      blurRadius: 14,
                      offset: const Offset(0, 6),
                    ),
                  ],
                ),
                child: const Icon(
                  AppIcons.lightning,
                  color: AppColors.onPrimary,
                ),
              ),
            ),
            Transform.translate(
              offset: const Offset(0, -14),
              child: Text(
                Strings.charge,
                style: TextStyle(fontSize: 11, color: AppColors.primary),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
