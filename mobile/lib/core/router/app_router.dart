import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/presentation/login_page.dart';
import '../../features/auth/presentation/onboarding_page.dart';
import '../../features/auth/presentation/forgot_password_page.dart';
import '../../features/auth/presentation/otp_args.dart';
import '../../features/auth/presentation/otp_page.dart';
import '../../features/auth/presentation/register_page.dart';
import '../../features/auth/presentation/reset_args.dart';
import '../../features/auth/presentation/reset_password_page.dart';
import '../../features/auth/presentation/reset_success_page.dart';
import '../../features/auth/presentation/splash_page.dart';
import '../../features/auth/presentation/splash_state.dart';
import '../../features/auth/presentation/welcome_back_page.dart';
import '../../features/history/presentation/history_page.dart';
import '../../features/home/presentation/home_page.dart';
import '../../features/stations/presentation/locations_page.dart';
import '../../features/coupons/presentation/coupons_page.dart';
import '../../features/profile/presentation/change_password_page.dart';
import '../../features/profile/presentation/delete_account_page.dart';
import '../../features/profile/domain/contact_confirmation.dart';
import '../../features/profile/domain/contact_kind.dart';
import '../../features/profile/presentation/add_contact_page.dart';
import '../../features/profile/presentation/confirm_contact_page.dart';
import '../../features/profile/presentation/edit_profile_page.dart';
import '../../features/profile/presentation/settings_page.dart';
import '../../features/support/presentation/contact_page.dart';
import '../../features/profile/presentation/profile_page.dart';
import '../../features/shell/presentation/shell_page.dart';
import '../../features/scan/presentation/scan_page.dart';
import '../../features/history/domain/stat_metric.dart';
import '../../features/history/presentation/activity_stats_page.dart';
import '../../features/wallet/presentation/topup_page.dart';
import '../../features/wallet/presentation/wallet_entry_page.dart';
import '../../features/wallet/presentation/wallet_page.dart';
import '../../features/notifications/presentation/notifications_page.dart';
import '../../features/wallet/presentation/withdraw_page.dart';
import '../../features/stations/presentation/station_detail_page.dart';
import '../../features/charging/presentation/charging_page.dart';
import '../../features/charging/presentation/setup/charge_setup_page.dart';
import '../session/session_controller.dart';
import 'routes.dart';

const _publicRoutes = {
  Routes.splash,
  Routes.onboarding,
  Routes.login,
  Routes.register,
  Routes.otp,
  Routes.forgotPassword,
  Routes.resetPassword,
  Routes.resetSuccess,
  Routes.welcomeBack,
};

final routerProvider = Provider<GoRouter>((ref) {
  final refresh = ValueNotifier<int>(0);
  ref.listen(sessionControllerProvider, (_, _) => refresh.value++);
  ref.listen(splashDoneProvider, (_, _) => refresh.value++);
  ref.onDispose(refresh.dispose);

  return GoRouter(
    initialLocation: Routes.splash,
    refreshListenable: refresh,
    redirect: (context, state) {
      final status = ref.read(sessionControllerProvider);
      final location = state.matchedLocation;
      final signedIn = status == SessionStatus.authenticated;
      if (!ref.read(splashDoneProvider)) {
        return location == Routes.splash ? null : Routes.splash;
      }
      if (location == Routes.splash) {
        if (signedIn) return Routes.home;
        final seen = ref.read(onboardingSeenProvider).value ?? false;
        return seen ? Routes.login : Routes.onboarding;
      }
      final isPublic = _publicRoutes.contains(location);
      if (signedIn && isPublic) return Routes.home;
      if (!signedIn && !isPublic) return Routes.login;
      return null;
    },
    routes: [
      GoRoute(path: Routes.splash, builder: (_, _) => const SplashPage()),
      GoRoute(
        path: Routes.onboarding,
        builder: (_, _) => const OnboardingPage(),
      ),
      GoRoute(path: Routes.login, builder: (_, _) => const LoginPage()),
      GoRoute(path: Routes.register, builder: (_, _) => const RegisterPage()),
      GoRoute(
        path: Routes.otp,
        builder: (_, state) => OtpPage(args: state.extra as OtpArgs),
      ),
      GoRoute(
        path: Routes.forgotPassword,
        builder: (_, _) => const ForgotPasswordPage(),
      ),
      GoRoute(
        path: Routes.resetPassword,
        builder: (_, state) =>
            ResetPasswordPage(args: state.extra as ResetArgs),
      ),
      GoRoute(
        path: Routes.resetSuccess,
        builder: (_, _) => const ResetSuccessPage(),
      ),
      GoRoute(
        path: Routes.welcomeBack,
        builder: (_, _) => const WelcomeBackPage(),
      ),
      GoRoute(
        path: Routes.charging,
        builder: (_, state) =>
            ChargingPage(sessionId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: Routes.editProfile,
        builder: (_, _) => const EditProfilePage(),
      ),
      GoRoute(
        path: Routes.addEmail,
        builder: (_, state) => AddContactPage(
          kind: ContactKind.email,
          changing: state.extra == true,
        ),
      ),
      GoRoute(
        path: Routes.addPhone,
        builder: (_, state) => AddContactPage(
          kind: ContactKind.phone,
          changing: state.extra == true,
        ),
      ),
      GoRoute(
        path: Routes.confirmEmail,
        builder: (_, state) => ConfirmContactPage(
          kind: ContactKind.email,
          confirmation: state.extra as ContactConfirmation,
        ),
      ),
      GoRoute(
        path: Routes.confirmPhone,
        builder: (_, state) => ConfirmContactPage(
          kind: ContactKind.phone,
          confirmation: state.extra as ContactConfirmation,
        ),
      ),
      GoRoute(path: Routes.settings, builder: (_, _) => const SettingsPage()),
      GoRoute(
        path: Routes.changePassword,
        builder: (_, _) => const ChangePasswordPage(),
      ),
      GoRoute(
        path: Routes.deleteAccount,
        builder: (_, _) => const DeleteAccountPage(),
      ),
      GoRoute(path: Routes.contact, builder: (_, _) => const ContactPage()),
      GoRoute(path: Routes.coupons, builder: (_, _) => const CouponsPage()),
      GoRoute(path: Routes.scan, builder: (_, _) => const ScanPage()),
      GoRoute(
        path: Routes.activityStats,
        builder: (_, state) => ActivityStatsPage(
          metric: StatMetric.values.firstWhere(
            (metric) => metric.name == state.pathParameters['metric'],
            orElse: () => StatMetric.charges,
          ),
        ),
      ),
      GoRoute(
        path: Routes.walletEntry,
        builder: (_, state) =>
            WalletEntryPage(entryId: state.pathParameters['id']!),
      ),
      GoRoute(path: Routes.wallet, builder: (_, _) => const WalletPage()),
      GoRoute(path: Routes.topup, builder: (_, _) => const TopupPage()),
      GoRoute(path: Routes.withdraw, builder: (_, _) => const WithdrawPage()),
      GoRoute(
        path: Routes.notifications,
        builder: (_, _) => const NotificationsPage(),
      ),
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
