package at.eb.writepdf;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDCalRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * Created by andi on 16.12.14.
 */
public class PdfProto {

    public static class PdfStampException extends Exception {

        public PdfStampException(String msg, Throwable e) {
            super(msg, e);
        }
    }

    public static class PdfStampJpeg {

        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final File file;

        public PdfStampJpeg(int x, int y, int width, int height, File file) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.file = file;
        }
    }

    public static void stampImageOverlay(InputStream templatePdf, OutputStream outputPdf,
                                         List<PdfStampJpeg> jpegs) throws PdfStampException {
        PDDocument doc = null;
        try {
            Iterator<PdfStampJpeg> jpegIterator = jpegs.iterator();
            if (!jpegIterator.hasNext()) {
                return;
            }
            doc = PDDocument.load(templatePdf);
            PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);
            while (jpegIterator.hasNext()) {
                PdfStampJpeg jpeg = jpegIterator.next();
                PDXObjectImage img = new PDJpeg(doc, new FileInputStream(jpeg.file));
                contentStream.drawXObject(img, jpeg.x, jpeg.y, jpeg.width, jpeg.height);
            }
            contentStream.close();
            doc.save(outputPdf);

        } catch (IOException e) {
            throw new PdfStampException("Could not stamp image to pdf", e);
        } catch (COSVisitorException e) {
            throw new PdfStampException("Could not stamp image to pdf", e);
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException e) {
                    throw new PdfStampException("Could not close doc", e);
                }
            }
        }
    }

    public static class PdfStampMarker {
        public static class Color {
            public final int r;
            public final int g;
            public final int b;

            public Color(int r, int g, int b) {
                this.r = r;
                this.g = g;
                this.b = b;
            }
        }

        public final String id;
        public final float startX;
        public final float startY;
        public final float endX;
        public final float endY;
        public final Color color;

        public PdfStampMarker(String id, float startX, float startY, float endX, float endY, Color color) {
            this.id = id;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.color = color;
        }

        private double dx() {
            return Math.abs(startX - endX);
        }

        private double dy() {
            return Math.abs(startY - endY);
        }

        private enum Location {
            TOP, RIGHT, BOTTOM, LEFT
        }

        private Location getLocation() {
            if (dx() > dy())
                if (startX < endX)
                    return Location.RIGHT;
                else
                    return Location.LEFT;
            else if (startY < endY)
                return Location.TOP;
            else
                return Location.BOTTOM;
        }

        public float getXTextOffset(float textWidth, float fontPadding) {
            if (getLocation() == Location.TOP)
                return (textWidth / 2 + fontPadding) * -1;
            else if (getLocation() == Location.BOTTOM)
                return (textWidth / 2 + fontPadding) * -1;
            else if (getLocation() == Location.RIGHT)
                return 0 + fontPadding;
            else
                return (textWidth + fontPadding) * -1;
        }

        public float getYTextOffset(float fontSize, float fontPadding) {
            if (getLocation() == Location.TOP)
                return 0 + fontPadding;
            else if (getLocation() == Location.BOTTOM)
                return (fontSize + fontPadding) * -1f;
            else
                return fontSize / 2 * -1;
        }
    }

    public static void stampMarkerOverlay(InputStream templatePdf, OutputStream outputPdf,
                                          List<PdfStampMarker> markers) throws PdfStampException {
        PDDocument doc = null;
        try {
            Iterator<PdfStampMarker> markerIterator = markers.iterator();
            if (!markerIterator.hasNext()) {
                return;
            }
            doc = PDDocument.load(templatePdf);
            PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);

            PDFont font = PDType1Font.HELVETICA;
            PDRectangle pageSize = page.findCropBox();
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();
            float lineWidth = Math.max(pageWidth, pageHeight) / 10000;
            float markerRadius = lineWidth * 10;
            float fontSize = Math.min(pageWidth, pageHeight) / 100;
            float fontPadding = Math.max(pageWidth, pageHeight) / 250;

            PDColorSpace colorSpace = PDColorSpaceFactory.createColorSpace(PDDeviceRGB.NAME);

            PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true, true);
            contentStream.saveGraphicsState();
            contentStream.setFont(font, fontSize);
            contentStream.setLineWidth(lineWidth);
            contentStream.setLineCapStyle(1);
            contentStream.setStrokingColorSpace(colorSpace);
            contentStream.setNonStrokingColorSpace(colorSpace);
            while (markerIterator.hasNext()) {
                PdfStampMarker marker = markerIterator.next();
                contentStream.setStrokingColor(marker.color.r, marker.color.g, marker.color.b);
                contentStream.setNonStrokingColor(marker.color.r, marker.color.g, marker.color.b);
                contentStream.drawLine(marker.startX, marker.startY, marker.endX, marker.endY);

                drawStartMarker(contentStream, marker.startX, marker.startY, markerRadius);

                float textWidth = font.getStringWidth(marker.id) * 0.001f * fontSize;
                contentStream.beginText();
                contentStream.setTextScaling(1, 1, 0, 0);
                contentStream.moveTextPositionByAmount(
                        marker.endX + marker.getXTextOffset(textWidth, fontPadding),
                        marker.endY + marker.getYTextOffset(fontSize, fontPadding));
                contentStream.drawString(marker.id);
                contentStream.endText();
            }
            contentStream.restoreGraphicsState();
            contentStream.close();
            doc.save(outputPdf);

        } catch (IOException e) {
            throw new PdfStampException("Could not stamp image to pdf", e);
        } catch (COSVisitorException e) {
            throw new PdfStampException("Could not stamp image to pdf", e);
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException e) {
                    throw new PdfStampException("Could not close doc", e);
                }
            }
        }
    }

    private static void drawStartMarker(PDPageContentStream contentStream, double centerX, double centerY, double markerRadius) throws IOException {
        int sections = 10;
        float[] x = new float[sections + 1];
        float[] y = new float[sections + 1];

        int i = 0;
        for (int angle = 0; angle <= 360; angle += 360 / sections) {
            Point p = polarToCartesian(centerX, centerY, markerRadius, angle);
            x[i] = (float) p.x;
            y[i] = (float) p.y;
            i++;
        }
        contentStream.fillPolygon(x, y);
    }

    private static class Point {
        public final double x;
        public final double y;

        private Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static Point polarToCartesian(double centerX, double centerY, double radius, double angleInDegrees) {
        double angleInRadians = (angleInDegrees - 90) * Math.PI / 180.0;

        return new Point(centerX + (radius * Math.cos(angleInRadians)),
                centerY + (radius * Math.sin(angleInRadians)));
    }
}
