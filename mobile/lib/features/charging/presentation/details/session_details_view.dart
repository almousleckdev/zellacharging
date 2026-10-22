import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../stations/presentation/station_providers.dart';
import '../../domain/charging_session.dart';
import '../car_scene.dart';
import '../session_formatting.dart';
import '../session_message_card.dart';
import 'details_summary.dart';
import 'device_info_card.dart';
import 'fee_details_card.dart';
import 'location_card.dart';

class SessionDetailsView extends ConsumerWidget {
  const SessionDetailsView({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final station = ref.watch(stationProvider(session.stationId)).asData?.value;
    final message = session.statusMessage;
    return ListView(
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        FractionallySizedBox(
          widthFactor: 0.8,
          child: CarScene(level: session.chargeLevel, charging: false),
        ),
        const SizedBox(height: AppSpacing.md),
        if (message != null) ...[
          SessionMessageCard(message: message, color: session.stateColor),
          const SizedBox(height: AppSpacing.md),
        ],
        DetailsSummary(session: session),
        const SizedBox(height: AppSpacing.md),
        LocationCard(session: session, station: station),
        const SizedBox(height: AppSpacing.md),
        FeeDetailsCard(session: session),
        const SizedBox(height: AppSpacing.md),
        DeviceInfoCard(session: session, station: station),
      ],
    );
  }
}
