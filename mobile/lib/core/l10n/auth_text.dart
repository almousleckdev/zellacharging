import 'app_language.dart';

abstract final class AuthText {
  static String get signIn => AppLanguage.pick(fr: 'Connexion', en: 'Login');

  static String get createAccount =>
      AppLanguage.pick(fr: 'Créer un compte', en: 'Create account');

  static String get signInTitle =>
      AppLanguage.pick(fr: 'Bon retour', en: 'Welcome back');

  static String get signInSubtitle => AppLanguage.pick(
    fr: 'Connectez-vous pour continuer à recharger.',
    en: 'Sign in to keep charging.',
  );

  static String get registerSubtitle => AppLanguage.pick(
    fr: 'Créez votre compte pour commencer à recharger.',
    en: 'Create your account to start charging.',
  );

  static String get identifierLabel => AppLanguage.pick(
    fr: 'E-mail ou numéro de téléphone',
    en: 'Email or phone number',
  );

  static String get emailLabel => AppLanguage.pick(fr: 'E-mail', en: 'Email');

  static String get phoneLabel =>
      AppLanguage.pick(fr: 'Numéro de téléphone', en: 'Phone number');

  static String get passwordLabel =>
      AppLanguage.pick(fr: 'Mot de passe', en: 'Password');

  static String get firstNameLabel =>
      AppLanguage.pick(fr: 'Prénom', en: 'First name');

  static String get lastNameLabel =>
      AppLanguage.pick(fr: 'Nom', en: 'Last name');

  static String get contactHint => AppLanguage.pick(
    fr: 'E-mail ou numéro de téléphone (au moins un)',
    en: 'Email or phone number (at least one)',
  );

  static String get noAccount => AppLanguage.pick(
    fr: 'Vous n\'avez pas de compte ?',
    en: 'Don\'t have an account?',
  );

  static String get signUp =>
      AppLanguage.pick(fr: 'Inscription', en: 'Sign up');

  static String get haveAccount => AppLanguage.pick(
    fr: 'Vous avez déjà un compte ?',
    en: 'Already have an account?',
  );

  static String get verifyTitle =>
      AppLanguage.pick(fr: 'Vérifiez votre compte', en: 'Verify your account');

  static String get codeLabel =>
      AppLanguage.pick(fr: 'Code à 6 chiffres', en: '6-digit code');

  static String get verify => AppLanguage.pick(fr: 'Vérifier', en: 'Verify');

  static String get resendCode =>
      AppLanguage.pick(fr: 'Renvoyer le code', en: 'Resend code');

  static String get codeResent => AppLanguage.pick(
    fr: 'Un nouveau code a été envoyé.',
    en: 'A new code has been sent.',
  );

  static String get skip => AppLanguage.pick(fr: 'PASSER', en: 'SKIP');

  static String get orContinueWith =>
      AppLanguage.pick(fr: 'Ou continuer avec', en: 'Or continue with');

  static String get google => AppLanguage.pick(fr: 'Google', en: 'Google');

  static String get apple => AppLanguage.pick(fr: 'Apple', en: 'Apple');

  static String get comingSoon => AppLanguage.pick(
    fr: 'Cette option de connexion arrive bientôt.',
    en: 'This sign-in option is coming soon.',
  );

  static String get welcomeTitle => AppLanguage.pick(
    fr: 'Alimentez chaque trajet',
    en: 'Power every journey',
  );

  static String get welcomeBody => AppLanguage.pick(
    fr: 'Découvrez les stations de recharge proches, scannez pour démarrer et suivez votre recharge en temps réel.',
    en: 'Discover nearby charging stations, scan to start, and follow your charge in real time.',
  );

  static String get slideFindTitle =>
      AppLanguage.pick(fr: 'Trouvez chaque station', en: 'Find every station');

  static String get slideFindBody => AppLanguage.pick(
    fr: 'Parcourez les bornes proches et voyez quels connecteurs sont libres en ce moment.',
    en: 'Browse nearby chargers and see which connectors are free right now.',
  );

  static String get slideScanTitle =>
      AppLanguage.pick(fr: 'Scannez et rechargez', en: 'Scan and charge');

  static String get slideScanBody => AppLanguage.pick(
    fr: 'Scannez le QR code de la borne, choisissez un connecteur et démarrez en quelques secondes.',
    en: 'Scan the QR code on the charger, choose a connector and start charging in seconds.',
  );

  static String get slideTrackTitle =>
      AppLanguage.pick(fr: 'Suivez chaque recharge', en: 'Track every charge');

  static String get slideTrackBody => AppLanguage.pick(
    fr: 'Suivez la progression en temps réel et gardez la trace de votre historique et de votre consommation.',
    en: 'Follow your charging progress in real time and keep track of your history and energy usage.',
  );
}
