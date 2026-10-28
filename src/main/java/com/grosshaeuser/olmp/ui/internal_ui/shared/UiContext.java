package com.grosshaeuser.olmp.ui.internal_ui.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UiContext {
    private final UiExceptionHandler exceptionHandler;
    private final UiNotificationHandler notificationHandler;

    public void handleException(RuntimeException e) {
        exceptionHandler.handleException(e);
    }

    public void showError(String message) {
        exceptionHandler.showError(message);
    }

    public void showWarning(String message) {
        exceptionHandler.showWarning(message);
    }

    public void showSuccessNotification(String message) {
        notificationHandler.showSuccessNotification(message);
    }

    public void showInfoNotification(String message) {
        notificationHandler.showInfoNotification(message);
    }
}
