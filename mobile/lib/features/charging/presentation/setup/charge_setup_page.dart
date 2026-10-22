import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/router/routes.dart';
import '../../../../core/network/api_error.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../shared/app_toast.dart';
import '../../../../shared/submit_mixin.dart';
import '../../../../shared/widgets/async_body.dart';
import '../../../../shared/widgets/labeled_section.dart';
import '../../../profile/presentation/profile_providers.dart';
import '../../../stations/domain/station.dart';
import '../../../stations/presentation/station_providers.dart';
import '../../../wallet/domain/mobile_operator.dart';
import '../../../wallet/presentation/wallet_providers.dart';
import '../../domain/charge_quote.dart';
import '../../domain/charge_request.dart';
import 'amount_section.dart';
import 'charge_launcher.dart';
import 'connector_picker.dart';
import 'payment_section.dart';
import 'setup_header.dart';
import 'start_button.dart';

class ChargeSetupPage extends ConsumerStatefulWidget {
  const ChargeSetupPage({
    super.key,
    required this.stationId,
    this.initialConnectorId,
  });

  final String stationId;
  final int? initialConnectorId;

  @override
  ConsumerState<ChargeSetupPage> createState() => _ChargeSetupPageState();
}

class _ChargeSetupPageState extends ConsumerState<ChargeSetupPage>
    with SubmitMixin {
  final _formKey = GlobalKey<FormState>();
  final _amount = TextEditingController(text: '2000');
  late final _phone = TextEditingController(
    text: ref.read(profileProvider).asData?.value.phone ?? '',
  );
  late int? _connector = widget.initialConnectorId;
  MobileOperator _operator = MobileOperator.airtel;
  late String _attempt = _newAttempt();
  Timer? _refresh;

  @override
  void initState() {
    super.initState();
    _refresh = Timer.periodic(
      const Duration(seconds: 5),
      (_) => ref.invalidate(stationProvider(widget.stationId)),
    );
  }

  void _syncSelection(Station station) {
    final first = !_checked;
    _checked = true;
    final chosen = _connector;
    if (chosen == null) return;
    final current = station.connectors.where((c) => c.connectorId == chosen);
    if (current.isNotEmpty && current.first.available) return;
    setState(() => _connector = null);
    AppToast.info(
      context,
      first ? StationText.connectorBusy : StationText.connectorTaken,
    );
  }

  bool _checked = false;

  String _newAttempt() => DateTime.now().microsecondsSinceEpoch.toString();

  @override
  void dispose() {
    _refresh?.cancel();
    _amount.dispose();
    _phone.dispose();
    super.dispose();
  }

  ChargeQuote get _quote => ChargeQuote.of(
    int.tryParse(_amount.text) ?? 0,
    ref.watch(walletProvider).asData?.value,
  );

  void _start() {
    final connector = _connector;
    if (connector == null) {
      showError(StationText.selectConnector);
      return;
    }
    if (!_formKey.currentState!.validate()) return;
    submit(() async {
      final request = ChargeRequest(
        stationId: widget.stationId,
        connectorId: connector,
        amountMinor: int.parse(_amount.text),
        attemptKey: _attempt,
        payment: _quote.needsPayment
            ? ChargePayment(operator: _operator, msisdn: _phone.text.trim())
            : null,
      );
      try {
        final session = await ChargeLauncher(ref).launch(request);
        if (mounted) context.pushReplacement(Routes.chargingPath(session.id));
      } on ApiError {
        _attempt = _newAttempt();
        rethrow;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    ref.listen(stationProvider(widget.stationId), (_, next) {
      next.whenData(_syncSelection);
    });
    final station = ref.watch(stationProvider(widget.stationId));
    final currency = ref.watch(walletProvider).asData?.value.currency ?? '';
    return Scaffold(
      appBar: AppBar(title: Text(ChargeText.setupTitle)),
      body: AsyncBody(
        value: station,
        onRetry: () => ref.invalidate(stationProvider(widget.stationId)),
        builder: (data) => _form(data, currency),
      ),
    );
  }

  Widget _form(Station station, String currency) {
    final quote = _quote;
    return Form(
      key: _formKey,
      child: ListView(
        padding: const EdgeInsets.all(AppSpacing.lg),
        children: [
          SetupHeader(title: station.id),
          LabeledSection(
            title: ChargeText.setupAmountQuestion,
            child: AmountSection(
              controller: _amount,
              currency: currency,
              onChanged: () => setState(() {}),
            ),
          ),
          LabeledSection(
            title: StationText.chooseConnector,
            child: ConnectorPicker(
              connectors: station.connectors,
              selected: _connector,
              onSelected: (value) => setState(() => _connector = value),
            ),
          ),
          LabeledSection(
            title: ChargeText.payment,
            child: PaymentSection(
              quote: quote,
              currency: currency,
              operator: _operator,
              phone: _phone,
              onOperator: (value) => setState(() => _operator = value),
            ),
          ),
          const SizedBox(height: AppSpacing.xl),
          StartButton(
            quote: quote,
            currency: currency,
            loading: loading,
            onPressed: _start,
          ),
        ],
      ),
    );
  }
}
