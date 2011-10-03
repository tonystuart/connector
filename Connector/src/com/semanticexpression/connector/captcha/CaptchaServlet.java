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

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CaptchaServlet extends HttpServlet
{
  public static final String CAPTCHA_KEY = "captchaKey";

  private String characterSet;
  private String fontName;
  private int fontSize;
  private int imageHeight;
  private int imageWidth;
  private int keyLength;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Max-Age", 0);

    String captchaKey = CaptchaCreator.generateKey(characterSet, keyLength);
    HttpSession session = request.getSession(true);
    session.setAttribute(CAPTCHA_KEY, captchaKey);

    OutputStream outputStream = response.getOutputStream();
    CaptchaCreator.createImage(captchaKey, fontName, fontSize, imageWidth, imageHeight, outputStream);
    outputStream.close();
  }

  private int getRequiredIntParameter(String parameterName)
  {
    return Integer.parseInt(getRequiredParameter(parameterName));
  }

  private String getRequiredParameter(String parameterName)
  {
    return getServletConfig().getInitParameter(parameterName);
  }

  @Override
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
    characterSet = getRequiredParameter("characterSet");
    keyLength = getRequiredIntParameter("keyLength");
    fontName = getRequiredParameter("fontName");
    fontSize = getRequiredIntParameter("fontSize");
    imageWidth = getRequiredIntParameter("imageWidth");
    imageHeight = getRequiredIntParameter("imageHeight");
  }

}
