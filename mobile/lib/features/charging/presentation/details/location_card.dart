import 'package:flutter/material.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/info_card.dart';
import '../../../../shared/widgets/info_row.dart';
import '../../../stations/domain/station.dart';
import '../../domain/charging_session.dart';

class LocationCard extends StatelessWidget {
  const LocationCard({super.key, required this.session, this.station});

  final ChargingSession session;
  final Station? station;

  String? get _coordinates {
    final latitude = station?.latitude;
    final longitude = station?.longitude;
    if (latitude == null || longitude == null) return null;
    return '${latitude.toStringAsFixed(5)}, ${longitude.toStringAsFixed(5)}';
  }

  @override
  Widget build(BuildContext context) {
    final coordinates = _coordinates;
    return InfoCard(
      title: HistoryText.locationTitle,
      children: [
        InfoRow(label: StationText.stationDetails, value: session.stationId),
        InfoRow(label: StationText.connector, value: '${session.connectorId}'),
        if (coordinates != null)
          InfoRow(label: HistoryText.gps, value: coordinates),
      ],
    );
  }
}
