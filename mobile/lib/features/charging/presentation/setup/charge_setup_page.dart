import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../core/router/routes.dart';
import '../../../../shared/app_toast.dart';
import '../../../../shared/widgets/async_body.dart';
import '../../../stations/presentation/station_providers.dart';
import '../../domain/charge_plan.dart';
import '../../domain/charge_request.dart';
import '../charge_flow_controller.dart';
import '../charge_flow_state.dart';
import 'charge_action_bar.dart';
import 'charge_flow_view.dart';
import 'connector_selection_mixin.dart';
import 'setup_form.dart';
import 'watch_charge_plan.dart';

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
    with ConnectorSelectionMixin {
  final _formKey = GlobalKey<FormState>();
  late String _attempt = _newAttempt();
  Timer? _refresh;

  @override
  void initState() {
    super.initState();
    connector = widget.initialConnectorId;
    _refresh = Timer.periodic(
      const Duration(seconds: 5),
      (_) => ref.invalidate(stationProvider(widget.stationId)),
    );
  }

  @override
  void dispose() {
    _refresh?.cancel();
    super.dispose();
  }

  String _newAttempt() => DateTime.now().microsecondsSinceEpoch.toString();

  Future<void> _start(ChargePlan plan) async {
    if (plan.needsTopup) {
      context.push(Routes.topup);
      return;
    }
    final chosen = connector;
    if (chosen == null) {
      AppToast.warning(context, PayText.selectConnectorFirst);
      return;
    }
    final session = await ref
        .read(chargeFlowProvider.notifier)
        .launch(
          ChargeRequest(
            stationId: widget.stationId,
            connectorId: chosen,
            amountMinor: plan.amountMinor,
            attemptKey: _attempt,
          ),
        );
    if (session != null && mounted) {
      context.pushReplacement(Routes.chargingPath(session.id));
    }
  }

  void _retry() {
    final error = ref.read(chargeFlowProvider).error;
    if (error != null && !error.isUncertain) _attempt = _newAttempt();
    ref.read(chargeFlowProvider.notifier).reset();
  }

  @override
  Widget build(BuildContext context) {
    ref.listen(stationProvider(widget.stationId), (_, next) {
      next.whenData(syncSelection);
    });
    final flow = ref.watch(chargeFlowProvider);
    final plan = watchChargePlan(
      ref,
      stationId: widget.stationId,
      connector: connector,
    );
    final idle = flow.phase == ChargePhase.idle;
    return Scaffold(
      appBar: AppBar(title: Text(ChargeText.setupTitle)),
      body: idle
          ? AsyncBody(
              value: ref.watch(stationProvider(widget.stationId)),
              onRetry: () => ref.invalidate(stationProvider(widget.stationId)),
              builder: (station) => SetupForm(
                formKey: _formKey,
                station: station,
                plan: plan,
                connector: connector,
                onConnector: (value) => setState(() => connector = value),
              ),
            )
          : ChargeFlowView(
              flow: flow,
              onRetry: _retry,
              onClose: () => context.pop(),
            ),
      bottomNavigationBar: idle
          ? ChargeActionBar(
              plan: plan,
              connectorChosen: connector != null,
              onPressed: () => _start(plan),
            )
          : null,
    );
  }
}
