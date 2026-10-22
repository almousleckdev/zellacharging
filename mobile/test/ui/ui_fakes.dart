import 'dart:convert';
import 'dart:typed_data';

import 'package:ndzela/core/links/link_launcher.dart';
import 'package:ndzela/core/location/coordinates.dart';
import 'package:ndzela/core/media/photo_picker.dart';
import 'package:ndzela/core/media/photo_source.dart';
import 'package:ndzela/core/media/picked_photo.dart';
import 'package:ndzela/core/network/api_error.dart';
import 'package:ndzela/core/session/session_controller.dart';
import 'package:ndzela/core/storage/preference_store.dart';
import 'package:ndzela/features/coupons/domain/coupon.dart';
import 'package:ndzela/features/coupons/domain/coupon_repository.dart';
import 'package:ndzela/features/support/domain/support_info.dart';
import 'package:ndzela/features/support/domain/support_repository.dart';
import 'package:ndzela/core/location/location_service.dart';
import 'package:ndzela/features/charging/domain/charging_session.dart';
import 'package:ndzela/features/charging/domain/metering.dart';
import 'package:ndzela/features/charging/domain/session_fees.dart';
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
  batteryPercent: 67,
  lastVoltage: 401,
  fees: cost == null
      ? null
      : SessionFees(
          electricMinor: cost,
          vatMinor: 0,
          serviceMinor: 0,
          totalMinor: cost,
          currency: 'XAF',
          decimals: 0,
        ),
  metering: const Metering(
    totalKwh: 125.43,
    powerKw: 59.9,
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
  final deleted = <String>[];

  @override
  Future<void> delete(String sessionId) async => deleted.add(sessionId);

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
  Uint8List? avatar;
  final uploads = <PickedPhoto>[];
  var avatarRemoved = 0;
  final passwordChanges = <String>[];
  final closedWith = <String>[];
  final renamed = <String>[];

  @override
  Future<Profile> fetch() async => const Profile(
    firstName: 'Alexandrine-Marguerite',
    lastName: 'Montgomery-Dubois',
    email: 'alexandrine.montgomery-dubois@very-long-company-domain.example.com',
    phone: '+24177123456',
  );

  @override
  Future<Profile> updateName(String firstName, String lastName) async {
    renamed.add('$firstName $lastName');
    return Profile(firstName: firstName, lastName: lastName);
  }

  @override
  Future<Uint8List?> fetchAvatar() async => avatar;

  @override
  Future<void> uploadAvatar(Uint8List bytes, String contentType) async {
    uploads.add(PickedPhoto(bytes: bytes, contentType: contentType));
    avatar = bytes;
  }

  @override
  Future<void> deleteAvatar() async {
    avatarRemoved++;
    avatar = null;
  }

  @override
  Future<void> changePassword(
    String currentPassword,
    String newPassword,
  ) async {
    if (currentPassword == 'wrong') {
      throw ApiError.fromCode('WRONG_PASSWORD');
    }
    passwordChanges.add(newPassword);
  }

  @override
  Future<void> deleteAccount(String password) async {
    if (password == 'wrong') throw ApiError.fromCode('WRONG_PASSWORD');
    closedWith.add(password);
  }
}

final tinyPng = base64Decode(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
);

class FakeCoupons implements CouponRepository {
  FakeCoupons([this.coupons = const []]);

  final List<Coupon> coupons;

  @override
  Future<List<Coupon>> mine() async => coupons;
}

class FakeSupport implements SupportRepository {
  FakeSupport([this.info = const SupportInfo()]);

  final SupportInfo info;

  @override
  Future<SupportInfo> fetch() async => info;
}

class FakePhotoPicker implements PhotoPicker {
  final requested = <PhotoSource>[];

  @override
  Future<PickedPhoto?> pick(PhotoSource source) async {
    requested.add(source);
    return PickedPhoto(bytes: tinyPng, contentType: 'image/jpeg');
  }
}

class FakeLinks implements LinkLauncher {
  final opened = <Uri>[];

  @override
  Future<bool> open(Uri link) async {
    opened.add(link);
    return true;
  }
}

class MemoryPreferences implements PreferenceStore {
  final values = <String, String>{};

  @override
  Future<String?> read(String key) async => values[key];

  @override
  Future<void> save(String key, String value) async => values[key] = value;
}

class FakeSession extends SessionController {
  static var signOuts = 0;

  @override
  SessionStatus build() => SessionStatus.authenticated;

  @override
  Future<void> signOut() async {
    signOuts++;
    state = SessionStatus.unauthenticated;
  }
}

class NoLocation implements LocationService {
  @override
  Future<Coordinates?> currentPosition() async => null;
}
