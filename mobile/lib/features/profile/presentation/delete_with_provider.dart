import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../auth/domain/social_provider.dart';
import '../../auth/presentation/social_button.dart';
import '../../auth/presentation/social_providers_for.dart';

class DeleteWithProvider extends StatelessWidget {
  const DeleteWithProvider({super.key, required this.onPressed});

  final ValueChanged<SocialProvider>? onPressed;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          ProfileText.deleteWithProvider,
          style: Theme.of(
            context,
          ).textTheme.bodyMedium?.copyWith(color: AppColors.textSecondary),
        ),
        const SizedBox(height: AppSpacing.md),
        for (final provider in socialProvidersFor(context)) ...[
          SocialButton(
            provider: provider,
            onPressed: onPressed == null ? null : () => onPressed!(provider),
          ),
          const SizedBox(height: AppSpacing.sm),
        ],
      ],
    );
  }
}
