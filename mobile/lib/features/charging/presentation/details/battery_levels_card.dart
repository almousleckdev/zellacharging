import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/info_card.dart';
import '../../../../shared/widgets/info_row.dart';
import '../../domain/charging_session.dart';
import '../session_formatting.dart';

class BatteryLevelsCard extends StatelessWidget {
  const BatteryLevelsCard({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    return InfoCard(
      title: HistoryText.batteryLevels,
      children: [
        InfoRow(
          label: HistoryText.batteryStart,
          value: session.startBatteryText,
        ),
        InfoRow(label: HistoryText.batteryEnd, value: session.batteryText),
      ],
    );
  }
}
