package com.grosshaeuser.olmp.ui.internal_ui.admin.pages;

import com.grosshaeuser.olmp.inventory.service.MediaLookupService;
import com.grosshaeuser.olmp.inventory.service.MediaManagementService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.admin.AdminLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiNotificationHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.inventory.MediaManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/media-management", layout = AdminLayout.class)
@PageTitle("Media Management")
@RolesAllowed(SecurityConstants.Roles.ADMIN)
public class MediaManagementPage extends MediaManagementView {
    public MediaManagementPage(MediaManagementService mediaService, MediaLookupService lookupService, UiExceptionHandler uiExceptionHandler, UiNotificationHandler uiNotificationHandler) {
        super(mediaService, lookupService, uiExceptionHandler, uiNotificationHandler);
    }
}
