import 'station_connector.dart';

enum SelectionAction {
  keep,
  dropBusyAtOpen,
  dropTaken,
  adoptPlugged,
  askWhichPlugged,
  none,
}

class SelectionDecision {
  const SelectionDecision(this.action, [this.connectorId]);

  final SelectionAction action;
  final int? connectorId;
}

abstract final class ConnectorSync {
  static SelectionDecision decide({
    required List<StationConnector> connectors,
    required int? chosen,
    required List<int> plugged,
    required bool firstCheck,
  }) {
    if (chosen != null) {
      final current = connectors.where((c) => c.connectorId == chosen);
      if (current.isNotEmpty && current.first.startable) {
        return const SelectionDecision(SelectionAction.keep);
      }
      return SelectionDecision(
        firstCheck ? SelectionAction.dropBusyAtOpen : SelectionAction.dropTaken,
      );
    }
    if (plugged.length == 1) {
      return SelectionDecision(SelectionAction.adoptPlugged, plugged.first);
    }
    return SelectionDecision(
      plugged.length > 1
          ? SelectionAction.askWhichPlugged
          : SelectionAction.none,
    );
  }
}
