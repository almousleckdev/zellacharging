import 'station_connector.dart';

class ConnectorWatch {
  Map<int, String>? _last;

  List<int> pluggedIn(List<StationConnector> connectors) {
    final previous = _last;
    _last = {for (final c in connectors) c.connectorId: c.status};
    if (previous == null) return const [];
    return [
      for (final connector in connectors)
        if (connector.status == 'Preparing' &&
            previous[connector.connectorId] != 'Preparing')
          connector.connectorId,
    ];
  }
}
