import 'package:flutter_test/flutter_test.dart';
import 'package:zella/features/stations/domain/connector_sync.dart';
import 'package:zella/features/stations/domain/connector_watch.dart';
import 'package:zella/features/stations/domain/station_connector.dart';

StationConnector _c(int id, String status, {bool startable = false}) =>
    StationConnector(
      connectorId: id,
      status: status,
      available: status == 'Available',
      startable: startable || status == 'Available' || status == 'Preparing',
    );

void main() {
  group('ConnectorWatch', () {
    test('the first look reports nothing, so existing cables are not news', () {
      final watch = ConnectorWatch();
      expect(
        watch.pluggedIn([_c(1, 'Preparing'), _c(2, 'Available')]),
        isEmpty,
      );
    });

    test('a connector that turns to Preparing is reported once', () {
      final watch = ConnectorWatch();
      watch.pluggedIn([_c(1, 'Available'), _c(2, 'Available')]);
      expect(watch.pluggedIn([_c(1, 'Available'), _c(2, 'Preparing')]), [2]);
      expect(
        watch.pluggedIn([_c(1, 'Available'), _c(2, 'Preparing')]),
        isEmpty,
      );
    });

    test('two cables at once are both reported', () {
      final watch = ConnectorWatch();
      watch.pluggedIn([_c(1, 'Available'), _c(2, 'Available')]);
      expect(watch.pluggedIn([_c(1, 'Preparing'), _c(2, 'Preparing')]), [1, 2]);
    });
  });

  group('ConnectorSync', () {
    final both = [_c(1, 'Available'), _c(2, 'Charging')];

    test('a chosen connector that is still startable is kept', () {
      final decision = ConnectorSync.decide(
        connectors: both,
        chosen: 1,
        plugged: const [],
        firstCheck: false,
      );
      expect(decision.action, SelectionAction.keep);
    });

    test(
      'a chosen connector that became busy is dropped with the right reason',
      () {
        expect(
          ConnectorSync.decide(
            connectors: both,
            chosen: 2,
            plugged: const [],
            firstCheck: true,
          ).action,
          SelectionAction.dropBusyAtOpen,
        );
        expect(
          ConnectorSync.decide(
            connectors: both,
            chosen: 2,
            plugged: const [],
            firstCheck: false,
          ).action,
          SelectionAction.dropTaken,
        );
      },
    );

    test('one new cable is adopted when nothing is chosen', () {
      final decision = ConnectorSync.decide(
        connectors: both,
        chosen: null,
        plugged: const [1],
        firstCheck: false,
      );
      expect(decision.action, SelectionAction.adoptPlugged);
      expect(decision.connectorId, 1);
    });

    test('several new cables ask the customer instead of guessing', () {
      final decision = ConnectorSync.decide(
        connectors: both,
        chosen: null,
        plugged: const [1, 2],
        firstCheck: false,
      );
      expect(decision.action, SelectionAction.askWhichPlugged);
      expect(decision.connectorId, isNull);
    });

    test('a plugged-in connector is selectable even though it is not free', () {
      expect(_c(1, 'Preparing').startable, isTrue);
      expect(_c(1, 'Preparing').available, isFalse);
      expect(_c(1, 'Charging').startable, isFalse);
    });
  });
}
