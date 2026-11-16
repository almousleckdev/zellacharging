import 'app_language.dart';

abstract final class PasswordSetupText {
  static String get title => AppLanguage.pick(
    fr: 'Créez votre mot de passe',
    en: 'Create your password',
  );

  static String get body => AppLanguage.pick(
    fr: 'Votre compte a été créé pour vous. Nous vous envoyons un code pour choisir votre mot de passe.',
    en: 'Your account was created for you. We’ll send you a code so you can choose your password.',
  );
}
