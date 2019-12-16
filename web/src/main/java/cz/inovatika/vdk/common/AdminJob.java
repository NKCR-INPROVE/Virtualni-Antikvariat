/*
 * Copyright (C) 2013-2015 Alberto Hernandez
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.inovatika.vdk.common;

import cz.inovatika.vdk.Options;
import cz.inovatika.vdk.UsersController;
import java.io.IOException;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author alberto
 */
public class AdminJob {

  Logger LOGGER = Logger.getLogger(AdminJob.class.getName());

  private final Options opts;
  private final VDKJobData jobData;
  String configFile;

  public AdminJob(VDKJobData jobData) throws Exception {
    this.jobData = jobData;
    this.configFile = jobData.getConfigFile();
    opts = Options.getInstance();
    init();
  }

  private void init() {

  }

  public void run() {
    checkOffers();
  }

  // Check expiration date offer.
  // When expires archive them and send emails
  private void checkOffers() {
    Calendar now = Calendar.getInstance();
    Calendar o = Calendar.getInstance();
    Options opts = Options.getInstance();
    Map<String, String> mails = new HashMap<>();
    String kn;
    String email;

    int days = opts.getInt("expirationDays", 7) * 3;

    //expired. Should archive and send emails
    SolrQuery query = new SolrQuery("archived:false");
    query.addFilterQuery("created:[NOW/DAY-" + days + "DAYS TO NOW]");
    try (SolrClient client = new HttpSolrClient.Builder(opts.getString("solrHost")).build()) {
      SolrDocumentList docs = client.query(opts.getString("offersCore"), query).getResults();
      for (SolrDocument doc : docs) {
        kn = (String) doc.getFirstValue("knihovna");
        email = mails.get(kn);
        if (email == null) {
          email = UsersController.getUser(kn).email;
          mails.put(kn, email);
        }

        sendMail(email, (String) doc.getFirstValue("nazev"), (String) doc.getFirstValue("id"));

        // Find receivers and send mails
        query = new SolrQuery("offer_id:" + doc.getFirstValue("id"));
        SolrDocumentList docs2 = client.query(opts.getString("offersCore"), query).getResults();
        for (SolrDocument doc2 : docs2) {
          kn = (String) doc2.getFirstValue("chci");
          email = mails.get(kn);
          if (email == null) {
            email = UsersController.getUser(kn).email;
            mails.put(kn, email);
          }
          sendMail(email, (String) doc.getFirstValue("nazev"), (String) doc.getFirstValue("id"));
        }

        // Archive 
        // set archive true
      }
    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, "Error checking offers");
      LOGGER.log(Level.SEVERE, null, ex);

    }
  }

  private void sendMail(String to, String offerName, String offerId) {
    String from = opts.getString("admin.email");
    try {
      Properties properties = System.getProperties();
      Session session = Session.getDefaultInstance(properties);

      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));
      message.addRecipient(Message.RecipientType.TO,
              new InternetAddress(to));

      message.setSubject(opts.getString("admin.email.offer.subject"));

      String link = opts.getString("app.url") + "/reports/protocol.vm?id=" + offerId;
      String body = opts.getString("admin.email.offer.body")
              .replace("${offer.nazev}", offerName)
              .replace("${offer.report}", link);
      message.setText(body);

      Transport.send(message);
      LOGGER.fine("Sent message successfully....");
    } catch (MessagingException ex) {
      LOGGER.log(Level.SEVERE, "Error sending email to: {0}, from {1} ", new Object[]{to, from});
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }
}
