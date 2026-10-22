import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';

class AuthBackdrop extends StatelessWidget {
  const AuthBackdrop({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Stack(
      fit: StackFit.expand,
      children: [
        const ColoredBox(color: AppColors.textPrimary),
        Image.asset(
          'assets/images/auth_background.jpg',
          fit: BoxFit.cover,
          alignment: Alignment.bottomCenter,
          gaplessPlayback: true,
          errorBuilder: (_, _, _) => const SizedBox.shrink(),
        ),
        DecoratedBox(
          decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [
                AppColors.textPrimary.withValues(alpha: 0.4),
                AppColors.textPrimary.withValues(alpha: 0.9),
              ],
            ),
          ),
        ),
        child,
      ],
    );
  }
}
