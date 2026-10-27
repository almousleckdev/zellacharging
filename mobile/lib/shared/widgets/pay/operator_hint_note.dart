import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../features/wallet/domain/mobile_operator.dart';
import '../../../features/wallet/domain/operator_hint.dart';

class OperatorHintNote extends StatelessWidget {
  const OperatorHintNote({
    super.key,
    required this.msisdn,
    required this.selected,
    required this.onUse,
  });

  final String msisdn;
  final MobileOperator selected;
  final ValueChanged<MobileOperator> onUse;

  @override
  Widget build(BuildContext context) {
    final suggested = OperatorHint.forNumber(msisdn);
    if (suggested == null || suggested == selected) {
      return const SizedBox.shrink();
    }
    final theme = Theme.of(context);
    return Container(
      margin: const EdgeInsets.only(top: AppSpacing.sm),
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: AppColors.primarySoft,
        borderRadius: BorderRadius.circular(AppSpacing.radius),
      ),
      child: Row(
        children: [
          const Icon(AppIcons.info, size: 18, color: AppColors.primary),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: Text(
              PayFlowText.operatorMismatch(suggested.label),
              style: theme.textTheme.bodySmall,
            ),
          ),
          TextButton(
            onPressed: () => onUse(suggested),
            child: Text(PayFlowText.useOperator(suggested.label)),
          ),
        ],
      ),
    );
  }
}
