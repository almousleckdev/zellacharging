package com.evcharge.customer.notification.application;

import com.evcharge.customer.notification.domain.PendingNotification;
import com.evcharge.customer.notification.domain.PushContent;
import com.evcharge.customer.shared.NotificationType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationTexts {

    private static final String ENGLISH = "en";

    public PushContent render(PendingNotification notification, String language) {
        return render(notification.id(), notification.type(), notification.params(), language);
    }

    public PushContent render(UUID id, NotificationType type, Map<String, String> params, String language) {
        String[] text = textOf(type, ENGLISH.equalsIgnoreCase(language));
        Map<String, String> data = new HashMap<>(params);
        data.put("type", type.name());
        data.put("notificationId", id.toString());
        return new PushContent(text[0], text[1], data, 0);
    }

    private static String[] textOf(NotificationType type, boolean en) {
        return switch (type) {
            case TOPUP_STARTED -> pick(en, "Recharge en cours", "Validez le paiement sur votre téléphone.",
                    "Top-up in progress", "Approve the payment on your phone.");
            case TOPUP_SUCCEEDED -> pick(en, "Recharge réussie", "Votre portefeuille a été crédité.",
                    "Top-up successful", "Your wallet has been credited.");
            case TOPUP_FAILED -> pick(en, "Recharge échouée", "Le paiement n'a pas abouti.",
                    "Top-up failed", "The payment did not go through.");
            case WITHDRAWAL_STARTED -> pick(en, "Demande de retrait envoyée", "Votre retrait est en attente de validation.",
                    "Withdrawal request sent", "Your withdrawal is awaiting approval.");
            case WITHDRAWAL_APPROVED -> pick(en, "Retrait validé", "Votre retrait a été validé et est en cours d'envoi.",
                    "Withdrawal approved", "Your withdrawal was approved and is being sent.");
            case WITHDRAWAL_REJECTED -> pick(en, "Retrait refusé", "Le montant a été remis dans votre portefeuille.",
                    "Withdrawal declined", "The amount is back in your wallet.");
            case WITHDRAWAL_SUCCEEDED -> pick(en, "Retrait effectué", "L'argent a été envoyé sur votre compte.",
                    "Withdrawal completed", "The money was sent to your account.");
            case WITHDRAWAL_FAILED -> pick(en, "Retrait échoué", "Le montant a été remis dans votre portefeuille.",
                    "Withdrawal failed", "The amount is back in your wallet.");
            case CHARGE_STARTED -> pick(en, "Recharge démarrée", "Votre véhicule est en charge.",
                    "Charging started", "Your vehicle is charging.");
            case CHARGE_STOPPED -> pick(en, "Recharge arrêtée", "La recharge a été arrêtée.",
                    "Charging stopped", "The charge was stopped.");
            case CHARGE_COMPLETED -> pick(en, "Recharge terminée", "Consultez le détail de votre recharge.",
                    "Charging complete", "See the details of your charge.");
            case CHARGE_FAILED -> pick(en, "Recharge impossible", "La recharge n'a pas pu démarrer.",
                    "Charging failed", "The charge could not start.");
            case CHARGE_INTERRUPTED -> pick(en, "Recharge interrompue", "Votre recharge s'est arrêtée de façon inattendue.",
                    "Charging interrupted", "Your charge stopped unexpectedly.");
            case ACCOUNT_REGISTERED -> pick(en, "Bienvenue chez Zella", "Votre compte est prêt.",
                    "Welcome to Zella", "Your account is ready.");
            case PASSWORD_CHANGED -> pick(en, "Mot de passe modifié", "Si ce n'était pas vous, contactez le support.",
                    "Password changed", "If this was not you, contact support.");
            case ACCOUNT_SUSPENDED_ARREARS -> pick(en, "Compte suspendu",
                    "Votre compte est suspendu car votre loyer NDZela est impayé. Réglez-le pour le réactiver.",
                    "Account suspended",
                    "Your account is suspended because your NDZela rent is unpaid. Pay it to reactivate it.");
            case ACCOUNT_RESTORED -> pick(en, "Compte réactivé",
                    "Votre loyer est réglé : votre compte est de nouveau actif.",
                    "Account reactivated", "Your rent is paid: your account is active again.");
            case SUSPENDED_HELP_DETECTED -> pick(en, "Activité détectée",
                    "Un rechargement depuis votre numéro vers un autre compte a été détecté pendant votre suspension. "
                            + "Cela enfreint les conditions d'utilisation : des sanctions peuvent s'appliquer.",
                    "Activity detected",
                    "A top-up from your number into another account was detected during your suspension. "
                            + "This breaks the terms of use: sanctions may apply.");
            case HELPER_WARNED -> pick(en, "Avertissement",
                    "Votre compte a servi à aider une personne suspendue à recharger. "
                            + "Cela enfreint les conditions d'utilisation : des sanctions peuvent s'appliquer.",
                    "Warning",
                    "Your account was used to help a suspended person charge. "
                            + "This breaks the terms of use: sanctions may apply.");
        };
    }

    private static String[] pick(boolean en, String frTitle, String frBody, String enTitle, String enBody) {
        return en ? new String[] {enTitle, enBody} : new String[] {frTitle, frBody};
    }
}
