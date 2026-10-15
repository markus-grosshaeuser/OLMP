package com.grosshaeuser.olmp.ui.internal_ui.shared.handler;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class UiExceptionHandler {

    public void handleException(RuntimeException e) {
        switch (e) {
            case OptimisticLockingFailureException _ -> showWarning(e.getMessage());
            case AccessDeniedException _,
                 IllegalArgumentException _,
                 DataIntegrityViolationException _ ->  showError(e.getMessage());
            default -> throw e;
        }
    }

    public void showError(String message) {
        showNotification(message, NotificationVariant.ERROR, 4000);
    }

    public void showWarning(String message) {
        showNotification(message, NotificationVariant.WARNING, 3000);
    }

    private void showNotification(String message, NotificationVariant variant, int duration) {
        Notification notification = new Notification(message);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.addThemeVariants(variant);
        notification.setDuration(duration);
        notification.open();
    }

}
