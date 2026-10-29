import 'charging_error_text.dart';
import 'app_language.dart';

abstract final class ErrorText {
  static String withReference(String message, String? reference) {
    if (reference == null || reference.isEmpty) return message;
    final short = reference.length > 8 ? reference.substring(0, 8) : reference;
    return '$message (${AppLanguage.pick(fr: 'réf.', en: 'ref.')} $short)';
  }

  static Map<String, String> get byCode => {
    'INVALID_CREDENTIALS': AppLanguage.pick(
      fr: 'E-mail, téléphone ou mot de passe incorrect.',
      en: 'Incorrect email, phone or password.',
    ),
    'ACCOUNT_NOT_VERIFIED': AppLanguage.pick(
      fr: 'Veuillez d’abord vérifier votre compte.',
      en: 'Please verify your account first.',
    ),
    'ACCOUNT_SUSPENDED': AppLanguage.pick(
      fr: 'Votre compte est suspendu. Contactez le support.',
      en: 'Your account is suspended. Contact support.',
    ),
    'EMAIL_ALREADY_SET': AppLanguage.pick(
      fr: 'Une adresse e-mail est déjà enregistrée sur ce compte.',
      en: 'This account already has an email address.',
    ),
    'PHONE_ALREADY_SET': AppLanguage.pick(
      fr: 'Un numéro de téléphone est déjà enregistré sur ce compte.',
      en: 'This account already has a phone number.',
    ),
    'NO_PENDING_EMAIL': AppLanguage.pick(
      fr: 'Aucune adresse e-mail en attente de confirmation. Recommencez.',
      en: 'No email is waiting for confirmation. Please start again.',
    ),
    'ACCOUNT_LOCKED': AppLanguage.pick(
      fr: 'Trop de tentatives. Réessayez dans quelques minutes.',
      en: 'Too many attempts. Try again in a few minutes.',
    ),
    'CONTACT_ALREADY_USED': AppLanguage.pick(
      fr: 'Cet e-mail ou ce téléphone est déjà enregistré.',
      en: 'This email or phone is already registered.',
    ),
    'EMAIL_REQUIRED': AppLanguage.pick(
      fr: 'Saisissez votre adresse e-mail.',
      en: 'Enter your email address.',
    ),
    'INVALID_PHONE': AppLanguage.pick(
      fr: 'Ce numéro de téléphone n’est pas valide.',
      en: 'This phone number is not valid.',
    ),
    'INVALID_CODE': AppLanguage.pick(
      fr: 'Le code est invalide ou a expiré.',
      en: 'The code is invalid or has expired.',
    ),
    'VERIFICATION_COOLDOWN': AppLanguage.pick(
      fr: 'Patientez un instant avant de demander un nouveau code.',
      en: 'Please wait a moment before requesting a new code.',
    ),
    'INVALID_QR': AppLanguage.pick(
      fr: 'Ce QR code n’est pas reconnu ou a expiré.',
      en: 'This QR code is not recognised or has expired.',
    ),
    'TOO_MANY_REQUESTS': AppLanguage.pick(
      fr: 'Trop de requêtes. Réessayez dans un instant.',
      en: 'Too many requests. Try again in a moment.',
    ),
    'SERVICE_UNAVAILABLE': AppLanguage.pick(
      fr: 'Le service est momentanément indisponible. Réessayez plus tard.',
      en: 'The service is temporarily unavailable. Try again later.',
    ),
    'INTERNAL_ERROR': AppLanguage.pick(
      fr: 'Une erreur inattendue est survenue. Réessayez ou contactez le support.',
      en: 'An unexpected error occurred. Try again or contact support.',
    ),
    'UNSUPPORTED_COUNTRY': AppLanguage.pick(
      fr: 'Ce pays n’est pas encore pris en charge.',
      en: 'This country is not supported yet.',
    ),
    'UNAUTHENTICATED': AppLanguage.pick(
      fr: 'Votre session a expiré. Veuillez vous reconnecter.',
      en: 'Your session has expired. Please sign in again.',
    ),
    'INSUFFICIENT_FUNDS': AppLanguage.pick(
      fr: 'Créditez votre portefeuille pour démarrer la recharge.',
      en: 'Top up your wallet to start charging.',
    ),
    'INVALID_AMOUNT': AppLanguage.pick(
      fr: 'Ce montant n’est pas autorisé.',
      en: 'This amount is not allowed.',
    ),
    'TOPUP_NOT_FOUND': AppLanguage.pick(
      fr: 'Ce paiement est introuvable.',
      en: 'This payment could not be found.',
    ),
    'PROVIDER_UNAVAILABLE': AppLanguage.pick(
      fr: 'Mobile Money est momentanément indisponible.',
      en: 'Mobile money is temporarily unavailable.',
    ),
    'WRONG_PASSWORD': AppLanguage.pick(
      fr: 'Le mot de passe est incorrect.',
      en: 'The password is incorrect.',
    ),
    'WALLET_NOT_EMPTY': AppLanguage.pick(
      fr: 'Videz d’abord votre portefeuille avant de supprimer le compte.',
      en: 'Empty your wallet before deleting the account.',
    ),
    'INVALID_AVATAR': AppLanguage.pick(
      fr: 'Cette photo n’est pas acceptée (JPEG, PNG ou WebP, 1 Mo maximum).',
      en: 'This photo is not accepted (JPEG, PNG or WebP, 1 MB maximum).',
    ),
    'VALIDATION_FAILED': AppLanguage.pick(
      fr: 'Vérifiez les champs signalés.',
      en: 'Please check the highlighted fields.',
    ),
    ...ChargingErrorText.byCode,
  };
}
