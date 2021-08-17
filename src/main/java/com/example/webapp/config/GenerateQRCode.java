package com.example.webapp.config;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

public class GenerateQRCode  {
    public static BitMatrix ParamsQRCode(String text, String invid, String sn) throws WriterException, IOException {
        int size = 200;
        String qrCodeText = new String(("ТМЦ: " + text + "\n" + "Инв. №: " + invid + "\n" + "SN: " + sn + "\n" + "\n\nhttp://invent.regions.office.np-ivc.ru/find?searchsn=" + invid).getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        return byteMatrix;
    }

    public String createQRImage(String text, String invid, BitMatrix qrm) throws IOException {
        String fileType = "png";
        int matrixWidth = qrm.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
//                  Paint and save the image using the qrm
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (qrm.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        graphics.setFont(new Font("Arial", Font.PLAIN, 9));
        graphics.drawString(text + "  " + invid,10, 10);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, fileType, baos);

        String data = DatatypeConverter.printBase64Binary(baos.toByteArray());
        String imageString = "data:image/png;base64," + data;
        return imageString;

    }
}