import '../../../../shared/widgets/dismissible_form.dart';
import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/labeled_section.dart';
import '../../../../shared/widgets/pay/secure_note.dart';
import '../../../stations/domain/station.dart';
import '../../domain/charge_plan.dart';
import 'connector_picker.dart';
import 'station_header_card.dart';
import 'wallet_balance_card.dart';

class SetupForm extends StatelessWidget {
  const SetupForm({
    super.key,
    required this.formKey,
    required this.station,
    required this.plan,
    required this.connector,
    required this.onConnector,
  });

  final GlobalKey<FormState> formKey;
  final Station station;
  final ChargePlan plan;
  final int? connector;
  final ValueChanged<int> onConnector;

  @override
  Widget build(BuildContext context) {
    return DismissibleForm(
      formKey: formKey,
      children: [
        StationHeaderCard(station: station),
        const SizedBox(height: AppSpacing.lg),
        LabeledSection(
          title: StationText.chooseConnector,
          child: ConnectorPicker(
            connectors: station.connectors,
            selected: connector,
            onSelected: onConnector,
          ),
        ),
        WalletBalanceCard(plan: plan),
        const SizedBox(height: AppSpacing.lg),
        const SecureNote(),
      ],
    );
  }
}
