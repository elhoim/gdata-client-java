/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package sample.appsforyourdomain.gmailsettings.gui;

import sample.appsforyourdomain.gmailsettings.Defaults;
import sample.appsforyourdomain.gmailsettings.InvalidUserException;
import com.google.gdata.util.ServiceException;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 * Tab containing all the send as information.
 */
public class TabSendAs extends Tab {

  protected JTextField name;
  protected JTextField address;
  protected JTextField replyTo;
  protected JButton submit;

 /**
  * Setup all the components on the tab.
  */
  public TabSendAs() {
    super("Send As", "");
    name = new JTextField(Defaults.SEND_AS_NAME, 25);
    address = new JTextField(Defaults.SEND_AS_ADDRESS, 25);
    replyTo = new JTextField(Defaults.SEND_AS_SENDAS, 25);

    submit = new JButton("Set Send As");
    submit.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if (GmailSettingsClient.settings == null) {
            JOptionPane.showMessageDialog(null, GmailSettingsClient.ERROR_AUTHENTICATION_REQUIRED,
                GmailSettingsClient.APP_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
          }

          try {
            GmailSettingsClient.settings.createSendAs(GmailSettingsClient.users.
                getSelectedUsers(), name.getText(), address.getText(), replyTo.getText());
          } catch (InvalidUserException e) {
             JOptionPane.showMessageDialog(null, e, GmailSettingsClient.APP_TITLE,
                 JOptionPane.ERROR_MESSAGE);
          } catch (ServiceException e) {
             JOptionPane.showMessageDialog(null, e, GmailSettingsClient.APP_TITLE,
                 JOptionPane.ERROR_MESSAGE);
          } catch (MalformedURLException e) {
             JOptionPane.showMessageDialog(null, e, GmailSettingsClient.APP_TITLE,
                 JOptionPane.ERROR_MESSAGE);
          } catch (IOException e) {
             JOptionPane.showMessageDialog(null, e, GmailSettingsClient.APP_TITLE,
                 JOptionPane.ERROR_MESSAGE);
          } 
        }
      });

    setLayout(new FlowLayout());

    add(new JLabel("Name:"));
    add(name);
    add(new JLabel("Address:"));
    add(address);
    add(new JLabel("Send As:"));
    add(replyTo);
    add(submit);
  }
}
