package com.grosshaeuser.olmp.inventory.service;

import com.grosshaeuser.olmp.inventory.dto.media.MediaInventoryItemDto;

import java.util.List;

public interface BarcodePrintService {
    String createPrintDocument(String mediaTitle, List<MediaInventoryItemDto> copies);
}
