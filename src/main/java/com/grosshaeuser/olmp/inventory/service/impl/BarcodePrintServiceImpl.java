package com.grosshaeuser.olmp.inventory.service.impl;

import com.grosshaeuser.olmp.inventory.dto.media.MediaInventoryItemDto;
import com.grosshaeuser.olmp.inventory.service.BarcodePrintService;
import com.grosshaeuser.olmp.inventory.service.BarcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BarcodePrintServiceImpl implements BarcodePrintService {
    private final BarcodeService barcodeService;

    @Override
    public String createPrintDocument(String mediaTitle, List<MediaInventoryItemDto> copies) {
        Objects.requireNonNull(mediaTitle, "mediaTitle");
        if (copies == null || copies.isEmpty()) {
            throw new IllegalArgumentException("Select at least one copy to print");
        }
        String title = HtmlUtils.htmlEscape(mediaTitle);
        StringBuilder labels = new StringBuilder();
        for (var copy : copies) {
            if (copy == null || copy.inventoryNumber() == null || copy.inventoryNumber().isBlank()
                    || copy.barcode() == null || copy.barcode().isBlank()) {
                throw new IllegalArgumentException("Each label requires an inventory number and barcode");
            }
            String image = Base64.getEncoder().encodeToString(barcodeService.generateBarcodePng(copy.barcode()));
            labels
                    .append("<article class=\"label\"><div class=\"title\">")
                    .append(title)
                    .append("</div><img alt=\"Barcode\" src=\"data:image/png;base64,")
                    .append(image)
                    .append("\"></article>");
        }
        return """
                <!doctype html>
                <html lang="en"><head><meta charset="utf-8">
                <title>Barcode labels</title>
                <style>
                @page { size: A4 portrait; margin: 10mm; }
                * { box-sizing: border-box; }
                body { margin: 0; color: black; background: white; font-family: Arial, sans-serif; }
                main { display: grid; grid-template-columns: 1fr; gap: 4mm; justify-items: center; }
                .label { width: 124mm; height: 38mm; padding: 2mm; break-inside: avoid;
                page-break-inside: avoid; overflow: hidden; text-align: center; }
                .title { font-size: 10pt; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                img { display: block; width: 120mm; height: 14mm; object-fit: contain; margin: 1mm auto; }
                .number { font-family: monospace; font-size: 8pt; white-space: nowrap; }
                </style></head><body><main>
                """ + labels + "</main></body></html>";
    }
}
