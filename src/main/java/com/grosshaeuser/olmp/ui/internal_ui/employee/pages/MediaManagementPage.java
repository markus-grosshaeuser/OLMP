package com.grosshaeuser.olmp.ui.internal_ui.employee.pages;

import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.employee.EmployeeLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.inventory.InventoryContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.inventory.MediaManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "staff/media-management", layout = EmployeeLayout.class)
@PageTitle("Media Management")
@RolesAllowed(SecurityConstants.Roles.STAFF)
public class MediaManagementPage extends MediaManagementView {
    public MediaManagementPage(InventoryContext inventoryContext, UiContext uiContext) {
        super(inventoryContext, uiContext);
    }
}
