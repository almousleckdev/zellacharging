import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_icons.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/adaptive_dialog.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/primary_button.dart';
import '../domain/charging_session.dart';
import 'charge_scene.dart';
import 'charging_metrics.dart';
import 'details/session_details_view.dart';
import 'session_providers.dart';

class ChargingPage extends ConsumerStatefulWidget {
  const ChargingPage({super.key, required this.sessionId});

  final String sessionId;

  @override
  ConsumerState<ChargingPage> createState() => _ChargingPageState();
}

class _ChargingPageState extends ConsumerState<ChargingPage> with SubmitMixin {
  Future<void> _stop() async {
    final confirmed = await showConfirmDialog(
      context,
      title: ChargeText.stopConfirmTitle,
      message: ChargeText.stopConfirmBody,
      confirmLabel: ChargeText.stopCharging,
      destructive: true,
    );
    if (!confirmed) return;
    await submit(() async {
      await ref.read(sessionRepositoryProvider).stop(widget.sessionId);
      ref.invalidate(liveSessionProvider(widget.sessionId));
      ref.invalidate(activeSessionProvider);
    });
  }

  @override
  Widget build(BuildContext context) {
    final live = ref.watch(liveSessionProvider(widget.sessionId));
    final session = live.value;
    final finished = session != null && !session.state.isLive;
    return Scaffold(
      backgroundColor: finished ? AppColors.surfaceMuted : null,
      extendBodyBehindAppBar: !finished,
      appBar: finished
          ? null
          : AppBar(
              backgroundColor: Colors.transparent,
              title: Text(ChargeText.chargingTitle),
            ),
      body: AsyncBody(
        value: live,
        onRetry: () => ref.invalidate(liveSessionProvider(widget.sessionId)),
        builder: (data) => data.state.isLive
            ? _liveBody(data)
            : SessionDetailsView(session: data),
      ),
    );
  }

  Widget _liveBody(ChargingSession session) => Column(
    children: [
      Expanded(
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            ChargeScene(session: session),
            Padding(
              padding: const EdgeInsets.all(AppSpacing.md),
              child: ChargingMetrics(session: session),
            ),
          ],
        ),
      ),
      if (session.state.canStop)
        SafeArea(
          top: false,
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.md),
            child: PrimaryButton(
              label: ChargeText.stopCharging,
              icon: AppIcons.stopCircle,
              loading: loading,
              onPressed: _stop,
            ),
          ),
        ),
    ],
  );
}
