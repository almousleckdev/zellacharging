import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/l10n/strings.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../shared/submit_mixin.dart';
import '../../../shared/widgets/async_body.dart';
import '../../../shared/widgets/error_banner.dart';
import '../../../shared/widgets/primary_button.dart';
import '../domain/charging_session.dart';
import 'charge_scene.dart';
import 'charging_metrics.dart';
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

  @override
  Widget build(BuildContext context) {
    final live = ref.watch(liveSessionProvider(widget.sessionId));
    return Scaffold(
      appBar: AppBar(title: const Text(Strings.chargingTitle)),
      body: AsyncBody(
        value: live,
        onRetry: () => ref.invalidate(liveSessionProvider(widget.sessionId)),
        builder: (session) => Column(
          children: [
            Expanded(child: _details(session)),
            if (session.state.canStop)
              SafeArea(
                top: false,
                child: Padding(
                  padding: const EdgeInsets.all(AppSpacing.md),
                  child: PrimaryButton(
                    label: Strings.stopCharging,
                    icon: Icons.stop_circle_outlined,
                    loading: loading,
                    onPressed: _stop,
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _details(ChargingSession session) => ListView(
    padding: const EdgeInsets.all(AppSpacing.md),
    children: [
      ChargeScene(session: session),
      const SizedBox(height: AppSpacing.lg),
      if (errorMessage != null) ...[
        ErrorBanner(message: errorMessage!),
        const SizedBox(height: AppSpacing.md),
      ],
      ChargingMetrics(session: session),
    ],
  );
}
