package com.grosshaeuser.olmp.inventory.service;

import com.grosshaeuser.olmp.inventory.service.impl.BarcodeServiceImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class BarcodeServiceTest {
    @ParameterizedTest
    @ValueSource(strings = {"e90e7f6e-10a0-427b-8c35-30e6e1c7f526", "existing-barcode"})
    void generatesReadablePngWithBarcodeBars(String value) throws Exception {
        byte[] png = new BarcodeServiceImpl().generateBarcodePng(value);
        var image = ImageIO.read(new ByteArrayInputStream(png));
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isGreaterThan(100);
        assertThat(image.getHeight()).isGreaterThan(20);
        boolean black = false;
        boolean white = false;
        for (int x = 0; x < image.getWidth(); x++) {
            int color = image.getRGB(x, image.getHeight() / 2) & 0xffffff;
            black |= color == 0;
            white |= color == 0xffffff;
        }
        assertThat(black).isTrue();
        assertThat(white).isTrue();
    }
}
