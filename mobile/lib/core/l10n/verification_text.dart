import 'app_language.dart';

abstract final class VerificationText {
  static String get resumed => AppLanguage.pick(
    fr: 'Votre compte n’est pas encore vérifié. Nous venons de vous envoyer un code par e-mail.',
    en: 'Your account is not verified yet. We just sent you a code by email.',
  );
}
