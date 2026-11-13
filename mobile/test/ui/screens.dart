import 'package:flutter_riverpod/misc.dart' show Override;
import 'package:flutter/widgets.dart';
import 'package:zella/core/l10n/strings.dart';
import 'package:zella/features/charging/domain/session_repository.dart';
import 'package:zella/features/stations/domain/station_repository.dart';
import 'package:zella/features/wallet/domain/wallet_repository.dart';
import 'package:zella/core/network/api_error.dart';
import 'package:zella/shared/widgets/pay/flow_panel.dart';
import 'package:zella/shared/widgets/pay/payment_step.dart';
import 'package:zella/core/location/location_providers.dart';
import 'package:zella/features/auth/presentation/forgot_password_page.dart';
import 'package:zella/features/auth/presentation/login_page.dart';
import 'package:zella/features/auth/presentation/reset_success_page.dart';
import 'package:zella/features/auth/presentation/splash_page.dart';
import 'package:zella/features/auth/presentation/onboarding_page.dart';
import 'package:zella/features/auth/presentation/register_page.dart';
import 'package:zella/features/charging/domain/session_state.dart';
import 'package:zella/features/charging/presentation/charging_page.dart';
import 'package:zella/features/charging/presentation/session_providers.dart';
import 'package:zella/features/charging/presentation/setup/charge_setup_page.dart';
import 'package:zella/features/history/presentation/history_page.dart';
import 'package:zella/features/home/presentation/home_page.dart';
import 'package:zella/core/links/link_providers.dart';
import 'package:zella/core/media/media_providers.dart';
import 'package:zella/core/storage/preference_providers.dart';
import 'package:zella/features/coupons/domain/coupon.dart';
import 'package:zella/features/coupons/presentation/coupon_providers.dart';
import 'package:zella/features/coupons/presentation/coupons_page.dart';
import 'package:zella/features/profile/presentation/change_password_page.dart';
import 'package:zella/features/profile/presentation/delete_account_page.dart';
import 'package:zella/features/profile/domain/contact_confirmation.dart';
import 'package:zella/features/profile/domain/contact_kind.dart';
import 'package:zella/features/profile/presentation/add_contact_page.dart';
import 'package:zella/features/profile/presentation/confirm_contact_page.dart';
import 'package:zella/features/profile/presentation/edit_profile_page.dart';
import 'package:zella/features/profile/presentation/settings_page.dart';
import 'package:zella/features/support/domain/support_info.dart';
import 'package:zella/features/support/presentation/contact_page.dart';
import 'package:zella/features/support/presentation/support_providers.dart';
import 'package:zella/features/profile/presentation/profile_page.dart';
import 'package:zella/features/profile/presentation/profile_providers.dart';
import 'package:zella/features/stations/presentation/locations_page.dart';
import 'package:zella/features/stations/presentation/station_detail_page.dart';
import 'package:zella/features/stations/presentation/station_providers.dart';
import 'package:zella/features/history/domain/stat_metric.dart';
import 'package:zella/features/history/presentation/activity_stats_page.dart';
import 'package:zella/features/wallet/presentation/topup_page.dart';
import 'package:zella/features/wallet/presentation/wallet_entry_page.dart';
import 'package:zella/features/wallet/presentation/wallet_page.dart';
import 'package:zella/features/wallet/presentation/withdraw_page.dart';
import 'package:zella/features/wallet/presentation/wallet_providers.dart';
import 'package:zella/features/notifications/presentation/notification_providers.dart';
import 'package:zella/features/notifications/presentation/notifications_page.dart';
import 'package:zella/features/stations/presentation/detail/map_providers.dart';
import 'fake_notifications.dart';
import 'ui_fakes.dart';

final screens = <String, Widget Function()>{
  'splash': () => const SplashPage(),
  'forgot': () => const ForgotPasswordPage(),
  'reset-success': () => const ResetSuccessPage(),
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
  'wallet': () => const WalletPage(),
  'wallet-entry': () => const WalletEntryPage(entryId: '1'),
  'stats-charges': () => const ActivityStatsPage(metric: StatMetric.charges),
  'stats-energy': () => const ActivityStatsPage(metric: StatMetric.energy),
  'stats-spent': () => const ActivityStatsPage(metric: StatMetric.spent),
  'withdraw': () => const WithdrawPage(),
  'notifications': () => const NotificationsPage(),
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
  'add-email': () => const AddContactPage(kind: ContactKind.email),
  'add-phone': () => const AddContactPage(kind: ContactKind.phone),
  'confirm-email': () => const ConfirmContactPage(
    kind: ContactKind.email,
    confirmation: ContactConfirmation(target: 'aline@example.com'),
  ),
  'pay-progress': () => FlowPanel(
    kind: FlowKind.progress,
    title: PayFlowText.approveTitle,
    message: PayFlowText.approveBody('Airtel Money', '+24177123456'),
    steps: [
      PaymentStep(PayFlowText.stepRequest, StepStatus.done),
      PaymentStep(PayFlowText.stepApprove, StepStatus.active),
      PaymentStep(PayFlowText.stepConfirm, StepStatus.pending),
      PaymentStep(PayFlowText.stepStart, StepStatus.pending),
    ],
  ),
  'pay-success': () => FlowPanel(
    kind: FlowKind.success,
    title: PayFlowText.topupDoneTitle,
    message: PayFlowText.topupDoneBody('5 000 FCFA'),
    primaryLabel: PayFlowText.done,
    onPrimary: () {},
  ),
  'pay-failed': () => FlowPanel(
    kind: FlowKind.failed,
    title: PayFlowText.failedTitle,
    message: ApiError.fromCode('PAYMENT_NOT_COMPLETED').message,
    primaryLabel: PayFlowText.retry,
    onPrimary: () {},
    secondaryLabel: PayFlowText.done,
    onSecondary: () {},
  ),
  'pay-verifying': () => FlowPanel(
    kind: FlowKind.verifying,
    title: PayFlowText.verifyingTitle,
    message: ApiError.fromCode('PAYMENT_VERIFYING').message,
    primaryLabel: PayFlowText.done,
    onPrimary: () {},
  ),
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
  email: 'support@zella.example',
  phone: '+24177000000',
  whatsapp: '+24177000001',
  agreementUrl: 'https://zella.example/terms',
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
]) => overridesWith(
  state: state,
  profile: profile,
  picker: picker,
  links: links,
  preferences: preferences,
  support: support,
  couponList: couponList,
);

List<Override> overridesWith({
  SessionState state = SessionState.active,
  FakeProfile? profile,
  FakePhotoPicker? picker,
  FakeLinks? links,
  MemoryPreferences? preferences,
  FakeSupport? support,
  FakeCoupons? couponList,
  WalletRepository? wallet,
  StationRepository? stationRepo,
  SessionRepository? sessionRepo,
  List<Override> extra = const [],
}) => [
  stationRepositoryProvider.overrideWithValue(stationRepo ?? FakeStations()),
  sessionRepositoryProvider.overrideWithValue(
    sessionRepo ?? FakeSessions(state),
  ),
  walletRepositoryProvider.overrideWithValue(wallet ?? FakeWallet()),
  notificationRepositoryProvider.overrideWithValue(FakeNotifications()),
  mapTileProvider.overrideWithValue(NoTiles()),
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
  ...extra,
];
