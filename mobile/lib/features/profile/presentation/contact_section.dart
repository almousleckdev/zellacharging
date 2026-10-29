import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/widgets/info_card.dart';
import '../domain/profile.dart';

class ContactSection extends StatelessWidget {
  const ContactSection({super.key, required this.profile});

  final Profile profile;

  @override
  Widget build(BuildContext context) {
    return InfoCard(
      title: ProfileText.contactDetails,
      children: [
        _ContactRow(
          icon: AppIcons.envelope,
          label: Strings.email,
          value: profile.email,
          verified: profile.emailVerified,
          addLabel: ProfileText.addEmail,
          onAdd: () => context.push(Routes.addEmail),
        ),
        const Divider(height: AppSpacing.lg),
        _ContactRow(
          icon: AppIcons.phone,
          label: Strings.phone,
          value: profile.phone,
          verified: profile.phoneVerified,
          addLabel: ProfileText.addPhone,
          onAdd: () => context.push(Routes.addPhone),
        ),
      ],
    );
  }
}

class _ContactRow extends StatelessWidget {
  const _ContactRow({
    required this.icon,
    required this.label,
    required this.value,
    required this.verified,
    required this.addLabel,
    required this.onAdd,
  });

  final IconData icon;
  final String label;
  final String? value;
  final bool verified;
  final String addLabel;
  final VoidCallback onAdd;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = verified ? AppColors.success : AppColors.textSecondary;
    if (value == null) {
      return ListTile(
        contentPadding: EdgeInsets.zero,
        leading: Icon(icon, color: AppColors.primary),
        title: Text(
          addLabel,
          style: theme.textTheme.bodyMedium?.copyWith(
            color: AppColors.primary,
            fontWeight: FontWeight.w700,
          ),
        ),
        trailing: Icon(AppIcons.plus, color: AppColors.primary),
        onTap: onAdd,
      );
    }
    return Row(
      children: [
        Icon(icon, color: AppColors.textSecondary),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
              Text(
                value!,
                style: theme.textTheme.bodyMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
        Text(
          verified ? ProfileText.verified : ProfileText.notVerified,
          style: theme.textTheme.bodySmall?.copyWith(
            color: color,
            fontWeight: FontWeight.w700,
          ),
        ),
      ],
    );
  }
}
