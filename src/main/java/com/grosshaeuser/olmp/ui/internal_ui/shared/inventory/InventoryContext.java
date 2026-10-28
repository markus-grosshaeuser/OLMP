package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.media.MediaInventoryItemDto;
import com.grosshaeuser.olmp.inventory.service.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.List;

@Component
@RequiredArgsConstructor
@Getter
public class InventoryContext {
    private final MediaManagementService mediaManagementService;
    private final MediaLookupService mediaLookupService;
    private final MediaReferenceService mediaReferenceService;

    private final InventoryNumberService inventoryNumberService;
    private final BarcodeService barcodeService;
    private final BarcodePrintService barcodePrintService;


    public String generateInventoryNumber() {
        return inventoryNumberService.generate();
    }

    public BufferedImage generateBarcode(String barcodeData) {
        return barcodeService.generateBarcode(barcodeData);
    }

    public byte[] generateBarcodePng(String barcodeData) {
        return barcodeService.generateBarcodePng(barcodeData);
    }

    public String createPrintDocument(String mediaTitle, List<MediaInventoryItemDto> mediaInventoryItems) {
        return barcodePrintService.createPrintDocument(mediaTitle, mediaInventoryItems);
    }
}
