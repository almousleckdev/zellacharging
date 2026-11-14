abstract final class Routes {
  static const splash = '/splash';
  static const onboarding = '/onboarding';
  static const login = '/login';
  static const register = '/register';
  static const otp = '/otp';
  static const forgotPassword = '/forgot-password';
  static const resetPassword = '/reset-password';
  static const resetSuccess = '/reset-success';
  static const welcomeBack = '/welcome-back';
  static const home = '/home';
  static const locations = '/locations';
  static const history = '/history';
  static const activityStats = '/history/stats/:metric';
  static const profile = '/profile';
  static const editProfile = '/profile/edit';
  static const settings = '/profile/settings';
  static const addEmail = '/profile/edit/email';
  static const confirmEmail = '/profile/edit/email/confirm';
  static const confirmPhone = '/profile/edit/phone/confirm';
  static const addPhone = '/profile/edit/phone';
  static const changePassword = '/profile/settings/password';
  static const deleteAccount = '/profile/settings/delete';
  static const contact = '/profile/contact';
  static const coupons = '/profile/coupons';
  static const notifications = '/notifications';
  static const scan = '/scan';
  static const wallet = '/wallet';
  static const topup = '/wallet/topup';
  static const withdraw = '/wallet/withdraw';
  static const walletEntry = '/wallet/entries/:id';
  static const station = '/stations/:id';
  static const charging = '/charging/:id';
  static const chargeSetup = '/stations/:id/charge';

  static String walletEntryPath(String id) => '/wallet/entries/$id';

  static String chargingPath(String id) => '/charging/$id';

  static String activityStatsPath(String metric) => '/history/stats/$metric';

  static String chargeSetupPath(String id, {int? connectorId}) {
    final path = '/stations/${Uri.encodeComponent(id)}/charge';
    return connectorId == null ? path : '$path?connector=$connectorId';
  }

  static String stationPath(String id, {int? connectorId}) {
    final path = '/stations/${Uri.encodeComponent(id)}';
    return connectorId == null ? path : '$path?connector=$connectorId';
  }
}
