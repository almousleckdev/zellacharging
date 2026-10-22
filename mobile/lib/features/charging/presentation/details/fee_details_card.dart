import 'package:flutter/material.dart';

import '../../../../core/format/money_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../shared/widgets/info_card.dart';
import '../../../../shared/widgets/info_row.dart';
import '../../domain/charging_session.dart';
import '../session_formatting.dart';

class FeeDetailsCard extends StatelessWidget {
  const FeeDetailsCard({super.key, required this.session});

  final ChargingSession session;

  String _money(int? minor) {
    final fees = session.fees;
    if (minor == null || fees == null) return '--';
    return MoneyFormat.format(minor, fees.currency, fees.decimals);
  }

  @override
  Widget build(BuildContext context) {
    final fees = session.fees;
    return InfoCard(
      title: HistoryText.feeDetails,
      children: [
        InfoRow(
          label: HistoryText.electricFee,
          value: _money(fees?.electricMinor),
        ),
        InfoRow(label: HistoryText.vat, value: _money(fees?.vatMinor)),
        InfoRow(
          label: HistoryText.serviceFee,
          value: _money(fees?.serviceMinor),
        ),
        const Divider(height: 16, color: AppColors.border),
        InfoRow(
          label: HistoryText.total,
          value: session.costText,
          emphasized: true,
        ),
      ],
    );
  }
}
