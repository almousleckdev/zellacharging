import 'package:flutter/material.dart';

import '../../core/l10n/strings.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_icons.dart';
import '../../core/theme/app_spacing.dart';
import '../otp_delivery.dart';

Future<OtpDelivery?> showOtpChannelSheet(BuildContext context) =>
    showModalBottomSheet<OtpDelivery>(
      context: context,
      backgroundColor: AppColors.surface,
      showDragHandle: false,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(
          top: Radius.circular(AppSpacing.radiusLarge),
        ),
      ),
      builder: (_) => const _OtpChannelSheet(),
    );

class _OtpChannelSheet extends StatelessWidget {
  const _OtpChannelSheet();

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const SizedBox(height: AppSpacing.sm),
          Container(
            width: 40,
            height: 4,
            decoration: BoxDecoration(
              color: AppColors.border,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: AppSpacing.lg,
              vertical: AppSpacing.lg,
            ),
            child: Text(
              ProfileText.otpSheetTitle,
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                color: AppColors.textPrimary,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
          const Divider(height: 1, color: AppColors.border),
          _ChannelRow(
            icon: AppIcons.whatsapp,
            iconColor: AppColors.success,
            label: ProfileText.otpViaWhatsapp,
            recommended: true,
            onTap: () => Navigator.pop(context, OtpDelivery.whatsapp),
          ),
          const Divider(height: 1, color: AppColors.border),
          _ChannelRow(
            icon: AppIcons.sms,
            iconColor: AppColors.textPrimary,
            label: ProfileText.otpViaSms,
            onTap: () => Navigator.pop(context, OtpDelivery.sms),
          ),
          const SizedBox(height: AppSpacing.md),
        ],
      ),
    );
  }
}

class _ChannelRow extends StatelessWidget {
  const _ChannelRow({
    required this.icon,
    required this.iconColor,
    required this.label,
    required this.onTap,
    this.recommended = false,
  });

  final IconData icon;
  final Color iconColor;
  final String label;
  final VoidCallback onTap;
  final bool recommended;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.lg,
          vertical: AppSpacing.lg,
        ),
        child: Row(
          children: [
            Icon(icon, size: 30, color: iconColor),
            const SizedBox(width: AppSpacing.lg),
            Expanded(
              child: Text(
                label,
                style: theme.textTheme.titleMedium?.copyWith(
                  color: AppColors.textPrimary,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            if (recommended) const _RecommendedChip(),
          ],
        ),
      ),
    );
  }
}

class _RecommendedChip extends StatelessWidget {
  const _RecommendedChip();

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.sm,
      ),
      decoration: BoxDecoration(
        color: AppColors.successSoft,
        borderRadius: BorderRadius.circular(AppSpacing.radius),
      ),
      child: Text(
        ProfileText.recommended,
        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
          color: AppColors.chargeDeep,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
