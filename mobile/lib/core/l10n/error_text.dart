import 'app_language.dart';

abstract final class ErrorText {
  static Map<String, String> get byCode => {
    'INVALID_CREDENTIALS': AppLanguage.pick(
      fr: 'E-mail, téléphone ou mot de passe incorrect.',
      en: 'Incorrect email, phone or password.',
    ),
    'ACCOUNT_NOT_VERIFIED': AppLanguage.pick(
      fr: 'Veuillez d’abord vérifier votre compte.',
      en: 'Please verify your account first.',
    ),
    'ACCOUNT_LOCKED': AppLanguage.pick(
      fr: 'Trop de tentatives. Réessayez dans quelques minutes.',
      en: 'Too many attempts. Try again in a few minutes.',
    ),
    'CONTACT_ALREADY_USED': AppLanguage.pick(
      fr: 'Cet e-mail ou ce téléphone est déjà enregistré.',
      en: 'This email or phone is already registered.',
    ),
    'CONTACT_REQUIRED': AppLanguage.pick(
      fr: 'Saisissez un e-mail ou un numéro de téléphone.',
      en: 'Enter an email or a phone number.',
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
    'STATION_NOT_FOUND': AppLanguage.pick(
      fr: 'Cette station est introuvable.',
      en: 'This station could not be found.',
    ),
    'CATALOG_UNAVAILABLE': AppLanguage.pick(
      fr: 'Le réseau de recharge est momentanément indisponible.',
      en: 'The charging network is temporarily unavailable.',
    ),
    'CONNECTOR_OR_SESSION_BUSY': AppLanguage.pick(
      fr: 'Une recharge est déjà en cours pour vous ou sur ce connecteur.',
      en: 'A charge is already in progress for you or on this connector.',
    ),
    'SESSION_NOT_ACTIVE': AppLanguage.pick(
      fr: 'Aucune recharge en cours à arrêter.',
      en: 'There is no active charge to stop.',
    ),
    'SESSION_NOT_FOUND': AppLanguage.pick(
      fr: 'Cette recharge est introuvable.',
      en: 'This charge could not be found.',
    ),
    'SESSION_STILL_ACTIVE': AppLanguage.pick(
      fr: 'Une recharge en cours ne peut pas être supprimée.',
      en: 'A charge in progress cannot be deleted.',
    ),
    'STOP_REFUSED': AppLanguage.pick(
      fr: 'La borne a refusé l’arrêt. Veuillez réessayer.',
      en: 'The charger refused to stop. Please try again.',
    ),
    'CHARGING_UNAVAILABLE': AppLanguage.pick(
      fr: 'Le réseau de recharge est momentanément indisponible.',
      en: 'The charging network is temporarily unavailable.',
    ),
    'ID_TAG_UNAVAILABLE': AppLanguage.pick(
      fr: 'Impossible de préparer votre identité de recharge. Réessayez.',
      en: 'Could not prepare your charging identity. Try again.',
    ),
    'TARIFF_NOT_CONFIGURED': AppLanguage.pick(
      fr: 'Les tarifs ne sont pas encore configurés pour cette station.',
      en: 'Pricing is not configured for this station yet.',
    ),
    'CHARGER_REFUSED': AppLanguage.pick(
      fr: 'La borne a refusé de démarrer. Essayez un autre connecteur.',
      en: 'The charger refused to start. Try another connector.',
    ),
    'CHARGER_TIMEOUT': AppLanguage.pick(
      fr: 'La borne n’a pas répondu à temps. Réessayez.',
      en: 'The charger did not answer in time. Try again.',
    ),
    'CHARGER_UNREACHABLE': AppLanguage.pick(
      fr: 'La borne est hors ligne.',
      en: 'The charger is offline.',
    ),
    'PAYMENT_NOT_COMPLETED': AppLanguage.pick(
      fr: 'Le paiement n’a pas abouti. Rien n’a été débité.',
      en: 'The payment was not completed. Nothing was charged.',
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
  };
}
