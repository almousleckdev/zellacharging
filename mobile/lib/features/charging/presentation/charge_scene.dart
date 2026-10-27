import 'package:flutter/material.dart';

import '../../../core/theme/app_spacing.dart';
import '../domain/charging_session.dart';
import '../domain/session_state.dart';
import 'battery_freshness.dart';
import 'car_stage.dart';
import 'charge_hero.dart';
import 'session_formatting.dart';

class ChargeScene extends StatelessWidget {
  const ChargeScene({super.key, required this.session});

  final ChargingSession session;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        ChargeHero(session: session),
        BatteryFreshness(session: session),
        const SizedBox(height: AppSpacing.md),
        CarStage(
          level: session.chargeLevel,
          charging: session.state == SessionState.active,
        ),
      ],
    );
  }
}
