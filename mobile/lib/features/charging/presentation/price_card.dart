import 'package:flutter/material.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../shared/widgets/soft_card.dart';
import '../../../shared/widgets/status_chip.dart';
import '../domain/charging_session.dart';
import 'session_formatting.dart';

class PriceCard extends StatelessWidget {
  const PriceCard({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SoftCard(
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  Strings.amountSoFar,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
                FittedBox(
                  fit: BoxFit.scaleDown,
                  alignment: Alignment.centerLeft,
                  child: Text(
                    session.priceText,
                    style: theme.textTheme.headlineLarge?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
              ],
            ),
          ),
          if (session.priceIsEstimate)
            const StatusChip(
              label: Strings.estimated,
              color: AppColors.chargeDeep,
            ),
        ],
      ),
    );
  }
}
