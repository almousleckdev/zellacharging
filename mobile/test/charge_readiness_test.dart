import 'package:flutter_test/flutter_test.dart';
import 'package:zella/features/charging/domain/charge_plan.dart';
import 'package:zella/features/charging/domain/charge_readiness.dart';
import 'package:zella/features/stations/domain/station_connector.dart';
import 'package:zella/features/wallet/domain/wallet.dart';

const _wallet = Wallet(
  balanceMinor: 5000,
  reservedMinor: 0,
  availableMinor: 5000,
  currency: 'XAF',
  decimals: 0,
);

const _free = Wallet(
  balanceMinor: 0,
  reservedMinor: 0,
  availableMinor: 0,
  currency: 'XAF',
  decimals: 0,
  freeCharging: true,
);

StationConnector _connector(String status) => StationConnector(
  connectorId: 1,
  status: status,
  available: status == 'Available',
  startable: status == 'Available' || status == 'Preparing',
);

ChargeReadiness _check(int amount, StationConnector? connector, [Wallet? w]) =>
    ChargeReadiness.of(
      plan: ChargePlan.of(amountMinor: amount, wallet: w ?? _wallet),
      connector: connector,
    );

void main() {
  final plugged = _connector('Preparing');

  test('a plugged connector and a valid amount can start', () {
    expect(_check(2000, plugged), ChargeReadiness.ready);
  });

  test('an unplugged connector blocks the start', () {
    expect(_check(2000, _connector('Available')), ChargeReadiness.plugIn);
  });

  test('no connector chosen blocks the start', () {
    expect(_check(2000, null), ChargeReadiness.chooseConnector);
  });

  test('an empty amount asks for one', () {
    expect(_check(0, plugged), ChargeReadiness.enterAmount);
  });

  test('an amount under the minimum is refused', () {
    expect(_check(100, plugged), ChargeReadiness.amountTooLow);
  });

  test('an amount above the balance is refused', () {
    expect(_check(8000, plugged), ChargeReadiness.exceedsBalance);
  });

  test('an amount above the maximum is refused', () {
    expect(_check(300000, plugged), ChargeReadiness.amountTooHigh);
  });

  test('a free account starts without any amount', () {
    expect(
      _check(ChargePlan.maximumChargeMinor, plugged, _free),
      ChargeReadiness.ready,
    );
  });

  test('a free account still needs a plugged connector', () {
    expect(
      _check(ChargePlan.maximumChargeMinor, _connector('Available'), _free),
      ChargeReadiness.plugIn,
    );
  });
}
