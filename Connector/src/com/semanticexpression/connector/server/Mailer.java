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

package com.semanticexpression.connector.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mailer
{
  private Properties properties;

  public Mailer(String configurationFileName)
  {
    try
    {
      File file = new File(configurationFileName);
      BufferedReader reader = new BufferedReader(new FileReader(file));
      try
      {
        properties = new Properties();
        properties.load(reader);
        Log.debug("Mailer.Mailer: properties=%s", properties);
      }
      finally
      {
        reader.close();
      }
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

  public void send(Collection<String> toAddresses, Collection<String> ccAddresses, String subject, String text)
  {
    try
    {
      InternetAddress[] toInternetAddresses = getInternetAddresses(toAddresses);
      InternetAddress[] ccInternetAddresses = getInternetAddresses(ccAddresses);

      Session session = Session.getInstance(properties, null);

      MimeMessage mimeMessage = new MimeMessage(session);
      mimeMessage.setRecipients(Message.RecipientType.TO, toInternetAddresses);
      mimeMessage.setRecipients(Message.RecipientType.CC, ccInternetAddresses);
      mimeMessage.setSubject(subject);
      mimeMessage.setText(text, "utf-8");

      Transport.send(mimeMessage);
    }
    catch (MessagingException e)
    {
      if (Boolean.parseBoolean((String)properties.get("mailer.exception.log")))
      {
        writeMailExceptionToLog(e);
      }
      if (Boolean.parseBoolean((String)properties.get("mailer.exception.rethrow")))
      {
        throw new RuntimeException(e);
      }
    }
  }

  private InternetAddress[] getInternetAddresses(Collection<String> addresses) throws AddressException
  {
    InternetAddress[] internetAddresses = null;
    if (addresses != null)
    {
      int addressOffset = 0;
      int addressCount = addresses.size();
      internetAddresses = new InternetAddress[addressCount];
      for (String address : addresses)
      {
        InternetAddress internetAddress = new InternetAddress(address);
        internetAddresses[addressOffset] = internetAddress;
        addressOffset++;
      }
    }
    return internetAddresses;
  }

  private void writeMailExceptionToLog(Exception e)
  {
    do
    {
      Log.error("Mailer.writeMailExceptionToLog: an exception occurred sending mail, e=%s", e.toString());
      if (e instanceof SendFailedException)
      {
        SendFailedException sendFailedException = (SendFailedException)e;
        Address[] invalidAddresses = sendFailedException.getInvalidAddresses();
        if (invalidAddresses != null)
        {
          for (int i = 0; i < invalidAddresses.length; i++)
          {
            Log.error("Mailer.writeMailExceptionToLog: invalidAddress=%s", invalidAddresses[i]);
          }
        }
        Address[] validUnsent = sendFailedException.getValidUnsentAddresses();
        if (validUnsent != null)
        {
          for (int i = 0; i < validUnsent.length; i++)
          {
            Log.error("Mailer.writeMailExceptionToLog: validUnsent=%s", validUnsent[i]);
          }
        }
        Address[] validSent = sendFailedException.getValidSentAddresses();
        if (validSent != null)
        {
          for (int i = 0; i < validSent.length; i++)
          {
            Log.error("Mailer.writeMailExceptionToLog: validSent=%s", validSent[i]);
          }
        }
      }
    }
    while ((e instanceof MessagingException) && (e = ((MessagingException)e).getNextException()) != null);
  }

}