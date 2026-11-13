import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../domain/contact_channel.dart';

class ContactModeSwitch extends StatelessWidget {
  const ContactModeSwitch({
    super.key,
    required this.selected,
    required this.onChanged,
  });

  final ContactChannel selected;
  final ValueChanged<ContactChannel> onChanged;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(AppSpacing.xs),
      decoration: BoxDecoration(
        color: AppColors.surfaceMuted,
        borderRadius: BorderRadius.circular(AppSpacing.radius),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        children: [
          _option(context, ContactChannel.email, AuthText.signUpWithEmail),
          _option(context, ContactChannel.phone, AuthText.signUpWithPhone),
        ],
      ),
    );
  }

  Widget _option(BuildContext context, ContactChannel channel, String label) {
    final active = channel == selected;
    return Expanded(
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: () => onChanged(channel),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: AppSpacing.sm),
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: active ? AppColors.surface : AppColors.surfaceMuted,
            borderRadius: BorderRadius.circular(AppSpacing.radiusSmall),
            border: active ? Border.all(color: AppColors.primary) : null,
          ),
          child: Text(
            label,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: active ? AppColors.primary : AppColors.textSecondary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
      ),
    );
  }
}
