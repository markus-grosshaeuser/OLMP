package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public interface ConfirmDelete {
    default ConfirmDialog getConfirmDialog(String header, Html text, ComponentEventListener<ConfirmDialog.ConfirmEvent> listener) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.addClassName("confirm-delete-dialog");
        dialog.setHeader(header);
        dialog.setText(text);
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(listener);
        return dialog;
    }
}
