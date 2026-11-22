import 'dart:convert';
import 'package:flutter/painting.dart';
import 'package:flutter_map/flutter_map.dart';
import 'dart:typed_data';

import 'package:zella/core/links/link_launcher.dart';
import 'package:zella/core/location/coordinates.dart';
import 'package:zella/core/media/photo_picker.dart';
import 'package:zella/core/media/photo_source.dart';
import 'package:zella/core/media/picked_photo.dart';
import 'package:zella/core/network/api_error.dart';
import 'package:zella/core/session/session_controller.dart';
import 'package:zella/core/storage/preference_store.dart';
import 'package:zella/features/auth/domain/social_authenticator.dart';
import 'package:zella/features/auth/domain/social_credential.dart';
import 'package:zella/features/auth/domain/social_provider.dart';
import 'package:zella/features/coupons/domain/coupon.dart';
import 'package:zella/features/free_sunday/domain/free_sunday_offer.dart';
import 'package:zella/features/free_sunday/domain/free_sunday_repository.dart';
import 'package:zella/features/coupons/domain/coupon_repository.dart';
import 'package:zella/features/support/domain/support_info.dart';
import 'package:zella/features/support/domain/support_repository.dart';
import 'package:zella/core/location/location_service.dart';
import 'package:zella/features/charging/domain/charging_session.dart';
import 'package:zella/features/charging/domain/metering.dart';
import 'package:zella/features/charging/domain/session_fees.dart';
import 'package:zella/features/charging/domain/session_quote.dart';
import 'package:zella/features/charging/domain/session_repository.dart';
import 'package:zella/features/charging/domain/session_state.dart';
import 'package:zella/features/profile/domain/profile.dart';
import 'package:zella/features/auth/domain/auth_repository.dart';
import 'package:zella/features/auth/domain/contact_channel.dart';
import 'package:zella/features/auth/domain/registration_data.dart';
import 'package:zella/features/auth/domain/verification_target.dart';
import 'package:zella/shared/otp_delivery.dart';
import 'package:zella/features/profile/domain/profile_repository.dart';
import 'package:zella/features/stations/domain/resolved_connector.dart';
import 'package:zella/features/stations/domain/station.dart';
import 'package:zella/features/stations/domain/station_connector.dart';
import 'package:zella/features/stations/domain/station_repository.dart';
import 'package:zella/features/wallet/domain/mobile_operator.dart';
import 'package:zella/features/wallet/domain/payout_number.dart';
import 'package:zella/features/wallet/domain/topup_result.dart';
import 'package:zella/features/wallet/domain/wallet.dart';
import 'package:zella/features/wallet/domain/wallet_entry.dart';
import 'package:zella/features/wallet/domain/wallet_entry_detail.dart';

import 'package:zella/features/wallet/domain/withdrawal_result.dart';

import 'package:zella/features/wallet/domain/wallet_repository.dart';

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
    latitude: 0.4162,
    longitude: 9.4673,
    isoCode: 'GA',
    vendor: 'JC.DW',
    model: 'V1.1',
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
  Future<ChargingSession> start(
    String s,
    int c,
    int a, {
    required String idempotencyKey,
  }) async => session(state);

  @override
  Future<SessionQuote> quote(String s, int c, int a) async => SessionQuote(
    amountMinor: a,
    currency: 'XAF',
    decimals: 0,
    availableMinor: 984567,
    shortfallMinor: a > 984567 ? a - 984567 : 0,
    minimumMinor: 1000,
    maximumMinor: 200000,
    vatPercent: 0,
    serviceFeePercent: 0,
    estimatedEnergyKwh: 13.3,
  );

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
    withdrawalsEnabled: true,
    payoutNumber: '+24177123456',
    payoutOperator: MobileOperator.airtel,
    payoutNumbers: [
      PayoutNumber(msisdn: '+24177123456', operator: MobileOperator.airtel),
    ],
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

  @override
  Future<WithdrawalResult> withdraw({
    required int amountMinor,
    required MobileOperator operator,
    required String msisdn,
    required String password,
    required String idempotencyKey,
  }) async => const WithdrawalResult(id: 'w', state: 'PAID');

  @override
  Future<WithdrawalResult> withdrawalStatus(String id) async =>
      const WithdrawalResult(id: 'w', state: 'PAID');

  @override
  Future<List<WalletEntry>> entries() async => [
    WalletEntry(
      id: '1',
      type: 'TOPUP',
      amountMinor: 5000,
      createdAt: DateTime(2026, 10, 26, 22, 2),
    ),
    WalletEntry(
      id: '2',
      type: 'CAPTURE',
      amountMinor: 2040,
      createdAt: DateTime(2026, 10, 26, 22, 40),
    ),
    WalletEntry(
      id: '3',
      type: 'WITHDRAWAL',
      amountMinor: 1000,
      createdAt: DateTime(2026, 10, 27, 9, 15),
    ),
  ];

  @override
  Future<WalletEntryDetail> entryDetail(String id) async => WalletEntryDetail(
    entry: WalletEntry(
      id: id,
      type: 'TOPUP',
      amountMinor: 5000,
      createdAt: DateTime(2026, 10, 26, 22, 2),
    ),
    balanceAfterMinor: 984567,
    operator: 'MOOV',
    msisdn: '+24162820745',
    state: 'SUCCEEDED',
    reference: 'GAA2FPSPEONV7JHFFMGU',
    providerReference: 'MP26102600123',
  );

  final hidden = <String>[];

  @override
  Future<void> hideEntry(String id) async => hidden.add(id);
}

class FakeProfile implements ProfileRepository {
  Uint8List? avatar;
  final uploads = <PickedPhoto>[];
  var avatarRemoved = 0;
  final passwordChanges = <String>[];
  final closedWith = <String>[];
  final renamed = <String>[];

  Profile served = const Profile(
    firstName: 'Alexandrine-Marguerite',
    lastName: 'Montgomery-Dubois',
    email: 'alexandrine.montgomery-dubois@very-long-company-domain.example.com',
    phone: '+24177123456',
    emailVerified: true,
  );

  @override
  Future<Profile> fetch() async => served;

  @override
  Future<Profile> updateName(String firstName, String lastName) async {
    renamed.add('$firstName $lastName');
    return Profile(firstName: firstName, lastName: lastName);
  }

  final emailsAdded = <String>[];
  final phonesAdded = <String>[];
  final deliveries = <OtpDelivery>[];
  final confirmed = <String>[];

  @override
  Future<void> addEmail(String email) async => emailsAdded.add(email);

  var emailCodesSent = 0;

  @override
  Future<void> sendEmailCode() async => emailCodesSent++;

  @override
  Future<Profile> confirmEmail(String code) async {
    confirmed.add(code);
    return const Profile(firstName: 'A', lastName: 'B', email: 'x@y.z');
  }

  @override
  Future<void> addPhone(String phone, OtpDelivery delivery) async {
    phonesAdded.add(phone);
    deliveries.add(delivery);
  }

  @override
  Future<void> sendPhoneCode(OtpDelivery delivery) async =>
      deliveries.add(delivery);

  @override
  Future<Profile> confirmPhone(String code) async {
    confirmed.add(code);
    return const Profile(firstName: 'A', lastName: 'B', phone: '+24177000000');
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
  Future<void> deleteAccount({
    String? password,
    SocialCredential? credential,
  }) async {
    if (password == 'wrong') throw ApiError.fromCode('WRONG_PASSWORD');
    closedWith.add(password ?? credential!.provider.apiName);
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

class NoTiles extends TileProvider {
  @override
  ImageProvider getImage(TileCoordinates coordinates, TileLayer options) =>
      MemoryImage(tinyPng);
}

class FakeSocialAuthenticator implements SocialAuthenticator {
  FakeSocialAuthenticator({this.cancel = false});

  final bool cancel;
  final asked = <SocialProvider>[];

  @override
  Future<SocialCredential?> authenticate(SocialProvider provider) async {
    asked.add(provider);
    if (cancel) return null;
    return SocialCredential(provider: provider, idToken: 'token');
  }
}

class FakeAuth implements AuthRepository {
  final registered = <RegistrationData>[];
  final resets = <String>[];

  @override
  Future<VerificationTarget> register(RegistrationData data) async {
    registered.add(data);
    return data.email != null
        ? VerificationTarget(channel: ContactChannel.email, value: data.email!)
        : VerificationTarget(channel: ContactChannel.phone, value: data.phone!);
  }

  @override
  Future<void> forgotPassword(
    String identifier, {
    OtpDelivery? delivery,
  }) async => resets.add('$identifier:${delivery?.wire}');

  @override
  Future<void> verify(VerificationTarget target, String code) async {}

  @override
  Future<void> resendCode(
    VerificationTarget target, {
    OtpDelivery? delivery,
  }) async {}

  @override
  Future<void> resetPassword({
    required String identifier,
    required String code,
    required String newPassword,
  }) async {}

  @override
  Future<void> login({
    required String identifier,
    required String password,
  }) async {}

  @override
  Future<void> socialLogin(SocialCredential credential) async {}
}

class FakeFreeSunday implements FreeSundayRepository {
  FakeFreeSunday(this.offer, {this.failure});

  final FreeSundayOffer offer;
  final Object? failure;
  var calls = 0;

  @override
  Future<FreeSundayOffer> current() async {
    calls++;
    final error = failure;
    if (error != null) throw error;
    return offer;
  }
}
