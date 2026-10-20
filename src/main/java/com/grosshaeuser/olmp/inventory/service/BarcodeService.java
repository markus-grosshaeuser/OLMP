package com.grosshaeuser.olmp.inventory.service;

import java.awt.image.BufferedImage;

public interface BarcodeService {
    BufferedImage generateBarcode(String barcodeData);

    byte[] generateBarcodePng(String barcodeData);
}
