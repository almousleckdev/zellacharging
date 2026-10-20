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
  'settings': () => const ProfilePage(),
  'topup': () => const TopupPage(),
  'station': () => const StationDetailPage(
    stationId: 'DY202609110007-VERY-LONG-STATION-IDENTIFIER',
  ),
  'setup': () => const ChargeSetupPage(
    stationId: 'DY202609110007-VERY-LONG-STATION-IDENTIFIER',
    initialConnectorId: 1,
  ),
  'charging-active': () => const ChargingPage(sessionId: 'a'),
};

List<Override> overrides([SessionState state = SessionState.active]) => [
  stationRepositoryProvider.overrideWithValue(FakeStations()),
  sessionRepositoryProvider.overrideWithValue(FakeSessions(state)),
  walletRepositoryProvider.overrideWithValue(FakeWallet()),
  profileRepositoryProvider.overrideWithValue(FakeProfile()),
  locationServiceProvider.overrideWithValue(NoLocation()),
];
