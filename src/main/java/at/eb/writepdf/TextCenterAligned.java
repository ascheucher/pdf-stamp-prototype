package at.eb.writepdf;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;


public class TextCenterAligned {

    public static void main(String args[]) {
        try {
            PDDocument doc = PDDocument.loadNonSeq(new File("/tmp/TEST.pdf"), null);
            if (doc != null) {
                List allPages = doc.getDocumentCatalog().getAllPages();
                if (allPages != null && allPages.size() > 0) {
                    PDPage firstPage = (PDPage) allPages.get(0);
                    PDRectangle pageSize = firstPage.findMediaBox();
                    PDRectangle cropSize = firstPage.findCropBox();
                    float width = pageSize.getWidth();
                    int sourceDocumentFilePages = allPages.size();
                    int pageNo = 1;
                    float differenceY = 0;
                    if (cropSize.getHeight() != cropSize.getLowerLeftX()) {
                        differenceY = pageSize.getLowerLeftX() - cropSize.getLowerLeftX();
                    }
                    if (differenceY <= 0)
                        differenceY = 0;
                    differenceY = -10;
                    for (pageNo = 0; pageNo < sourceDocumentFilePages; pageNo++) {
                        PDPage page = (PDPage) allPages.get(pageNo);
                        PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true, true);
                        drawTextInCenter(contentStream, width, pageSize.getHeight(), cropSize.getLowerLeftX(),
                                differenceY, page, "<TEST TEXT - Center Aligned Text>");
                    }
                }
                doc.save("/tmp/GeneratedDOC.pdf");
                doc.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void drawTextInCenter(PDPageContentStream contentStream,
                                        float pageWidth, float pageHeight, float leftCropSize,
                                        float differenceY, PDPage page, String content) throws IOException {

        try {
            PDFont smallFont = PDType1Font.TIMES_ROMAN;
            PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
            borderULine.setWidth(1);
            float textWidth = (smallFont.getStringWidth(content) / 1000F * 7);

            float imageX = (float) (((pageWidth / 2) - (textWidth / 2)) + leftCropSize);
            float imageY = pageHeight - (pageHeight - 4 + differenceY);

            contentStream.beginText();
            contentStream.setFont(smallFont, 7);
            PDRectangle pageSize = page.findMediaBox();
            if (page.findRotation() == 270) {

                imageX = (float) (((pageHeight / 2) - (textWidth / 2)) + leftCropSize) + (float) (pageSize.getLowerLeftX());
                imageY = (float) (pageSize.getUpperRightX() - 10);
                float[] cords = getActualXY(imageX, imageY, page);
                imageX = cords[0];
                imageY = cords[1];
                AffineTransform transform = new AffineTransform();
                transform.translate(imageX, imageY);
                transform.rotate(Math.toRadians(-90));
                contentStream.setTextMatrix(transform);
            } else if (page.findRotation() == 90) {

                imageX = (float) (pageSize.getLowerLeftX()) + (float) (((pageHeight / 2) - (textWidth / 2)) + leftCropSize);
                imageY = (float) (pageSize.getUpperRightX() - 10);

                float[] cords = getActualXY(imageX, imageY, page);
                imageX = cords[0];
                imageY = cords[1];
                AffineTransform transform = new AffineTransform();
                transform.translate(imageX, imageY);
                transform.rotate(Math.toRadians(90));
                contentStream.setTextMatrix(transform);
            } else {
                contentStream.moveTextPositionByAmount(imageX, imageY);
            }
            contentStream.drawString(content);
            contentStream.endText();
            contentStream.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static float[] getActualXY(float x, float y, PDPage pdPage) {

        PDRectangle mediaBox = pdPage.getMediaBox();
        PDRectangle cropBox = pdPage.getCropBox();

        if (cropBox == null)
            cropBox = mediaBox;

        int mediaW = (int) mediaBox.getWidth();
        int mediaH = (int) mediaBox.getHeight();

        int cropX = (int) cropBox.getLowerLeftX();
        int cropY = (int) cropBox.getLowerLeftY();
        int cropW = (int) cropBox.getWidth();
        int cropH = (int) cropBox.getHeight();

        int rotation = pdPage.findRotation();

        float[] ret = new float[2];
        if (rotation == 90) {
            ret[1] = x + cropY;
            ret[0] = y + cropX;
        } else if ((rotation == 180)) {
            ret[0] = mediaW - (x + mediaW - cropW - cropX);
            ret[1] = y + cropY;
        } else if ((rotation == 270)) {
            ret[1] = mediaH - (x + mediaH - cropH - cropY);
            ret[0] = mediaW - (y + mediaW - cropW - cropX);
        } else {
            ret[0] = x;
            ret[1] = y;
        }
        return ret;
    }
}