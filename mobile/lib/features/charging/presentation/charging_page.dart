import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/router/routes.dart';
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
import 'delete_session.dart';
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

  Future<void> _delete() async {
    final deleted = await confirmAndDeleteSession(
      context,
      ref,
      widget.sessionId,
    );
    if (!deleted || !mounted) return;
    context.canPop() ? context.pop() : context.go(Routes.history);
  }

  @override
  Widget build(BuildContext context) {
    final live = ref.watch(liveSessionProvider(widget.sessionId));
    final session = live.asData?.value;
    final finished = session != null && !session.state.isLive;
    return Scaffold(
      backgroundColor: finished ? AppColors.surfaceMuted : null,
      appBar: AppBar(
        title: Text(
          finished ? HistoryText.detailsTitle : ChargeText.chargingTitle,
        ),
        actions: [
          if (finished)
            IconButton(
              tooltip: HistoryText.deleteCharge,
              icon: Icon(AppIcons.trash, color: AppColors.danger),
              onPressed: _delete,
            ),
        ],
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
          padding: const EdgeInsets.all(AppSpacing.md),
          children: [
            ChargeScene(session: session),
            const SizedBox(height: AppSpacing.lg),
            ChargingMetrics(session: session),
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
