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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CaptchaRunner
{
  public static void main(String[] args)
  {
    try
    {
      String key = CaptchaCreator.generateKey("abcdefghjkmnopqrstuvwxyzABCDEFGHJKMNPQRSTUVXYZ23456789", 6);
      FileOutputStream fileOutputStream = new FileOutputStream("/home/tony/captcha.jpg");
      CaptchaCreator.createImage(key, "Verdana", 30, 150, 50, fileOutputStream);
      fileOutputStream.close();
    }
    catch (FileNotFoundException e)
    {
      throw new RuntimeException(e);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
}
