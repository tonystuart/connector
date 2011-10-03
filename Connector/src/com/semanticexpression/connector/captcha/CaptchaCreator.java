// Copyright 2011 Semantic Expression, Inc. All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the GNU General Public License, either version 3 or (at your option)
// any later version. The terms of this license may be found at
// http://www.gnu.org/copyleft/gpl.html
//
// This program is made available on an "as is" basis, without warranties or
// conditions of any kind, either express or implied.
//
// Please contact us for other licensing options.
//
// Contributors:
//
// Anthony F. Stuart - Initial implementation
//
//
//

package com.semanticexpression.connector.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

public class CaptchaCreator
{
  public static void createImage(String captchaKey, String fontName, int fontSize, int imageWidth, int imageHeight, OutputStream outputStream) throws IOException
  {
    BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    Font font = new Font(fontName, Font.PLAIN, fontSize);
    g2d.setFont(font);
    FontMetrics fontMetrics = g2d.getFontMetrics();
    int fontWidth = fontMetrics.getMaxAdvance();
    int fontHeight = fontMetrics.getHeight();

    Random random = new Random();
    int x1 = random.nextInt(fontWidth);
    int y1 = random.nextInt(fontHeight);
    int x2 = random.nextInt(fontWidth);
    int y2 = random.nextInt(fontHeight);

    g2d.setColor(Color.GRAY);
    g2d.fillRect(0, 0, imageWidth, imageHeight);
    GradientPaint gradientPaint = new GradientPaint(x1, y1, Color.BLACK, x2, y2, Color.WHITE, true);
    g2d.setPaint(gradientPaint);
    Rectangle2D captchaKeyBounds = fontMetrics.getStringBounds(captchaKey, g2d);
    int centeredX = (imageWidth - (int)captchaKeyBounds.getWidth()) / 2;
    int centeredY = ((imageHeight - (int)captchaKeyBounds.getHeight()) / 2) + (int)captchaKeyBounds.getHeight();
    g2d.drawString(captchaKey, centeredX, centeredY);
    g2d.dispose();
    ImageIO.write(image, "jpg", outputStream);
  }

  public static String generateKey(String characterSet, int length)
  {
    Random random = new Random();
    StringBuilder s = new StringBuilder();
    int characterSetLength = characterSet.length();
    for (int i = 0; i < length; i++)
    {
      int characterSetOffset = random.nextInt(characterSetLength);
      char randomCharacter = characterSet.charAt(characterSetOffset);
      s.append(randomCharacter);
    }
    return s.toString();
  }
}
