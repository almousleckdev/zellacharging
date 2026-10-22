import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:phosphor_icons/phosphor_icons.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/app_toast.dart';

class SocialButtons extends StatelessWidget {
  const SocialButtons({super.key});

  bool get _showApple => defaultTargetPlatform == TargetPlatform.iOS;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      children: [
        Row(
          children: [
            const Expanded(child: Divider(color: AppColors.border)),
            Expanded(
              flex: 2,
              child: Center(
                child: FittedBox(
                  fit: BoxFit.scaleDown,
                  child: Text(
                    AuthText.orContinueWith,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: AppColors.textSecondary,
                    ),
                  ),
                ),
              ),
            ),
            const Expanded(child: Divider(color: AppColors.border)),
          ],
        ),
        const SizedBox(height: AppSpacing.md),
        Row(
          children: [
            Expanded(
              child: _SocialButton(
                icon: PhosphorIconsBold.googleLogo,
                label: AuthText.google,
              ),
            ),
            if (_showApple) ...[
              const SizedBox(width: AppSpacing.md),
              Expanded(
                child: _SocialButton(
                  icon: PhosphorIconsFill.appleLogo,
                  label: AuthText.apple,
                ),
              ),
            ],
          ],
        ),
      ],
    );
  }
}

class _SocialButton extends StatelessWidget {
  const _SocialButton({required this.icon, required this.label});

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: AppSpacing.buttonHeight - 6,
      child: OutlinedButton.icon(
        onPressed: () => AppToast.info(context, AuthText.comingSoon),
        icon: Icon(icon, size: 20, color: AppColors.textPrimary),
        label: Text(label),
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
