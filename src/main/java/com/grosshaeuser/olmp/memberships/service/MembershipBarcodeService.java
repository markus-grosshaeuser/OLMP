package com.grosshaeuser.olmp.memberships.service;

import java.awt.image.BufferedImage;

public interface MembershipBarcodeService {
    BufferedImage generateBarcode(String barcodeData);

    byte[] generateBarcodePng(String barcodeData);
}
