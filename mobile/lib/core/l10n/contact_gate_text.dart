import 'app_language.dart';

abstract final class ContactGateText {
  static String get title => AppLanguage.pick(
    fr: 'Finalisez votre compte',
    en: 'Finish setting up your account',
  );

  static String get body => AppLanguage.pick(
    fr: 'Pour recharger en toute sécurité, vérifiez votre numéro de téléphone et votre e-mail.',
    en: 'To charge safely, verify your phone number and your email.',
  );

  static String get phone => AppLanguage.pick(fr: 'Téléphone', en: 'Phone');

  static String get email => AppLanguage.pick(fr: 'E-mail', en: 'Email');

  static String get verified => AppLanguage.pick(fr: 'Vérifié', en: 'Verified');

  static String get toVerify =>
      AppLanguage.pick(fr: 'À vérifier', en: 'To verify');

  static String get missing => AppLanguage.pick(fr: 'À ajouter', en: 'To add');

  static String get addPhone =>
      AppLanguage.pick(fr: 'Ajouter mon téléphone', en: 'Add my phone');

  static String get verifyPhone =>
      AppLanguage.pick(fr: 'Vérifier mon téléphone', en: 'Verify my phone');

  static String get addEmail =>
      AppLanguage.pick(fr: 'Ajouter mon e-mail', en: 'Add my email');

  static String get verifyEmail =>
      AppLanguage.pick(fr: 'Vérifier mon e-mail', en: 'Verify my email');

  static String get signOut =>
      AppLanguage.pick(fr: 'Se déconnecter', en: 'Sign out');

  static String step(int current, int total) => AppLanguage.pick(
    fr: 'Étape $current sur $total',
    en: 'Step $current of $total',
  );

  static String get verifyPhoneTitle =>
      AppLanguage.pick(fr: 'Vérifiez votre téléphone', en: 'Verify your phone');

  static String get verifyEmailTitle =>
      AppLanguage.pick(fr: 'Vérifiez votre e-mail', en: 'Verify your email');

  static String get notVerifiedError => AppLanguage.pick(
    fr: 'Vérifiez votre téléphone et votre e-mail avant de recharger.',
    en: 'Verify your phone and email before charging.',
  );
}
