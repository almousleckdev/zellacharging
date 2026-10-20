import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/error_banner.dart';
import '../../../shared/widgets/primary_button.dart';
import '../domain/charging_session.dart';
import '../domain/session_state.dart';
import 'charge_car_graphic.dart';
import 'charging_metrics.dart';
import 'session_formatting.dart';
import 'session_providers.dart';
import 'stop_confirm_sheet.dart';

class ChargingPage extends ConsumerStatefulWidget {
  const ChargingPage({super.key, required this.sessionId});

  final String sessionId;

  @override
  ConsumerState<ChargingPage> createState() => _ChargingPageState();
}

class _ChargingPageState extends ConsumerState<ChargingPage> with SubmitMixin {
  Future<void> _stop() async {
    if (!await confirmStop(context)) return;
    await submit(() async {
      await ref.read(sessionRepositoryProvider).stop(widget.sessionId);
      ref.invalidate(liveSessionProvider(widget.sessionId));
      ref.invalidate(activeSessionProvider);
    });
  }

  String _message(ChargingSession session) => switch (session.state) {
    SessionState.requested ||
    SessionState.startAccepted => Strings.waitingForPlug,
    SessionState.stopping => Strings.finishing,
    SessionState.failed => Strings.sessionFailed,
    SessionState.completed || SessionState.abandoned => Strings.sessionDone,
    SessionState.active => session.stateLabel,
  };

  @override
  Widget build(BuildContext context) {
    final live = ref.watch(liveSessionProvider(widget.sessionId));
    return Scaffold(
      appBar: AppBar(title: const Text(Strings.chargingTitle)),
      body: AsyncBody(
        value: live,
        onRetry: () => ref.invalidate(liveSessionProvider(widget.sessionId)),
        builder: (session) => ListView(
          padding: const EdgeInsets.all(AppSpacing.md),
          children: [
            Text(
              '${session.stationId} · ${Strings.connectorLabel} ${session.connectorId}',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.titleMedium,
            ),
            if (session.state != SessionState.active) ...[
              const SizedBox(height: AppSpacing.xs),
              Text(
                _message(session),
                textAlign: TextAlign.center,
                style: const TextStyle(color: AppColors.textSecondary),
              ),
            ],
            const SizedBox(height: AppSpacing.lg),
            ChargeCarGraphic(session: session),
            const SizedBox(height: AppSpacing.lg),
            if (errorMessage != null) ...[
              ErrorBanner(message: errorMessage!),
              const SizedBox(height: AppSpacing.md),
            ],
            ChargingMetrics(session: session),
            const SizedBox(height: AppSpacing.lg),
            if (session.state.canStop)
              PrimaryButton(
                label: Strings.stopCharging,
                icon: Icons.stop_circle_outlined,
                loading: loading,
                onPressed: _stop,
              ),
          ],
        ),
      ),
    );
  }
}
