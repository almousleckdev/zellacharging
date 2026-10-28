import '../../../../shared/widgets/dismissible_form.dart';
import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/widgets/labeled_section.dart';
import '../../../../shared/widgets/pay/amount_hero.dart';
import '../../../../shared/widgets/pay/secure_note.dart';
import '../../../stations/domain/station.dart';
import '../../../wallet/domain/mobile_operator.dart';
import '../../domain/charge_plan.dart';
import 'amount_rules.dart';
import 'charge_summary.dart';
import 'connector_picker.dart';
import 'payment_method_card.dart';
import 'station_header_card.dart';

class SetupForm extends StatelessWidget {
  const SetupForm({
    super.key,
    required this.formKey,
    required this.station,
    required this.plan,
    required this.amount,
    required this.phone,
    required this.connector,
    required this.operator,
    required this.onConnector,
    required this.onOperator,
    required this.onAmountChanged,
  });

  final GlobalKey<FormState> formKey;
  final Station station;
  final ChargePlan plan;
  final TextEditingController amount;
  final TextEditingController phone;
  final int? connector;
  final MobileOperator operator;
  final ValueChanged<int> onConnector;
  final ValueChanged<MobileOperator> onOperator;
  final VoidCallback onAmountChanged;

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
        AmountHero(
          title: PayText.budgetTitle,
          controller: amount,
          currency: plan.currency,
          quickAmounts: ChargeText.chargeAmounts,
          helper: AmountRules.helper(plan),
          validator: (value) => AmountRules.validate(value, plan),
          onChanged: onAmountChanged,
        ),
        const SizedBox(height: AppSpacing.lg),
        PaymentMethodCard(
          plan: plan,
          operator: operator,
          phone: phone,
          onOperator: onOperator,
        ),
        const SizedBox(height: AppSpacing.lg),
        ChargeSummary(plan: plan),
        const SizedBox(height: AppSpacing.lg),
        const SecureNote(),
      ],
    );
  }
}
