package com.grosshaeuser.olmp.inventory.service;

import com.grosshaeuser.olmp.inventory.dto.media.MediaInventoryItemDto;
import com.grosshaeuser.olmp.inventory.service.impl.BarcodePrintServiceImpl;
import com.grosshaeuser.olmp.inventory.service.impl.BarcodeServiceImpl;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BarcodePrintServiceTest {
    private final BarcodePrintService service = new BarcodePrintServiceImpl(new BarcodeServiceImpl());

    @Test
    void createsA4LabelsWithEmbeddedBarcodesAndEscapedTitles() throws Exception {
        var first = MediaInventoryItemDto.newCopy();
        var second = MediaInventoryItemDto.newCopy();
        String html = service.createPrintDocument("<script>alert('x')</script> & Title", List.of(first, second));
        assertThat(html).contains("size: A4", "break-inside: avoid");
        assertThat(html).contains("&lt;script&gt;").doesNotContain("<script>");
        assertThat(html.split("<article ", -1)).hasSize(3);
        var images = Pattern.compile("data:image/png;base64,([^\"]+)").matcher(html);
        int count = 0;
        while (images.find()) {
            assertThat(ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(images.group(1))))).isNotNull();
            count++;
        }
        assertThat(count).isEqualTo(2);
        assertThat(html).doesNotContain("src=\"http", "<script", "onload=");
    }

    @Test
    void requiresCopiesWithPrintableBarcodeData() {
        assertThatIllegalArgumentException().isThrownBy(() -> service.createPrintDocument("Title", List.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> service.createPrintDocument("Title",
                List.of(new MediaInventoryItemDto(1L, "Inventory number", null))));
    }
}
