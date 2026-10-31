import 'app_language.dart';

abstract final class ProfileText {
  static String get language => AppLanguage.pick(fr: 'Langue', en: 'Language');

  static String get chooseLanguage =>
      AppLanguage.pick(fr: 'Choisissez une langue', en: 'Choose a language');

  static String get english => AppLanguage.pick(fr: 'English', en: 'English');

  static String get french => AppLanguage.pick(fr: 'Français', en: 'French');

  static String get editProfile =>
      AppLanguage.pick(fr: 'Modifier le profil', en: 'Edit profile');

  static String get saveChanges =>
      AppLanguage.pick(fr: 'Enregistrer', en: 'Save');

  static String get profileUpdated =>
      AppLanguage.pick(fr: 'Profil mis à jour', en: 'Profile updated');

  static String get logOut =>
      AppLanguage.pick(fr: 'Se déconnecter', en: 'Log out');

  static String get logOutTitle =>
      AppLanguage.pick(fr: 'Se déconnecter ?', en: 'Log out?');

  static String get logOutBody => AppLanguage.pick(
    fr: 'Vous devrez vous reconnecter pour continuer à recharger.',
    en: 'You will need to sign in again to keep charging.',
  );

  static String get myCoupons =>
      AppLanguage.pick(fr: 'Mes coupons', en: 'My coupons');

  static String get myWallet =>
      AppLanguage.pick(fr: 'Mon portefeuille', en: 'My wallet');

  static String get contactUs =>
      AppLanguage.pick(fr: 'Nous contacter', en: 'Contact us');

  static String get agreement => AppLanguage.pick(
    fr: 'Conditions d’utilisation',
    en: 'Platform agreement',
  );

  static String get settingsTitle =>
      AppLanguage.pick(fr: 'Paramètres', en: 'Settings');

  static String get changePhoto =>
      AppLanguage.pick(fr: 'Changer la photo', en: 'Change photo');

  static String get takePhoto =>
      AppLanguage.pick(fr: 'Prendre une photo', en: 'Take a photo');

  static String get chooseFromGallery => AppLanguage.pick(
    fr: 'Choisir dans la galerie',
    en: 'Choose from gallery',
  );

  static String get removePhoto =>
      AppLanguage.pick(fr: 'Supprimer la photo', en: 'Remove photo');

  static String get personalInfo => AppLanguage.pick(
    fr: 'Informations personnelles',
    en: 'Personal information',
  );

  static String get notificationsTitle =>
      AppLanguage.pick(fr: 'Notifications', en: 'Notifications');

  static String get notificationsBody => AppLanguage.pick(
    fr: 'Alertes de recharge et de portefeuille',
    en: 'Charging and wallet alerts',
  );

  static String get securitySection =>
      AppLanguage.pick(fr: 'Sécurité', en: 'Security');

  static String get changePassword =>
      AppLanguage.pick(fr: 'Changer le mot de passe', en: 'Change password');

  static String get currentPassword =>
      AppLanguage.pick(fr: 'Mot de passe actuel', en: 'Current password');

  static String get newPassword =>
      AppLanguage.pick(fr: 'Nouveau mot de passe', en: 'New password');

  static String get passwordChanged => AppLanguage.pick(
    fr: 'Mot de passe modifié. Reconnectez-vous.',
    en: 'Password changed. Please sign in again.',
  );

  static String get dangerSection =>
      AppLanguage.pick(fr: 'Compte', en: 'Account');

  static String get deleteAccount =>
      AppLanguage.pick(fr: 'Supprimer mon compte', en: 'Delete my account');

  static String get deleteAccountBody => AppLanguage.pick(
    fr: 'Votre compte et vos données personnelles seront supprimés définitivement. Cette action est irréversible. Votre portefeuille doit être vide et aucune recharge ne doit être en cours.',
    en: 'Your account and personal data will be permanently deleted. This cannot be undone. Your wallet must be empty and no charge may be in progress.',
  );

  static String get deleteWithProvider => AppLanguage.pick(
    fr: 'Confirmez avec le service que vous utilisez pour vous connecter.',
    en: 'Confirm with the service you use to sign in.',
  );

  static String get deleteAccountTitle => AppLanguage.pick(
    fr: 'Supprimer votre compte ?',
    en: 'Delete your account?',
  );

  static String get deleteAccountConfirm => AppLanguage.pick(
    fr: 'Supprimer définitivement',
    en: 'Delete permanently',
  );

  static String get contactTitle =>
      AppLanguage.pick(fr: 'Nous contacter', en: 'Contact us');

  static String get contactNotConfigured => AppLanguage.pick(
    fr: 'Les coordonnées du support seront bientôt disponibles.',
    en: 'Support contact details will be available soon.',
  );

  static String get agreementUnavailable => AppLanguage.pick(
    fr: 'Ce document sera bientôt disponible.',
    en: 'This document will be available soon.',
  );

  static String get couponsEmpty => AppLanguage.pick(
    fr: 'Vous n’avez aucun coupon pour le moment.',
    en: 'You have no coupons for now.',
  );

  static String get whatsapp =>
      AppLanguage.pick(fr: 'WhatsApp', en: 'WhatsApp');

  static String couponValidUntil(String date) =>
      AppLanguage.pick(fr: 'Valable jusqu’au $date', en: 'Valid until $date');

  static String get photoUpdated =>
      AppLanguage.pick(fr: 'Photo mise à jour', en: 'Photo updated');

  static String get photoRemoved =>
      AppLanguage.pick(fr: 'Photo supprimée', en: 'Photo removed');

  static String get contactDetails =>
      AppLanguage.pick(fr: 'Coordonnées', en: 'Contact details');

  static String get verified => AppLanguage.pick(fr: 'Vérifié', en: 'Verified');

  static String get notVerified =>
      AppLanguage.pick(fr: 'Non vérifié', en: 'Not verified');

  static String get addEmail => AppLanguage.pick(
    fr: 'Ajouter une adresse e-mail',
    en: 'Add an email address',
  );

  static String get addPhone => AppLanguage.pick(
    fr: 'Ajouter un numéro de téléphone',
    en: 'Add a phone number',
  );

  static String get addEmailHint => AppLanguage.pick(
    fr: 'Nous enverrons un code de confirmation à cette adresse.',
    en: 'We will send a confirmation code to this address.',
  );

  static String get addPhoneHint => AppLanguage.pick(
    fr: 'Ce numéro vous servira à vous connecter. La vérification par SMS arrivera bientôt.',
    en: 'This number can be used to sign in. SMS verification is coming soon.',
  );

  static String get sendCode =>
      AppLanguage.pick(fr: 'Envoyer le code', en: 'Send the code');

  static String get savePhone =>
      AppLanguage.pick(fr: 'Ajouter ce numéro', en: 'Add this number');

  static String get confirmEmailTitle =>
      AppLanguage.pick(fr: 'Confirmer l’e-mail', en: 'Confirm your email');

  static String confirmEmailBody(String email) => AppLanguage.pick(
    fr: 'Saisissez le code à 6 chiffres envoyé à $email.',
    en: 'Enter the 6-digit code sent to $email.',
  );

  static String get emailAdded =>
      AppLanguage.pick(fr: 'Adresse e-mail ajoutée', en: 'Email address added');

  static String get phoneAdded =>
      AppLanguage.pick(fr: 'Numéro ajouté', en: 'Phone number added');
}
