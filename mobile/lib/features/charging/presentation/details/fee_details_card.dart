import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/info_card.dart';
import '../../../../shared/widgets/info_row.dart';
import '../../domain/charging_session.dart';
import '../session_formatting.dart';

class FeeDetailsCard extends StatelessWidget {
  const FeeDetailsCard({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    return InfoCard(
      title: HistoryText.payment,
      children: [
        InfoRow(
          label: HistoryText.zellaCharge,
          value: session.costText,
          emphasized: true,
        ),
      ],
    );
  }
}
