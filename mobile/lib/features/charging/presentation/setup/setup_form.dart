import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/dismissible_form.dart';
import '../../../../shared/widgets/labeled_section.dart';
import '../../../../shared/widgets/pay/secure_note.dart';
import '../../../free_sunday/presentation/free_sunday_notice.dart';
import '../../../stations/domain/station.dart';
import '../../../stations/domain/station_connector.dart';
import '../../domain/charge_plan.dart';
import '../../domain/charge_readiness.dart';
import 'charge_amount_card.dart';
import 'charge_summary_card.dart';
import 'connector_picker.dart';
import 'plug_status_note.dart';
import 'station_header_card.dart';
import 'wallet_balance_card.dart';

class SetupForm extends StatelessWidget {
  const SetupForm({
    super.key,
    required this.formKey,
    required this.station,
    required this.plan,
    required this.readiness,
    required this.connector,
    required this.amount,
    required this.onConnector,
    required this.onAmount,
  });

  final GlobalKey<FormState> formKey;
  final Station station;
  final ChargePlan plan;
  final ChargeReadiness readiness;
  final StationConnector? connector;
  final TextEditingController amount;
  final ValueChanged<int> onConnector;
  final VoidCallback onAmount;

  @override
  Widget build(BuildContext context) {
    final chosen = connector;
    return DismissibleForm(
      formKey: formKey,
      children: [
        StationHeaderCard(station: station),
        const SizedBox(height: AppSpacing.lg),
        LabeledSection(
          title: StationText.chooseConnector,
          child: Column(
            children: [
              ConnectorPicker(
                connectors: station.connectors,
                selected: chosen?.connectorId,
                onSelected: onConnector,
              ),
              if (chosen != null) PlugStatusNote(connector: chosen),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.lg),
        if (plan.walletKnown && !plan.freeCharging) ...[
          ChargeAmountCard(
            controller: amount,
            plan: plan,
            readiness: readiness,
            onChanged: onAmount,
          ),
          const SizedBox(height: AppSpacing.lg),
        ],
        const FreeSundayNotice(),
        WalletBalanceCard(plan: plan),
        if (readiness.canStart && !plan.freeCharging) ...[
          const SizedBox(height: AppSpacing.lg),
          ChargeSummaryCard(plan: plan),
        ],
        const SizedBox(height: AppSpacing.lg),
        const SecureNote(),
      ],
    );
  }
}
