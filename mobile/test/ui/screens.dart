import 'package:flutter_riverpod/misc.dart' show Override;
import 'package:flutter/widgets.dart';
import 'package:ndzela/core/location/location_providers.dart';
import 'package:ndzela/features/auth/presentation/login_page.dart';
import 'package:ndzela/features/auth/presentation/onboarding_page.dart';
import 'package:ndzela/features/auth/presentation/register_page.dart';
import 'package:ndzela/features/auth/presentation/welcome_page.dart';
import 'package:ndzela/features/charging/domain/session_state.dart';
import 'package:ndzela/features/charging/presentation/charging_page.dart';
import 'package:ndzela/features/charging/presentation/session_providers.dart';
import 'package:ndzela/features/charging/presentation/setup/charge_setup_page.dart';
import 'package:ndzela/features/history/presentation/history_page.dart';
import 'package:ndzela/features/home/presentation/home_page.dart';
import 'package:ndzela/core/links/link_providers.dart';
import 'package:ndzela/core/media/media_providers.dart';
import 'package:ndzela/core/storage/preference_providers.dart';
import 'package:ndzela/features/coupons/domain/coupon.dart';
import 'package:ndzela/features/coupons/presentation/coupon_providers.dart';
import 'package:ndzela/features/coupons/presentation/coupons_page.dart';
import 'package:ndzela/features/profile/presentation/change_password_page.dart';
import 'package:ndzela/features/profile/presentation/delete_account_page.dart';
import 'package:ndzela/features/profile/presentation/edit_profile_page.dart';
import 'package:ndzela/features/profile/presentation/settings_page.dart';
import 'package:ndzela/features/support/domain/support_info.dart';
import 'package:ndzela/features/support/presentation/contact_page.dart';
import 'package:ndzela/features/support/presentation/support_providers.dart';
import 'package:ndzela/features/profile/presentation/profile_page.dart';
import 'package:ndzela/features/profile/presentation/profile_providers.dart';
import 'package:ndzela/features/stations/presentation/locations_page.dart';
import 'package:ndzela/features/stations/presentation/station_detail_page.dart';
import 'package:ndzela/features/stations/presentation/station_providers.dart';
import 'package:ndzela/features/wallet/presentation/topup_page.dart';
import 'package:ndzela/features/wallet/presentation/wallet_providers.dart';
import 'ui_fakes.dart';

final screens = <String, Widget Function()>{
  'welcome': () => const WelcomePage(),
  'onboarding': () => const OnboardingPage(),
  'login': () => const LoginPage(),
  'register': () => const RegisterPage(),
  'home': () => const HomePage(),
  'locations': () => const LocationsPage(),
  'history': () => const HistoryPage(),
  'profile': () => const ProfilePage(),
  'settings': () => const SettingsPage(),
  'contact': () => const ContactPage(),
  'coupons': () => const CouponsPage(),
  'change-password': () => const ChangePasswordPage(),
  'delete-account': () => const DeleteAccountPage(),
  'topup': () => const TopupPage(),
  'station': () => const StationDetailPage(
    stationId: 'DY202609110007-VERY-LONG-STATION-IDENTIFIER',
  ),
  'setup': () => const ChargeSetupPage(
    stationId: 'DY202609110007-VERY-LONG-STATION-IDENTIFIER',
    initialConnectorId: 1,
  ),
  'charging-active': () => const ChargingPage(sessionId: 'a'),
  'details': () => const ChargingPage(sessionId: 'a'),
  'edit-profile': () => const EditProfilePage(),
};

final coupons = [
  Coupon(
    id: 'c1',
    title: 'Première recharge offerte',
    description: 'Valable sur une recharge de plus de 1000 FCFA',
    expiresAt: DateTime(2027, 1, 31),
  ),
  const Coupon(id: 'c2', title: 'Coupon sans date'),
];

const supportInfo = SupportInfo(
  email: 'support@ndzela.example',
  phone: '+24177000000',
  whatsapp: '+24177000001',
  agreementUrl: 'https://ndzela.example/terms',
);

final screenStates = <String, SessionState>{'details': SessionState.completed};

List<Override> overrides([
  SessionState state = SessionState.active,
  FakeProfile? profile,
  FakePhotoPicker? picker,
  FakeLinks? links,
  MemoryPreferences? preferences,
  FakeSupport? support,
  FakeCoupons? couponList,
]) => [
  stationRepositoryProvider.overrideWithValue(FakeStations()),
  sessionRepositoryProvider.overrideWithValue(FakeSessions(state)),
  walletRepositoryProvider.overrideWithValue(FakeWallet()),
  profileRepositoryProvider.overrideWithValue(profile ?? FakeProfile()),
  locationServiceProvider.overrideWithValue(NoLocation()),
  couponRepositoryProvider.overrideWithValue(
    couponList ?? FakeCoupons(coupons),
  ),
  supportRepositoryProvider.overrideWithValue(
    support ?? FakeSupport(supportInfo),
  ),
  preferenceStoreProvider.overrideWithValue(preferences ?? MemoryPreferences()),
  photoPickerProvider.overrideWithValue(picker ?? FakePhotoPicker()),
  linkLauncherProvider.overrideWithValue(links ?? FakeLinks()),
];
