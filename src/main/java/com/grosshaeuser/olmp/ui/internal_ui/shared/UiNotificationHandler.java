package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.springframework.stereotype.Component;

@Component
public class UiNotificationHandler {

    public void showSuccessNotification(String message) {
        showNotification(message, NotificationVariant.SUCCESS, 3000);
    }

    public void showInfoNotification(String message) {
        showNotification(message, NotificationVariant.INFO, 4000);
    }

    private void showNotification(String message, NotificationVariant variant, int duration) {
        Notification notification = new Notification(message);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.addThemeVariants(variant);
        notification.setDuration(duration);
        notification.open();
    }
}
