import 'package:flutter/material.dart';
import 'package:flutter/gestures.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';

class TermsCheckbox extends StatelessWidget {
  const TermsCheckbox({
    super.key,
    required this.accepted,
    required this.onChanged,
    required this.onOpenTerms,
    this.showError = false,
  });

  final bool accepted;
  final ValueChanged<bool> onChanged;
  final VoidCallback onOpenTerms;
  final bool showError;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final plain = theme.textTheme.bodyMedium?.copyWith(
      color: AppColors.textSecondary,
    );
    final link = plain?.copyWith(
      color: AppColors.primary,
      fontWeight: FontWeight.w500,
    );
    final tap = TapGestureRecognizer()..onTap = onOpenTerms;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 24,
              height: 24,
              child: Checkbox.adaptive(
                value: accepted,
                onChanged: (value) => onChanged(value ?? false),
              ),
            ),
            const SizedBox(width: AppSpacing.sm),
            Expanded(
              child: Text.rich(
                TextSpan(
                  style: plain,
                  children: [
                    TextSpan(text: AuthText.agreePrefix),
                    TextSpan(
                      text: AuthText.terms,
                      style: link,
                      recognizer: tap,
                    ),
                    TextSpan(text: AuthText.agreeAnd),
                    TextSpan(
                      text: AuthText.privacy,
                      style: link,
                      recognizer: tap,
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
        if (showError && !accepted)
          Padding(
            padding: const EdgeInsets.only(top: AppSpacing.xs),
            child: Text(
              AuthText.mustAgree,
              style: theme.textTheme.bodySmall?.copyWith(
                color: AppColors.danger,
              ),
            ),
          ),
      ],
    );
  }
}
