package com.grosshaeuser.olmp.memberships.service.impl;

import com.grosshaeuser.olmp.memberships.service.MembershipBarcodeService;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@Service
public class MembershipBarcodeServiceImpl implements MembershipBarcodeService {
    @Override
    public BufferedImage generateBarcode(String barcodeData) {
        Code128Bean barcodeGenerator = new Code128Bean();
        barcodeGenerator.setFontSize(0.0);
        barcodeGenerator.setBarHeight(15);
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(320, BufferedImage.TYPE_BYTE_BINARY, false, 0);

        barcodeGenerator.generateBarcode(canvas, barcodeData);
        return canvas.getBufferedImage();
    }

    @Override
    public byte[] generateBarcodePng(String barcodeData) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(generateBarcode(barcodeData), "png", output)) {
                throw new IllegalStateException("PNG image encoding is unavailable");
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not encode barcode image", exception);
        }
    }
}
