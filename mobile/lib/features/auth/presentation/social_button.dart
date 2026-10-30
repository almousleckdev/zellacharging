import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../domain/social_provider.dart';

class SocialButton extends StatelessWidget {
  const SocialButton({
    super.key,
    required this.provider,
    required this.onPressed,
  });

  final SocialProvider provider;
  final VoidCallback? onPressed;

  String get _logo => switch (provider) {
    SocialProvider.google => 'assets/icons/google.png',
    SocialProvider.apple => 'assets/icons/apple.png',
  };

  String get _label => switch (provider) {
    SocialProvider.google => AuthText.google,
    SocialProvider.apple => AuthText.apple,
  };

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: AppSpacing.buttonHeight,
      child: OutlinedButton.icon(
        onPressed: onPressed,
        icon: Image.asset(_logo, width: 18, height: 18),
        label: Text(_label, style: const TextStyle(fontSize: 13)),
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.textPrimary,
          backgroundColor: AppColors.surface,
          side: const BorderSide(color: AppColors.border),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppSpacing.buttonRadius),
          ),
        ),
      ),
    );
  }
}
