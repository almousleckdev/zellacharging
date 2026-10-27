import 'app_language.dart';

abstract final class ChargingErrorText {
  static Map<String, String> get byCode => {
    'WITHDRAWAL_FAILED': AppLanguage.pick(
      fr: 'Le retrait n’a pas pu être effectué. Votre argent est resté dans votre portefeuille.',
      en: 'The withdrawal could not be completed. Your money stayed in your wallet.',
    ),
    'WITHDRAWAL_VERIFYING': AppLanguage.pick(
      fr: 'Votre retrait est en cours de vérification. Ne refaites pas la demande : le résultat sera visible dans votre historique.',
      en: 'Your withdrawal is being verified. Please do not request it again: the result will appear in your history.',
    ),
    'WITHDRAWALS_UNAVAILABLE': AppLanguage.pick(
      fr: 'Les retraits ne sont pas encore disponibles.',
      en: 'Withdrawals are not available yet.',
    ),
    'WITHDRAWAL_LIMIT_REACHED': AppLanguage.pick(
      fr: 'La limite de retrait quotidienne est atteinte.',
      en: 'The daily withdrawal limit has been reached.',
    ),
    'PHONE_NOT_VERIFIED': AppLanguage.pick(
      fr: 'Vérifiez d’abord votre numéro de téléphone.',
      en: 'Please verify your phone number first.',
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
    'INVALID_ACCOUNT_NUMBER': AppLanguage.pick(
      fr: 'Ce numéro n’est pas valide ou n’a pas de compte Airtel Money ou Moov Money. Vérifiez le numéro et l’opérateur, puis réessayez.',
      en: 'This number is not valid or has no Airtel Money or Moov Money account. Check the number and the operator, then try again.',
    ),
    'PROVIDER_REFUSED': AppLanguage.pick(
      fr: 'Votre opérateur a refusé la demande de paiement. Aucun montant n’a été débité. Vérifiez votre numéro et réessayez.',
      en: 'Your operator refused the payment request. Nothing was charged. Check your number and try again.',
    ),
    'PAYMENT_DECLINED': AppLanguage.pick(
      fr: 'Le paiement a été refusé ou annulé. Aucun montant n’a été débité. Vérifiez votre solde Mobile Money et réessayez.',
      en: 'The payment was declined or cancelled. Nothing was charged. Check your mobile money balance and try again.',
    ),
    'EXPIRED': AppLanguage.pick(
      fr: 'Le paiement n’a pas été validé à temps sur votre téléphone. Aucun montant n’a été débité. Vous pouvez réessayer.',
      en: 'The payment was not approved on your phone in time. Nothing was charged. You can try again.',
    ),
    'PAYMENT_NOT_COMPLETED': AppLanguage.pick(
      fr: 'Le paiement n’a pas pu être confirmé. Votre session de recharge n’a pas démarré. Veuillez vérifier votre paiement ou réessayer.',
      en: 'Your payment could not be confirmed. Your charging session has not started. Please check your payment or try again.',
    ),
    'PAYMENT_VERIFYING': AppLanguage.pick(
      fr: 'Votre paiement est en cours de vérification. La recharge n’a pas démarré. Ne réessayez pas tout de suite : votre solde sera crédité dès la confirmation.',
      en: 'Your payment is being verified. Charging has not started. Please do not retry yet: your balance will be credited as soon as it is confirmed.',
    ),
    'TOPUP_PENDING': AppLanguage.pick(
      fr: 'Un paiement est déjà en cours de vérification. Patientez avant d’en lancer un autre.',
      en: 'A payment is already being verified. Please wait before starting another.',
    ),
    'STATION_OFFLINE': AppLanguage.pick(
      fr: 'Cette borne est hors ligne.',
      en: 'This station is offline.',
    ),
    'CONNECTOR_NOT_FOUND': AppLanguage.pick(
      fr: 'Ce connecteur est introuvable.',
      en: 'This connector could not be found.',
    ),
    'CONNECTOR_UNAVAILABLE': AppLanguage.pick(
      fr: 'Ce connecteur n’est pas disponible. Vérifiez qu’il est libre et branché.',
      en: 'This connector is not available. Check that it is free and plugged in.',
    ),
    'IDEMPOTENCY_KEY_REUSED': AppLanguage.pick(
      fr: 'Cette demande a déjà été utilisée avec d’autres paramètres. Réessayez.',
      en: 'This request was already used with different parameters. Try again.',
    ),
    'OPERATOR_UNAVAILABLE': AppLanguage.pick(
      fr: 'Cet opérateur n’est pas disponible pour le moment.',
      en: 'This operator is not available right now.',
    ),
  };
}
