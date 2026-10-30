import 'package:flutter/material.dart';

import '../../../../core/format/date_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/info_card.dart';
import '../../../../shared/widgets/info_row.dart';
import '../../domain/station.dart';

class StationInfoCard extends StatelessWidget {
  const StationInfoCard({super.key, required this.station});

  final Station station;

  @override
  Widget build(BuildContext context) {
    final lastSeen = station.lastSeenAt;
    final iso = station.isoCode;
    return InfoCard(
      title: StationDetailText.infoTitle,
      children: [
        InfoRow(
          label: StationDetailText.status,
          value: station.online
              ? StationDetailText.online
              : StationText.stationOffline,
        ),
        if (lastSeen != null)
          InfoRow(
            label: StationDetailText.lastSeen,
            value: DateFormatter.dateTime(lastSeen),
          ),
        if (station.vendor != null)
          InfoRow(
            label: StationDetailText.manufacturer,
            value: station.vendor!,
          ),
        if (station.model != null)
          InfoRow(label: StationDetailText.model, value: station.model!),
        if (iso != null)
          InfoRow(
            label: StationDetailText.country,
            value: StationDetailText.countryName(iso),
          ),
        if (station.latitude != null && station.longitude != null)
          InfoRow(
            label: StationDetailText.coordinates,
            value:
                '${station.latitude!.toStringAsFixed(5)}, ${station.longitude!.toStringAsFixed(5)}',
          ),
      ],
    );
  }
}
