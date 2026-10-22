import 'package:flutter/material.dart';

import '../../../../core/format/date_format.dart';
import '../../../../core/l10n/strings.dart';
import '../../../../shared/widgets/info_card.dart';
import '../../../../shared/widgets/info_row.dart';
import '../../../stations/domain/station.dart';
import '../../../stations/domain/station_connector.dart';
import '../../domain/charging_session.dart';
import '../session_formatting.dart';

class DeviceInfoCard extends StatelessWidget {
  const DeviceInfoCard({super.key, required this.session, this.station});

  final ChargingSession session;
  final Station? station;

  StationConnector? get _connector {
    final matches = station?.connectors.where(
      (c) => c.connectorId == session.connectorId,
    );
    return matches == null || matches.isEmpty ? null : matches.first;
  }

  String get _dcPower {
    final power = _connector?.maxPowerKw;
    return power == null
        ? '--'
        : '${power.toStringAsFixed(0)} ${Strings.powerUnit}';
  }

  String _time(DateTime? value) =>
      value == null ? '--' : DateFormatter.dateTime(value);

  @override
  Widget build(BuildContext context) {
    return InfoCard(
      title: HistoryText.deviceInfo,
      children: [
        InfoRow(label: HistoryText.deviceNo, value: session.stationId),
        InfoRow(label: HistoryText.dcPower, value: _dcPower),
        InfoRow(label: HistoryText.voltageLabel, value: session.voltageText),
        InfoRow(label: HistoryText.startTime, value: _time(session.startedAt)),
        InfoRow(label: HistoryText.endTime, value: _time(session.stoppedAt)),
      ],
    );
  }
}
