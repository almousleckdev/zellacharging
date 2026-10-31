import 'app_language.dart';

abstract final class AuthText {
  static String get signIn =>
      AppLanguage.pick(fr: 'Se connecter', en: 'Sign In');

  static String get createAccount =>
      AppLanguage.pick(fr: 'Créer le compte', en: 'Create Account');

  static String get signInTitle =>
      AppLanguage.pick(fr: 'Bon retour', en: 'Welcome back');

  static String get signInSubtitle => AppLanguage.pick(
    fr: 'Connectez-vous à votre compte',
    en: 'Sign in to your account',
  );

  static String get registerSubtitle => AppLanguage.pick(
    fr: 'Créez votre compte pour commencer à recharger.',
    en: 'Create your account to start charging.',
  );

  static String get identifierLabel =>
      AppLanguage.pick(fr: 'E-mail', en: 'Email');

  static String get emailLabel => AppLanguage.pick(fr: 'E-mail', en: 'Email');

  static String get phoneLabel => AppLanguage.pick(
    fr: 'Numéro de téléphone (facultatif)',
    en: 'Phone number (optional)',
  );

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

  static String get signUp => AppLanguage.pick(fr: 'S’inscrire', en: 'Sign Up');

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

  static String get tagline => AppLanguage.pick(
    fr: 'Énergie propre. Un avenir meilleur.',
    en: 'Clean energy. A better tomorrow.',
  );

  static String get skipLabel => AppLanguage.pick(fr: 'Passer', en: 'Skip');

  static String get slide1Title => AppLanguage.pick(
    fr: 'Rechargez votre véhicule électrique partout, à tout moment',
    en: 'Charge your EV anywhere, anytime',
  );

  static String get slide1Body => AppLanguage.pick(
    fr: 'Trouvez des bornes, payez en toute sécurité et gardez le cap.',
    en: 'Find charging stations, pay securely, and keep moving.',
  );

  static String get slide2Title => AppLanguage.pick(
    fr: 'Paiements simples et sécurisés',
    en: 'Easy & secure payments',
  );

  static String get slide2Body => AppLanguage.pick(
    fr: 'Payez avec votre moyen préféré et obtenez une confirmation instantanée.',
    en: 'Pay with your preferred method and get instant confirmation.',
  );

  static String get slide3Title => AppLanguage.pick(
    fr: 'Un avenir plus propre et plus vert',
    en: 'A cleaner, greener future',
  );

  static String get slide3Body => AppLanguage.pick(
    fr: 'Ensemble, nous accélérons la transition vers une énergie propre.',
    en: 'Together we drive the transition to clean energy.',
  );

  static String get rememberMe =>
      AppLanguage.pick(fr: 'Se souvenir de moi', en: 'Remember me');

  static String get forgotPassword =>
      AppLanguage.pick(fr: 'Mot de passe oublié ?', en: 'Forgot password?');

  static String get registerTitle =>
      AppLanguage.pick(fr: 'Créez votre compte', en: 'Create your account');

  static String get registerSubtitleNew => AppLanguage.pick(
    fr: 'Rejoignez Zella Charging et commencez votre aventure.',
    en: 'Join Zella Charging and start your journey.',
  );

  static String get confirmPassword =>
      AppLanguage.pick(fr: 'Confirmer le mot de passe', en: 'Confirm password');

  static String get agreePrefix =>
      AppLanguage.pick(fr: 'J’accepte les ', en: 'I agree to the ');

  static String get terms =>
      AppLanguage.pick(fr: 'Conditions d’utilisation', en: 'Terms of Service');

  static String get agreeAnd => AppLanguage.pick(fr: ' et la ', en: ' and ');

  static String get privacy => AppLanguage.pick(
    fr: 'Politique de confidentialité',
    en: 'Privacy Policy',
  );

  static String get mustAgree => AppLanguage.pick(
    fr: 'Acceptez les conditions pour continuer.',
    en: 'Accept the terms to continue.',
  );

  static String get passwordsDiffer => AppLanguage.pick(
    fr: 'Les mots de passe ne correspondent pas.',
    en: 'Passwords do not match.',
  );

  static String get phoneNeedsEmail => AppLanguage.pick(
    fr: 'Saisissez votre e-mail. Vous pourrez ajouter votre téléphone dans votre profil.',
    en: 'Enter your email. You can add your phone number in your profile.',
  );

  static String get forgotTitle => AppLanguage.pick(
    fr: 'Mot de passe oublié ?',
    en: 'Forgot your password?',
  );

  static String get forgotBody => AppLanguage.pick(
    fr: 'Saisissez votre e-mail et nous vous enverrons un code pour réinitialiser votre mot de passe.',
    en: 'Enter your email and we’ll send you a code to reset your password.',
  );

  static String get sendCode =>
      AppLanguage.pick(fr: 'Envoyer le code', en: 'Send OTP');

  static String get backToLogin =>
      AppLanguage.pick(fr: 'Retour à la connexion', en: 'Back to login');

  static String get phoneResetSoon => AppLanguage.pick(
    fr: 'La réinitialisation par SMS arrive bientôt. Utilisez votre e-mail.',
    en: 'Reset by SMS is coming soon. Please use your email.',
  );

  static String get otpTitle =>
      AppLanguage.pick(fr: 'Vérifiez votre code', en: 'Verify your code');

  static String get otpSentTo => AppLanguage.pick(
    fr: 'Nous avons envoyé un code à 6 chiffres à',
    en: 'We’ve sent a 6-digit code to',
  );

  static String resendIn(String time) => AppLanguage.pick(
    fr: 'Renvoyer le code dans $time',
    en: 'Resend code in $time',
  );

  static String get changeEmail =>
      AppLanguage.pick(fr: 'Changer d’e-mail', en: 'Change email');

  static String get newPasswordTitle => AppLanguage.pick(
    fr: 'Créez un nouveau mot de passe',
    en: 'Create new password',
  );

  static String get newPasswordBody => AppLanguage.pick(
    fr: 'Votre nouveau mot de passe doit contenir au moins 8 caractères dont un chiffre.',
    en: 'Your new password must be at least 8 characters and include a number.',
  );

  static String get newPassword =>
      AppLanguage.pick(fr: 'Nouveau mot de passe', en: 'New password');

  static String get resetPassword => AppLanguage.pick(
    fr: 'Réinitialiser le mot de passe',
    en: 'Reset Password',
  );

  static String get resetDoneTitle => AppLanguage.pick(
    fr: 'Mot de passe réinitialisé !',
    en: 'Password reset successfully!',
  );

  static String get resetDoneBody => AppLanguage.pick(
    fr: 'Votre mot de passe a été mis à jour. Vous pouvez maintenant vous connecter.',
    en: 'Your password has been updated. You can now log in to your account.',
  );

  static String get goToLogin =>
      AppLanguage.pick(fr: 'Aller à la connexion', en: 'Go to Login');

  static String get welcomeBackTitle =>
      AppLanguage.pick(fr: 'Bon retour !', en: 'Welcome back!');

  static String get welcomeBackBody => AppLanguage.pick(
    fr: 'Tout est prêt. Redirection vers votre tableau de bord…',
    en: 'You’re all set. Redirecting to your dashboard…',
  );

  static String get next => AppLanguage.pick(fr: 'Suivant', en: 'Next');

  static String get weakPassword => AppLanguage.pick(
    fr: 'Au moins 8 caractères dont un chiffre',
    en: 'At least 8 characters including a number',
  );

  static String get accountVerified => AppLanguage.pick(
    fr: 'Compte vérifié. Connectez-vous.',
    en: 'Account verified. Please sign in.',
  );
}
