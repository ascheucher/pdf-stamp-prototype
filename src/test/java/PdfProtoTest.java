import at.eb.writepdf.PdfProto;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static at.eb.writepdf.PdfProto.PdfStampMarker.*;

/**
 * Created by andi on 16.12.14.
 */
public class PdfProtoTest {

    private static final String TEMPLATE_LANDSCAPE_WRONG_PDF = "BeispielGebaeude_Grundriss_OG02_landscape_wrong.pdf";
    private static final String TEMPLATE_PORTRAIT_PDF = "BeispielGebaeude_inkl_OEN-A3_portrait.pdf";
    private static final String TEMPLATE_LANDSCAPE_PDF = "Pland_landscape.pdf";
    private static final String TEST_RESOURCES = "src/test/resources";
    private static final String TMP_DIR = "build/tmp/tests";

    private static final String OUTPUT_LANDSCAPE_WRONG_PDF = TMP_DIR + "/StampedOutput-WrongLandscape.pdf";
    private static final String OUTPUT_PORTRAIT_PDF = TMP_DIR + "/StampedPortraitOutput.pdf";
    private static final String OUTPUT_PORTRAIT_MARKED_PDF = TMP_DIR + "/StampedPortraitMarkedOutput.pdf";
    private static final String OUTPUT_LANDSCAPE_PDF = TMP_DIR + "/StampedLandscapeOutput.pdf";
    private static final String OUTPUT_LANDSCAPE_MARKED_PDF = TMP_DIR + "/StampedLandscapeMarkedOutput.pdf";
    private static final String ARROW_LEFT = "arrow_left.jpg";

    private static final String RI_RI_EXAMPLE = "HBK_AP_--_EG01_01_G_RiRi_46_AR1380_G06.pdf";
    private static final String RI_RI_EXAMPLE_OUT = TMP_DIR + "/HBK_AP_--_EG01_01_G_RiRi_46_AR1380_G06_stamped.pdf";
    private static final String RI_RI_MARKER_EXAMPLE_OUT = TMP_DIR + "/HBK_AP_--_EG01_01_G_RiRi_46_AR1380_G06_stamped_marker.pdf";

    @Ignore
    @Test
    public void stampImageOverRiRiPlan() throws IOException, PdfProto.PdfStampException {
        doStampImageTest(RI_RI_EXAMPLE, ARROW_LEFT, RI_RI_EXAMPLE_OUT);
    }

    @Ignore
    @Test
    public void stampImageOverPlanWrongLandscape() throws IOException, PdfProto.PdfStampException {
        // papier in hochformat, druck in querformat in ACAD PDF drucktreiber.
        doStampImageTest(TEMPLATE_LANDSCAPE_WRONG_PDF, ARROW_LEFT, OUTPUT_LANDSCAPE_WRONG_PDF);
    }

    @Ignore
    @Test
    public void stampImageOverPortraitPlan() throws IOException, PdfProto.PdfStampException {
        doStampImageTest(TEMPLATE_PORTRAIT_PDF, "arrow_left.jpg", OUTPUT_PORTRAIT_PDF);
    }

    @Ignore
    @Test
    public void stampImageOverPlanLandscape() throws IOException, PdfProto.PdfStampException {
        doStampImageTest(TEMPLATE_LANDSCAPE_PDF, "arrow_left.jpg", OUTPUT_LANDSCAPE_PDF);
    }

    private void doStampImageTest(String inputPdf, String stampImage, String outputPdf) throws IOException, PdfProto.PdfStampException {
        InputStream template = ClassLoader.getSystemResourceAsStream(inputPdf);
        File tmpDir = new File(TMP_DIR);
        if (!tmpDir.isDirectory()) {
            boolean success = tmpDir.mkdirs();
        }
        File outFile = new File(outputPdf);
        System.out.println("Write to " + outFile.getAbsolutePath());
        boolean success = outFile.createNewFile();
        OutputStream target = new FileOutputStream(outFile);
        List<PdfProto.PdfStampJpeg> images = new ArrayList();
        File file = new File(TEST_RESOURCES + "/" + stampImage);
        System.out.println("file: " + file.getAbsolutePath());
        images.add(new PdfProto.PdfStampJpeg(0, 0, 500, 250, file));
        PdfProto.stampImageOverlay(template, target, images);
    }

    @Test
    public void stampMarkerOverPortraitPlan() throws IOException, PdfProto.PdfStampException {
        doStampMarkerTest(TEMPLATE_PORTRAIT_PDF, OUTPUT_PORTRAIT_MARKED_PDF, 0.1f, 50, 50);
    }

    @Test
    public void stampMarkerOverPlanLandscape() throws IOException, PdfProto.PdfStampException {
        doStampMarkerTest(TEMPLATE_LANDSCAPE_PDF, OUTPUT_LANDSCAPE_MARKED_PDF, 0.1f, 110, 90);
    }

    @Test
    public void stampMarkerOverRiRiPlan() throws IOException, PdfProto.PdfStampException {
        doStampMarkerTest(RI_RI_EXAMPLE, RI_RI_MARKER_EXAMPLE_OUT, 0.1f, 50, 50);
    }

    private void doStampMarkerTest(String inputPdf, String outputPdf, float scale, float xOffset, float yOffset) throws IOException, PdfProto.PdfStampException {
        InputStream template = ClassLoader.getSystemResourceAsStream(inputPdf);
        File tmpDir = new File(TMP_DIR);
        if (!tmpDir.isDirectory()) {
            tmpDir.mkdirs();
        }
        File outFile = new File(outputPdf);
        System.out.println("Write to " + outFile.getAbsolutePath());
        outFile.createNewFile();
        OutputStream target = new FileOutputStream(outFile);
        List<PdfProto.PdfStampMarker> markers = new ArrayList();
        Color color = new Color(255, 163, 25);
        markers.add(new PdfProto.PdfStampMarker("Point Top", 1000 * scale + xOffset, 1000 * scale + yOffset, 1100 * scale + xOffset, 2000 * scale + yOffset, color));
        markers.add(new PdfProto.PdfStampMarker("Point Right", 1000 * scale + xOffset, 1000 * scale + yOffset, 2000 * scale + xOffset, 900 * scale + yOffset, color));
        markers.add(new PdfProto.PdfStampMarker("Point Bottom", 1000 * scale + xOffset, 1000 * scale + yOffset, 900 * scale + xOffset, 100 * scale + yOffset, color));
        markers.add(new PdfProto.PdfStampMarker("Point Left", 1000 * scale + xOffset, 1000 * scale + yOffset, 100 * scale + xOffset, 1100 * scale + yOffset, color));
        PdfProto.stampMarkerOverlay(template, target, markers);
    }
}
