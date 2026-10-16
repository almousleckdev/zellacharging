import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/domain/verification_target.dart';
import '../../features/auth/presentation/login_page.dart';
import '../../features/auth/presentation/onboarding_page.dart';
import '../../features/auth/presentation/register_page.dart';
import '../../features/auth/presentation/welcome_page.dart';
import '../../features/auth/presentation/verify_page.dart';
import '../../features/history/presentation/history_page.dart';
import '../../features/home/presentation/home_page.dart';
import '../../features/stations/presentation/locations_page.dart';
import '../../features/profile/presentation/profile_page.dart';
import '../../features/shell/presentation/shell_page.dart';
import '../../features/scan/presentation/scan_page.dart';
import '../../features/wallet/presentation/topup_page.dart';
import '../../features/stations/presentation/station_detail_page.dart';
import '../../features/charging/presentation/charging_page.dart';
import '../../features/charging/presentation/setup/charge_setup_page.dart';
import '../session/session_controller.dart';
import 'routes.dart';

const _publicRoutes = {
  Routes.welcome,
  Routes.onboarding,
  Routes.login,
  Routes.register,
  Routes.verify,
};

final routerProvider = Provider<GoRouter>((ref) {
  final refresh = ValueNotifier<int>(0);
  ref.listen(sessionControllerProvider, (_, _) => refresh.value++);
  ref.onDispose(refresh.dispose);

  return GoRouter(
    initialLocation: Routes.welcome,
    refreshListenable: refresh,
    redirect: (context, state) {
      final status = ref.read(sessionControllerProvider);
      if (status == SessionStatus.unknown) return null;
      final isPublic = _publicRoutes.contains(state.matchedLocation);
      final signedIn = status == SessionStatus.authenticated;
      if (signedIn && isPublic) return Routes.home;
      if (!signedIn && !isPublic) return Routes.welcome;
      return null;
    },
    routes: [
      GoRoute(path: Routes.welcome, builder: (_, _) => const WelcomePage()),
      GoRoute(
        path: Routes.onboarding,
        builder: (_, _) => const OnboardingPage(),
      ),
      GoRoute(path: Routes.login, builder: (_, _) => const LoginPage()),
      GoRoute(path: Routes.register, builder: (_, _) => const RegisterPage()),
      GoRoute(
        path: Routes.verify,
        builder: (_, state) =>
            VerifyPage(target: state.extra as VerificationTarget),
      ),
      GoRoute(
        path: Routes.charging,
        builder: (_, state) =>
            ChargingPage(sessionId: state.pathParameters['id']!),
      ),
      GoRoute(path: Routes.scan, builder: (_, _) => const ScanPage()),
      GoRoute(path: Routes.topup, builder: (_, _) => const TopupPage()),
      GoRoute(
        path: Routes.chargeSetup,
        builder: (_, state) => ChargeSetupPage(
          stationId: state.pathParameters['id']!,
          initialConnectorId: int.tryParse(
            state.uri.queryParameters['connector'] ?? '',
          ),
        ),
      ),
      GoRoute(
        path: Routes.station,
        builder: (_, state) => StationDetailPage(
          stationId: state.pathParameters['id']!,
          highlightedConnectorId: int.tryParse(
            state.uri.queryParameters['connector'] ?? '',
          ),
        ),
      ),
      StatefulShellRoute.indexedStack(
        builder: (_, _, shell) => ShellPage(navigationShell: shell),
        branches: [
          StatefulShellBranch(
            routes: [
              GoRoute(path: Routes.home, builder: (_, _) => const HomePage()),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: Routes.locations,
                builder: (_, _) => const LocationsPage(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: Routes.history,
                builder: (_, _) => const HistoryPage(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: Routes.profile,
                builder: (_, _) => const ProfilePage(),
              ),
            ],
          ),
        ],
      ),
    ],
  );
});
