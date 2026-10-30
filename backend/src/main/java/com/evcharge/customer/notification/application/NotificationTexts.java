package com.evcharge.customer.notification.application;

import com.evcharge.customer.notification.domain.PendingNotification;
import com.evcharge.customer.notification.domain.PushContent;
import com.evcharge.customer.shared.NotificationType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationTexts {

    private static final String ENGLISH = "en";

    public PushContent render(PendingNotification notification, String language) {
        boolean english = ENGLISH.equalsIgnoreCase(language);
        String[] text = textOf(notification.type(), english);
        Map<String, String> data = new HashMap<>(notification.params());
        data.put("type", notification.type().name());
        data.put("notificationId", notification.id().toString());
        return new PushContent(text[0], text[1], data);
    }

    private static String[] textOf(NotificationType type, boolean en) {
        return switch (type) {
            case TOPUP_STARTED -> pick(en, "Recharge en cours", "Validez le paiement sur votre téléphone.",
                    "Top-up in progress", "Approve the payment on your phone.");
            case TOPUP_SUCCEEDED -> pick(en, "Recharge réussie", "Votre portefeuille a été crédité.",
                    "Top-up successful", "Your wallet has been credited.");
            case TOPUP_FAILED -> pick(en, "Recharge échouée", "Le paiement n'a pas abouti.",
                    "Top-up failed", "The payment did not go through.");
            case WITHDRAWAL_STARTED -> pick(en, "Retrait en cours", "Votre retrait est en cours de traitement.",
                    "Withdrawal in progress", "Your withdrawal is being processed.");
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
        };
    }

    private static String[] pick(boolean en, String frTitle, String frBody, String enTitle, String enBody) {
        return en ? new String[] {enTitle, enBody} : new String[] {frTitle, frBody};
    }
}
