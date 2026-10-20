import 'package:ndzela/core/location/coordinates.dart';
import 'package:ndzela/core/location/location_service.dart';
import 'package:ndzela/features/charging/domain/charging_session.dart';
import 'package:ndzela/features/charging/domain/metering.dart';
import 'package:ndzela/features/charging/domain/session_repository.dart';
import 'package:ndzela/features/charging/domain/session_state.dart';
import 'package:ndzela/features/profile/domain/profile.dart';
import 'package:ndzela/features/profile/domain/profile_repository.dart';
import 'package:ndzela/features/stations/domain/resolved_connector.dart';
import 'package:ndzela/features/stations/domain/station.dart';
import 'package:ndzela/features/stations/domain/station_connector.dart';
import 'package:ndzela/features/stations/domain/station_repository.dart';
import 'package:ndzela/features/wallet/domain/mobile_operator.dart';
import 'package:ndzela/features/wallet/domain/topup_result.dart';
import 'package:ndzela/features/wallet/domain/wallet.dart';
import 'package:ndzela/features/wallet/domain/wallet_repository.dart';

const connectors = [
  StationConnector(
    connectorId: 1,
    status: 'Available',
    available: true,
    connectorType: 'GB/T DC',
    maxPowerKw: 60,
  ),
  StationConnector(connectorId: 2, status: 'SuspendedEVSE', available: false),
];

final stations = [
  const Station(
    id: 'DY202609110007-VERY-LONG-STATION-IDENTIFIER',
    online: true,
    availableConnectors: 1,
    totalConnectors: 2,
    connectors: connectors,
    distanceKm: 12.4,
  ),
  const Station(
    id: 'CP-SITE-001',
    online: false,
    availableConnectors: 0,
    totalConnectors: 2,
    connectors: connectors,
  ),
];

ChargingSession session(SessionState state, {int? cost}) => ChargingSession(
  id: 's-$state',
  stationId: 'DY202609110007-VERY-LONG-STATION-IDENTIFIER',
  connectorId: 2,
  state: state,
  createdAt: DateTime(2026, 10, 16, 14, 30),
  energyKwh: 125.43,
  costMinor: cost,
  currency: 'XAF',
  currencyDecimals: 0,
  metering: const Metering(
    totalKwh: 125.43,
    powerKw: 59.9,
    stateOfChargePercent: 67,
    durationSeconds: 7385,
    voltage: 402,
    currentAmpere: 148.9,
  ),
);

class FakeStations implements StationRepository {
  @override
  Future<List<Station>> nearby(Coordinates? around) async => stations;

  @override
  Future<Station> byId(String id) async => stations.first;

  @override
  Future<ResolvedConnector> resolveQr(String content) async =>
      ResolvedConnector(station: stations.first, connector: connectors.first);
}

class FakeSessions implements SessionRepository {
  FakeSessions(this.state);

  final SessionState state;

  @override
  Future<ChargingSession> start(String s, int c, int a) async => session(state);

  @override
  Future<ChargingSession> stop(String id) async => session(state);

  @override
  Future<ChargingSession> get(String id) async => session(state, cost: 12500);

  @override
  Future<ChargingSession?> active() async => session(SessionState.active);

  @override
  Future<List<ChargingSession>> history() async => [
    session(SessionState.completed, cost: 1234567),
    session(SessionState.failed),
    session(SessionState.active),
  ];
}

class FakeWallet implements WalletRepository {
  @override
  Future<Wallet> fetch() async => const Wallet(
    balanceMinor: 1234567,
    reservedMinor: 250000,
    availableMinor: 984567,
    currency: 'XAF',
    decimals: 0,
  );

  @override
  Future<TopupResult> topUp({
    required int amountMinor,
    required String msisdn,
    required MobileOperator operator,
    required String idempotencyKey,
  }) async => const TopupResult(id: 't', state: 'SUCCEEDED');

  @override
  Future<TopupResult> topupStatus(String id) async =>
      const TopupResult(id: 't', state: 'SUCCEEDED');
}

class FakeProfile implements ProfileRepository {
  @override
  Future<Profile> fetch() async => const Profile(
    firstName: 'Alexandrine-Marguerite',
    lastName: 'Montgomery-Dubois',
    email: 'alexandrine.montgomery-dubois@very-long-company-domain.example.com',
    phone: '+24177123456',
  );
}

class NoLocation implements LocationService {
  @override
  Future<Coordinates?> currentPosition() async => null;
}
