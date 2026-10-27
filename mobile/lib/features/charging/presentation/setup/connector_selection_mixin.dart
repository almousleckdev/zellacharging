import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/l10n/strings.dart';
import '../../../../shared/app_toast.dart';
import '../../../stations/domain/connector_sync.dart';
import '../../../stations/domain/connector_watch.dart';
import '../../../stations/domain/station.dart';

mixin ConnectorSelectionMixin<T extends ConsumerStatefulWidget>
    on ConsumerState<T> {
  final _watch = ConnectorWatch();
  bool _checked = false;
  int? connector;

  void syncSelection(Station station) {
    final decision = ConnectorSync.decide(
      connectors: station.connectors,
      chosen: connector,
      plugged: _watch.pluggedIn(station.connectors),
      firstCheck: !_checked,
    );
    _checked = true;
    switch (decision.action) {
      case SelectionAction.dropBusyAtOpen:
        setState(() => connector = null);
        AppToast.info(context, StationText.connectorBusy);
      case SelectionAction.dropTaken:
        setState(() => connector = null);
        AppToast.info(context, StationText.connectorTaken);
      case SelectionAction.adoptPlugged:
        setState(() => connector = decision.connectorId);
        AppToast.info(context, PayText.cableDetected(decision.connectorId!));
      case SelectionAction.askWhichPlugged:
        AppToast.info(context, PayText.cableSeveral);
      case SelectionAction.keep || SelectionAction.none:
    }
  }
}
