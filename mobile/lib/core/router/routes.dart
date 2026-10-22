abstract final class Routes {
  static const welcome = '/welcome';
  static const onboarding = '/onboarding';
  static const login = '/login';
  static const register = '/register';
  static const verify = '/verify';
  static const home = '/home';
  static const locations = '/locations';
  static const history = '/history';
  static const profile = '/profile';
  static const editProfile = '/profile/edit';
  static const settings = '/profile/settings';
  static const changePassword = '/profile/settings/password';
  static const deleteAccount = '/profile/settings/delete';
  static const contact = '/profile/contact';
  static const coupons = '/profile/coupons';
  static const scan = '/scan';
  static const topup = '/wallet/topup';
  static const station = '/stations/:id';
  static const charging = '/charging/:id';
  static const chargeSetup = '/stations/:id/charge';

  static String chargingPath(String id) => '/charging/$id';

  static String chargeSetupPath(String id, {int? connectorId}) {
    final path = '/stations/${Uri.encodeComponent(id)}/charge';
    return connectorId == null ? path : '$path?connector=$connectorId';
  }

  static String stationPath(String id, {int? connectorId}) {
    final path = '/stations/${Uri.encodeComponent(id)}';
    return connectorId == null ? path : '$path?connector=$connectorId';
  }
}
