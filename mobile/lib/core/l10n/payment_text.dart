import 'app_language.dart';
import '../format/connector_label.dart';

abstract final class PayText {
  static String get budgetTitle =>
      AppLanguage.pick(fr: 'Budget de recharge', en: 'Charging budget');

  static String get budgetHelper => AppLanguage.pick(
    fr: 'La recharge s’arrête automatiquement quand ce montant est atteint.',
    en: 'Charging stops automatically when this amount is reached.',
  );

  static String estimatedEnergy(String kwh) => AppLanguage.pick(
    fr: '≈ $kwh kWh de recharge',
    en: '≈ $kwh kWh of charge',
  );

  static String range(String minimum, String maximum) => AppLanguage.pick(
    fr: 'Entre $minimum et $maximum',
    en: 'Between $minimum and $maximum',
  );

  static String get amountTitle =>
      AppLanguage.pick(fr: 'Montant à créditer', en: 'Amount to add');

  static String get methodTitle =>
      AppLanguage.pick(fr: 'Mode de paiement', en: 'Payment method');

  static String get walletCovers => AppLanguage.pick(
    fr: 'Votre solde couvre ce budget',
    en: 'Your balance covers this budget',
  );

  static String get walletAvailable =>
      AppLanguage.pick(fr: 'Solde disponible', en: 'Available balance');

  static String get walletReserved =>
      AppLanguage.pick(fr: 'Réservé', en: 'Reserved');

  static String get walletTitle =>
      AppLanguage.pick(fr: 'Mon portefeuille', en: 'My wallet');

  static String get shortfallExplained => AppLanguage.pick(
    fr: 'Votre solde ne couvre pas tout le budget. Complétez par Mobile Money.',
    en: 'Your balance does not cover the whole budget. Top up with mobile money.',
  );

  static String get summaryTitle =>
      AppLanguage.pick(fr: 'Récapitulatif', en: 'Summary');

  static String get budget => AppLanguage.pick(fr: 'Budget', en: 'Budget');

  static String get fromWallet =>
      AppLanguage.pick(fr: 'Depuis le portefeuille', en: 'From your wallet');

  static String get byMobileMoney =>
      AppLanguage.pick(fr: 'Par Mobile Money', en: 'By mobile money');

  static String get toPayNow =>
      AppLanguage.pick(fr: 'À payer maintenant', en: 'To pay now');

  static String get creditedToWallet => AppLanguage.pick(
    fr: 'Crédité sur votre portefeuille',
    en: 'Added to your wallet',
  );

  static String get unusedReturned => AppLanguage.pick(
    fr: 'Le montant non utilisé est reversé dans votre portefeuille.',
    en: 'Any unused amount is returned to your wallet.',
  );

  static String get secure => AppLanguage.pick(
    fr: 'Paiement sécurisé · confirmé par votre opérateur',
    en: 'Secure payment · confirmed by your operator',
  );

  static String payAndStart(String amount) => AppLanguage.pick(
    fr: 'Payer $amount et démarrer',
    en: 'Pay $amount and start',
  );

  static String payAmount(String amount) =>
      AppLanguage.pick(fr: 'Payer $amount', en: 'Pay $amount');

  static String get balanceUsed => AppLanguage.pick(
    fr: 'La recharge utilise votre solde disponible et s’arrête automatiquement quand il est épuisé.',
    en: 'Charging uses your available balance and stops automatically when it runs out.',
  );

  static String get freeCharging =>
      AppLanguage.pick(fr: 'Recharge gratuite', en: 'Free charging');

  static String get freeChargingNote => AppLanguage.pick(
    fr: 'Votre compte bénéficie de la recharge gratuite. Rien n’est débité de votre portefeuille.',
    en: 'Your account has free charging. Nothing is taken from your wallet.',
  );

  static String get walletUnknown =>
      AppLanguage.pick(fr: 'Portefeuille non chargé', en: 'Wallet not loaded');

  static String get walletUnknownNote => AppLanguage.pick(
    fr: 'Nous n’avons pas encore pu lire votre portefeuille. Vérifiez votre connexion puis actualisez.',
    en: 'We could not read your wallet yet. Check your connection, then refresh.',
  );

  static String get refreshWallet => AppLanguage.pick(
    fr: 'Actualiser mon portefeuille',
    en: 'Refresh my wallet',
  );

  static String get rechargeNeededTitle =>
      AppLanguage.pick(fr: 'Solde insuffisant', en: 'Insufficient balance');

  static String rechargeNeeded(String minimum) => AppLanguage.pick(
    fr: 'Il faut au moins $minimum dans votre portefeuille pour démarrer une recharge. Rechargez votre compte.',
    en: 'You need at least $minimum in your wallet to start a charge. Please top up your account.',
  );

  static String get rechargeAccount =>
      AppLanguage.pick(fr: 'Recharger mon compte', en: 'Top up my account');

  static String get startCharging =>
      AppLanguage.pick(fr: 'Démarrer la recharge', en: 'Start charging');

  static String get selectConnectorFirst => AppLanguage.pick(
    fr: 'Choisissez un connecteur pour continuer',
    en: 'Choose a connector to continue',
  );

  static String cableDetected(int connector) => AppLanguage.pick(
    fr: 'Câble détecté sur le connecteur ${ConnectorLabel.of(connector)}',
    en: 'Cable detected on connector ${ConnectorLabel.of(connector)}',
  );

  static String get cableSeveral => AppLanguage.pick(
    fr: 'Plusieurs câbles détectés. Choisissez votre connecteur.',
    en: 'Several cables detected. Choose your connector.',
  );

  static String get pluggedIn =>
      AppLanguage.pick(fr: 'Câble branché', en: 'Cable plugged in');

  static String get stationOffline =>
      AppLanguage.pick(fr: 'Borne hors ligne', en: 'Station offline');

  static String get stationOnline =>
      AppLanguage.pick(fr: 'Borne en ligne', en: 'Station online');

  static String get topupExplained => AppLanguage.pick(
    fr: 'Rechargez votre portefeuille par Mobile Money. Le solde est utilisable dès la confirmation de votre opérateur.',
    en: 'Top up your wallet with mobile money. The balance is usable as soon as your operator confirms.',
  );
}
