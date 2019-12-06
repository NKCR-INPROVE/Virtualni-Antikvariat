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
package cz.inovatika.vdk.solr;

import cz.inovatika.vdk.InitServlet;
import cz.inovatika.vdk.Options;
import cz.inovatika.vdk.common.Bohemika;
import cz.inovatika.vdk.common.DbUtils;
import cz.inovatika.vdk.solr.models.User;
import cz.inovatika.vdk.common.SolrIndexerCommiter;
import cz.inovatika.vdk.common.VDKJobData;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.json.JSONObject;
import org.w3c.dom.Document;

/**
 *
 * @author alberto
 */
public class Indexer {

  static final Logger LOGGER = Logger.getLogger(Indexer.class.getName());

  int total = 0;
  int offerIndexed = 0;
  int demandIndexed = 0;
  Map<String, SolrDocument> demandsCache;
  String errorMsg = "";

  private final String LAST_UPDATE = "last_run";
  private final String LAST_MESSAGE = "last_message";
  private String statusFileName;
  JSONObject statusJson = new JSONObject();

  private final Options opts;
  private final VDKJobData jobData;
  String configFile;
  Transformer transformer;
  Transformer trId;

  SimpleDateFormat sdf;
  SolrClient server;

  public Indexer() throws Exception {
    this.jobData = null;
    this.configFile = null;
    opts = Options.getInstance();
    init();
  }

  public Indexer(VDKJobData jobData) throws Exception {
    this.jobData = jobData;
    this.configFile = jobData.getConfigFile();
    opts = Options.getInstance();
    init();
  }

  public Indexer(String configFile) throws Exception {
    this.configFile = configFile;
    this.jobData = new VDKJobData(configFile, new JSONObject());
    this.jobData.load();
    opts = Options.getInstance();
    init();

  }

  private void init() throws Exception {
    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    TransformerFactory tfactory = TransformerFactory.newInstance();
    StreamSource xslt;
    File f = new File(InitServlet.CONFIG_DIR + File.separator + opts.getString("indexerXSL", "vdk_md5.xsl"));
    if (f.exists()) {
      xslt = new StreamSource(f);
    } else {
      xslt = new StreamSource(Options.class.getResourceAsStream("/cz/inovatika/vdk/vdk_md5.xsl"));
    }
    transformer = tfactory.newTransformer(xslt);
    if (transformer == null) {
      LOGGER.log(Level.SEVERE, "File {0} not found", InitServlet.CONFIG_DIR + File.separator + opts.getString("indexerXSL", "vdk_md5.xsl"));
      throw new FileNotFoundException();
    }

    StreamSource xslt2;
    f = new File(InitServlet.CONFIG_DIR + File.separator + opts.getString("indexerIdXSL", "vdk_id.xsl"));
    if (f.exists()) {
      xslt2 = new StreamSource(f);
    } else {
      xslt2 = new StreamSource(Options.class.getResourceAsStream("/cz/inovatika/vdk/vdk_id.xsl"));
    }
    trId = tfactory.newTransformer(xslt2);
    if (trId == null) {
      LOGGER.log(Level.SEVERE, "File {0} not found", InitServlet.CONFIG_DIR + File.separator + opts.getString("indexerIdXSL", "vdk_id.xsl"));
      throw new FileNotFoundException();
    }
    if (this.jobData == null) {
      statusFileName = System.getProperty("user.home")
              + File.separator + ".vdkcr" + File.separator
              + File.separator + "jobs" + File.separator
              + File.separator + "status" + File.separator + "indexer.status";
    } else {
      statusFileName = jobData.getStatusFile();
    }
    server = SolrIndexerCommiter.getServer();
  }

  public void clean() throws Exception {
    LOGGER.log(Level.FINE, "Cleaning index...");
    SolrIndexerCommiter.getServer().deleteByQuery("*:*");
    LOGGER.log(Level.INFO, "Index cleaned");
  }

  public JSONObject reindex() throws SolrServerException, IOException {
    JSONObject json = new JSONObject();
    try {
      jobData.getOpts().put("full_index", true);
      clean();
      json.put("index", index());
      indexAllOffers();
      indexAllDemands();
      indexAllWanted();

    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      json.put("error", ex);
      SolrIndexerCommiter.closeClients();
      //throw new Exception(ex);
    }
    return json;
  }

  private SolrInputDocument demandDoc(
          String id,
          String knihovna,
          String docCode,
          String zaznam,
          String exemplar,
          String update) {

    JSONObject j = new JSONObject();
    j.put("id", id);
    j.put("knihovna", knihovna);
    j.put("doc_code", docCode);
    j.put("zaznam", zaznam);
    j.put("exemplar", exemplar);

    SolrInputDocument doc = new SolrInputDocument();
    doc.addField("code", docCode);
    doc.addField("md5", docCode);
    addField(doc, "poptavka", knihovna, update);
    addField(doc, "poptavka_ext", j.toString(), update);

    return doc;
  }

  public void indexAllDemands() throws Exception {
    indexDemands("*");
  }

  private void indexDemands(String q) throws Exception {

    try (SolrClient client = new HttpSolrClient.Builder(String.format("%s/%s/",
            opts.getString("solrHost", "http://localhost:8983/solr"),
            opts.getString("demandsCore", "demands")))
            .build()) {

      List<SolrInputDocument> idocs = new ArrayList<>();
      SolrQuery query = (new SolrQuery(q)).setRows(1000).setSort(SortClause.asc("id"));
      String cursorMark = CursorMarkParams.CURSOR_MARK_START;
      boolean done = false;
      while (!done) {

        if (jobData.isInterrupted()) {
          LOGGER.log(Level.INFO, "INDEXER INTERRUPTED");
          break;
        }
        query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
        QueryResponse rsp = client.query(query);
        String nextCursorMark = rsp.getNextCursorMark();
        for (SolrDocument doc : rsp.getResults()) {
          idocs.add(demandDoc(
                  (String) doc.getFirstValue("id"),
                  (String) doc.getFirstValue("knihovna"),
                  (String) doc.getFirstValue("doc_code"),
                  (String) doc.getFirstValue("zaznam"),
                  (String) doc.getFirstValue("exemplar"),
                  "add"));
          demandIndexed++;
        }
        if (cursorMark.equals(nextCursorMark)) {
          done = true;
        }
        cursorMark = nextCursorMark;
        if (!idocs.isEmpty()) {
          server.add(idocs);
          idocs.clear();
          writeStatus();
        }
      }
      if (!idocs.isEmpty()) {
        server.add(idocs);
        idocs.clear();
      }

    }
    server.commit();
  }

  public void indexWanted(int wanted_id) throws Exception {
    Connection conn = DbUtils.getConnection();
    String sql = "select w.wants, zo.knihovna, k.code, zo.uniquecode from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
            + "where w.wanted_id=? "
            + "and w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer";
    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setInt(1, wanted_id);

    try (ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        String uniquecode = rs.getString("uniquecode");
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("code", uniquecode);
        doc.addField("md5", uniquecode);

        if (rs.getBoolean(1)) {
          addField(doc, "chci", rs.getString("code"), "add");
        } else {
          addField(doc, "nechci", rs.getString("code"), "add");
        }
        server.add(doc);
      }
    }
    server.commit();
  }

  public void indexWanted(String code, String knihovna, boolean wanted) throws Exception {

    SolrInputDocument doc = new SolrInputDocument();
    doc.addField("code", code);

    if (wanted) {
      addField(doc, "chci", knihovna, "add");
    } else {
      addField(doc, "nechci", knihovna, "add");
    }
    server.add(doc);

    server.commit();
  }

  public void indexOfferDb(int id) throws Exception {
    LOGGER.log(Level.INFO, "indexing offer {0}", id);
    Connection conn = DbUtils.getConnection();

    String sql = "SELECT offer.datum, ZaznamOffer.zaznamoffer_id, ZaznamOffer.offer, "
            + "ZaznamOffer.knihovna, ZaznamOffer.pr_knihovna, "
            + "ZaznamOffer.uniqueCode, ZaznamOffer.zaznam, ZaznamOffer.exemplar, ZaznamOffer.fields "
            + "FROM zaznamOffer, offer where offer.offer_id=zaznamOffer.offer and zaznamOffer.offer=?";
    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setInt(1, id);

    try (ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
//        server.add(offerDoc(rs.getInt("offer"),
//                rs.getDate("datum"),
//                rs.getString("uniquecode"),
//                rs.getInt("zaznamoffer_id"),
//                rs.getString("zaznam"),
//                rs.getString("knihovna"),
//                rs.getString("pr_knihovna"),
//                rs.getString("exemplar"),
//                rs.getString("fields")));

        offerIndexed++;

      }
    }
    server.commit();
    offerIndexed++;
    //SolrIndexerCommiter.postData("<commit/>");
  }

  public void removeAllWanted() throws Exception {
    SolrQuery query = new SolrQuery("chci:[* TO *]");
    query.addField("code");
    SolrDocumentList docs = IndexerQuery.query(query);
    long numFound = docs.getNumFound();
    Iterator<SolrDocument> iter = docs.iterator();
    while (iter.hasNext()) {
      if (jobData.isInterrupted()) {
        LOGGER.log(Level.INFO, "INDEXER INTERRUPTED");
        break;
      }
      StringBuilder sb = new StringBuilder();

      SolrDocument resultDoc = iter.next();
      String docCode = (String) resultDoc.getFieldValue("code");
      sb.append("<add><doc>");
      sb.append("<field name=\"code\">")
              .append(docCode)
              .append("</field>");
      sb.append("<field name=\"md5\">")
              .append(docCode)
              .append("</field>");

      sb.append("<field name=\"chci\" update=\"set\" null=\"true\" />");
      sb.append("</doc></add>");
      SolrIndexerCommiter.postData(sb.toString());
      SolrIndexerCommiter.postData("<commit/>");
    }
    query.setQuery("nechci:[* TO *]");
    query.addField("code");
    docs = IndexerQuery.query(query);
    iter = docs.iterator();
    while (iter.hasNext()) {
      if (jobData.isInterrupted()) {
        LOGGER.log(Level.INFO, "INDEXER INTERRUPTED");
        break;
      }
      StringBuilder sb = new StringBuilder();

      SolrDocument resultDoc = iter.next();
      String docCode = (String) resultDoc.getFieldValue("code");
      sb.append("<add><doc>");
      sb.append("<field name=\"code\">")
              .append(docCode)
              .append("</field>");
      sb.append("<field name=\"md5\">")
              .append(docCode)
              .append("</field>");

      sb.append("<field name=\"nechci\" update=\"set\" null=\"true\" />");
      sb.append("</doc></add>");
      SolrIndexerCommiter.postData(sb.toString());
      SolrIndexerCommiter.postData("<commit/>");
    }

    numFound += docs.getNumFound();
    if (numFound > 0 && !jobData.isInterrupted()) {
      removeAllWanted();
    }
  }

  public void removeAllOffers() throws Exception {
    SolrQuery query = new SolrQuery("nabidka:[* TO *]");
    query.addField("code");
    SolrDocumentList docs = IndexerQuery.query(query);
    Iterator<SolrDocument> iter = docs.iterator();
    while (iter.hasNext()) {
      if (jobData.isInterrupted()) {
        LOGGER.log(Level.INFO, "INDEXER INTERRUPTED");
        break;
      }
      StringBuilder sb = new StringBuilder();

      SolrDocument resultDoc = iter.next();
      String docCode = (String) resultDoc.getFieldValue("code");
      sb.append("<add><doc>");
      sb.append("<field name=\"code\">")
              .append(docCode)
              .append("</field>");
      sb.append("<field name=\"md5\">")
              .append(docCode)
              .append("</field>");

      sb.append("<field name=\"nabidka\" update=\"set\" null=\"true\" />");
      sb.append("<field name=\"nabidka_ext\" update=\"set\" null=\"true\" />");
      sb.append("<field name=\"nabidka_datum\" update=\"set\" null=\"true\" />");
      sb.append("</doc></add>");
      SolrIndexerCommiter.postData(sb.toString());
      SolrIndexerCommiter.postData("<commit/>");
    }

    long numFound = docs.getNumFound();
    if (numFound > 0 && !jobData.isInterrupted()) {
      removeAllOffers();
    }
  }

  public void indexDemand(
          String id,
          String knihovna,
          String docCode,
          String zaznam,
          String exemplar) throws Exception {

    server.add(demandDoc(
            id,
            knihovna,
            docCode,
            zaznam,
            exemplar,
            "add"));
    demandIndexed++;
    server.commit();
  }

  public void removeAllDemands() throws Exception {
    SolrQuery query = new SolrQuery("poptavka:[* TO *]");
    query.addField("code");
    query.setRows(1000);
    SolrDocumentList docs = IndexerQuery.query(query);
    long numFound = docs.getNumFound();
    Iterator<SolrDocument> iter = docs.iterator();
    while (iter.hasNext()) {
      if (jobData.isInterrupted()) {
        LOGGER.log(Level.INFO, "INDEXER INTERRUPTED");
        break;
      }
      StringBuilder sb = new StringBuilder();

      SolrDocument resultDoc = iter.next();
      String docCode = (String) resultDoc.getFieldValue("code");
      sb.append("<add><doc>");
      sb.append("<field name=\"code\">")
              .append(docCode)
              .append("</field>");
      sb.append("<field name=\"md5\">")
              .append(docCode)
              .append("</field>");

      sb.append("<field name=\"poptavka\" update=\"set\" null=\"true\" />");
      sb.append("<field name=\"poptavka_ext\" update=\"set\" null=\"true\" />");
      sb.append("</doc></add>");
      SolrIndexerCommiter.postData(sb.toString());
      SolrIndexerCommiter.postData("<commit/>");
      LOGGER.log(Level.INFO, "Demands for {0} removed.", docCode);
    }
    if (numFound > 0 && !jobData.isInterrupted()) {
      removeAllDemands();
    }
  }

  public void removeDemand(
          String id,
          String knihovna,
          String docCode,
          String zaznam,
          String exemplar) throws Exception {

    server.add(demandDoc(
            id,
            knihovna,
            docCode,
            zaznam,
            exemplar,
            "remove"));
    server.commit();
  }

  private void removeWanted(String knihovna, String code) throws Exception {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField("code", code);
    doc.addField("md5", code);
    addField(doc, "chci", knihovna, "remove");
    addField(doc, "nechci", knihovna, "remove");

    server.add(doc);
    server.commit();
  }

  public void indexAllWanted() throws Exception {
    Connection conn = DbUtils.getConnection();
    String sql = "select w.wants, zo.knihovna, k.code, zo.uniquecode from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
            + "where w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer";
    PreparedStatement ps = conn.prepareStatement(sql);

    try (ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        if (jobData.isInterrupted()) {
          LOGGER.log(Level.INFO, "INDEXER INTERRUPTED");
          break;
        }

        SolrInputDocument doc = new SolrInputDocument();
        String uniquecode = rs.getString("uniquecode");
        doc.addField("code", uniquecode);
        doc.addField("md5", uniquecode);

        if (rs.getBoolean(1)) {
          addField(doc, "chci", rs.getString("code"), "add");
        } else {
          addField(doc, "nechci", rs.getString("code"), "add");
        }
        server.add(doc);

      }
    }
    server.commit();
  }

  public void removeOffer(int id) throws Exception {
    Connection conn = DbUtils.getConnection();
    String sql = "SELECT ZaznamOffer.uniqueCode, ZaznamOffer.zaznam, ZaznamOffer.exemplar, "
            + "ZaznamOffer.fields, offer.datum "
            + "FROM zaznamOffer, Offer "
            + "where ZaznamOffer.offer=Offer.offer_id AND ZaznamOffer.offer=?";
    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setInt(1, id);

    ResultSet rs = ps.executeQuery();

    StringBuilder sb = new StringBuilder();
    sb.append("<add>");
    while (rs.next()) {
      String docCode = rs.getString("uniqueCode");
      String datum = sdf.format(rs.getDate("datum"));
      sb.append("<doc>");
      sb.append("<field name=\"code\">")
              .append(docCode)
              .append("</field>");
      sb.append("<field name=\"md5\">")
              .append(docCode)
              .append("</field>");
      sb.append("<field name=\"nabidka\" update=\"remove\">")
              .append(id)
              .append("</field>");
      sb.append("<field name=\"nabidka_datum\" update=\"remove\">")
              .append(datum)
              .append("</field>");
      JSONObject nabidka_ext = new JSONObject();
      JSONObject nabidka_ext_n = new JSONObject();
      nabidka_ext_n.put("zaznam", rs.getString("zaznam"));
      nabidka_ext_n.put("ex", rs.getString("exemplar"));
      nabidka_ext_n.put("fields", rs.getString("fields"));
      nabidka_ext.put("" + id, nabidka_ext_n);
      sb.append("<field name=\"nabidka_ext\" update=\"remove\">")
              .append(nabidka_ext)
              .append("</field>");

      sb.append("</doc>");
    }
    rs.close();
    sb.append("</add>");
    SolrIndexerCommiter.postData(sb.toString());
    SolrIndexerCommiter.postData("<commit/>");
  }

  private void addField(SolrInputDocument doc, String name, Object value, String modifier) {
    Map<String, Object> map = new HashMap();
    map.put(modifier, value);
    doc.addField(name, map);
  }

  private SolrInputDocument offerDoc(
          String offerid,
          String datum,
          String docCode,
          String zaznamoffer_id,
          String zaznam,
          String knihovna,
          Collection<Object> chci,
          String exemplar,
          String fields) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField("code", docCode);
    doc.addField("md5", docCode);

    addField(doc, "nabidka", offerid, "add");
    addField(doc, "nabidka_datum", datum, "add");

    JSONObject nabidka_ext_n = new JSONObject();
    nabidka_ext_n.put("id", zaznamoffer_id);
    nabidka_ext_n.put("offer_id", offerid);
    nabidka_ext_n.put("doc_code", docCode);
    nabidka_ext_n.put("zaznam", zaznam);
    nabidka_ext_n.put("knihovna", knihovna);
    // nabidka_ext_n.put("pr_knihovna", pr_knihovna);
    nabidka_ext_n.put("ex", exemplar);
    nabidka_ext_n.put("datum", datum);
    if (fields != null) {
      nabidka_ext_n.put("fields", new JSONObject(fields));
    }
    if (fields != null) {
      nabidka_ext_n.put("chci", new JSONObject(fields));
    }

    addField(doc, "nabidka_ext", nabidka_ext_n.toString(), "add");

    if (chci != null) {
      addField(doc, "chci", chci.toArray(), "add");
    }

    return doc;
  }

  public void indexOffer(String id) throws Exception {
    indexOffers("offer_id:" + id);
  }

  public void indexDocOffers(String uniqueCode) throws Exception {
    removeDocOffers(uniqueCode);
    indexOffers("doc_code:" + uniqueCode);
  }

  public void indexAllOffers() throws Exception {
    indexOffers("*");
  }

  private void indexOffers(String q) throws Exception {

    try (SolrClient client = new HttpSolrClient.Builder(String.format("%s/%s/",
            opts.getString("solrHost", "http://localhost:8983/solr"),
            opts.getString("offersCore", "offers")))
            .build()) {

      List<SolrInputDocument> idocs = new ArrayList<>();
      SolrQuery query = (new SolrQuery(q)).setRows(1000).addFilterQuery("content_type:doc").setSort(SortClause.asc("id"));
      String cursorMark = CursorMarkParams.CURSOR_MARK_START;
      boolean done = false;
      while (!done) {

        if (jobData != null && jobData.isInterrupted()) {
          LOGGER.log(Level.INFO, "INDEXER INTERRUPTED");
          break;
        }
        query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
        QueryResponse rsp = client.query(query);
        String nextCursorMark = rsp.getNextCursorMark();
        for (SolrDocument doc : rsp.getResults()) {
          idocs.add(offerDoc((String) doc.getFirstValue("offer_id"),
                  (String) doc.getFirstValue("datum"),
                  (String) doc.getFirstValue("doc_code"),
                  (String) doc.getFirstValue("id"),
                  (String) doc.getFirstValue("zaznam"),
                  (String) doc.getFirstValue("knihovna"),
                  doc.getFieldValues("chci"),
                  (String) doc.getFirstValue("exemplar"),
                  (String) doc.getFirstValue("fields")));
          offerIndexed++;
        }
        if (cursorMark.equals(nextCursorMark)) {
          done = true;
        }
        cursorMark = nextCursorMark;
        if (!idocs.isEmpty()) {
          server.add(idocs);
          idocs.clear();
          writeStatus();
        }
      }
      if (!idocs.isEmpty()) {
        server.add(idocs);
        idocs.clear();
      }

    }
    server.commit();
  }

  public void reindexDocByIdentifier(String identifier) throws Exception {
    LOGGER.log(Level.INFO, "----- Reindexing doc {0} ...", identifier);

    SolrQuery query = new SolrQuery("id:\"" + identifier + "\"");
    query.addField("id,code");
    query.setRows(1000);
    SolrDocumentList docs = IndexerQuery.query(opts.getString("solrIdCore", "vdk_id"), query);
    Iterator<SolrDocument> iter = docs.iterator();
    while (iter.hasNext()) {
      SolrDocument resultDoc = iter.next();
      String uniqueCode = (String) resultDoc.getFieldValue("code");
      reindexDoc(uniqueCode, identifier);
    }
  }

  public void reindexDoc(String uniqueCode, String identifier) throws Exception {

    String oldUniqueCode;
    SolrQuery query = new SolrQuery("id:\"" + identifier + "\"");
    query.addField("id,code");
    query.setRows(1000);
    SolrDocumentList docs = IndexerQuery.query(query);
    Iterator<SolrDocument> iter = docs.iterator();
    while (iter.hasNext()) {
      SolrDocument resultDoc = iter.next();
      oldUniqueCode = (String) resultDoc.getFieldValue("code");

      if (oldUniqueCode != null && !oldUniqueCode.equals(uniqueCode)) {
        LOGGER.log(Level.INFO, "Cleaning doc {0} from index...", oldUniqueCode);
        String s = "<delete><query>code:" + oldUniqueCode + "</query></delete>";
        SolrIndexerCommiter.postData(s);
        indexDoc(oldUniqueCode);
        SolrIndexerCommiter.postData("<commit/>");
      }
    }

    LOGGER.log(Level.INFO, "Cleaning doc {0} from index...", uniqueCode);
    String s = "<delete><query>code:" + uniqueCode + "</query></delete>";
    SolrIndexerCommiter.postData(s);
    SolrIndexerCommiter.postData("<commit/>");

    indexDoc(uniqueCode);
  }

  public void removeDocOffers(String uniqueCode) throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("<add><doc>");
    sb.append("<field name=\"code\">")
            .append(uniqueCode)
            .append("</field>");
    sb.append("<field name=\"md5\">")
            .append(uniqueCode)
            .append("</field>");

    sb.append("<field name=\"nabidka\" update=\"set\" null=\"true\" />");
    sb.append("<field name=\"nabidka_ext\" update=\"set\" null=\"true\" />");
    sb.append("<field name=\"nabidka_datum\" update=\"set\" null=\"true\" />");
    sb.append("<field name=\"chci\" update=\"set\" null=\"true\" />");
    sb.append("<field name=\"nechci\" update=\"set\" null=\"true\" />");
    sb.append("</doc></add>");

    SolrIndexerCommiter.postData(sb.toString());
    SolrIndexerCommiter.postData("<commit/>");
  }

  public void indexDocOffersDb(String uniqueCode) throws NamingException, SQLException, IOException, SolrServerException, Exception {
    Connection conn = DbUtils.getConnection();
    try {
      String sql = "SELECT offer,datum, ZaznamOffer.zaznamoffer_id, ZaznamOffer.offer, "
              + "ZaznamOffer.uniqueCode, ZaznamOffer.zaznam, ZaznamOffer.exemplar, "
              + "ZaznamOffer.fields, ZaznamOffer.knihovna, ZaznamOffer.pr_knihovna, ZaznamOffer.pr_timestamp "
              + "FROM ZaznamOffer "
              + "JOIN offer ON offer.offer_id=ZaznamOffer.offer where offer.closed=? and ZaznamOffer.uniquecode=?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setBoolean(1, true);
      ps.setString(2, uniqueCode);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          if (jobData.isInterrupted()) {
            LOGGER.log(Level.INFO, "INDEXER INTERRUPTED");
            break;
          }
//          server.add(offerDoc(rs.getInt("offer"),
//                  rs.getDate("datum"),
//                  rs.getString("uniquecode"),
//                  rs.getInt("zaznamoffer_id"),
//                  rs.getString("zaznam"),
//                  rs.getString("knihovna"),
//                  rs.getString("pr_knihovna"),
//                  rs.getString("exemplar"),
//                  rs.getString("fields")));

          offerIndexed++;

        }
      }
      server.commit();
    } finally {
      if (conn != null && !conn.isClosed()) {
        conn.close();
      }
    }
  }

  public void indexDoc(String uniqueCode) throws Exception {

    try {
      LOGGER.log(Level.INFO, "Indexace doc {0}...", uniqueCode);
      StringBuilder sb = new StringBuilder();
      sb.append("<add>");

      SolrQuery query = new SolrQuery("code:\"" + uniqueCode + "\"");
      query.addField("id,code,code_type,xml,bohemika");
      query.setRows(1000);
      SolrDocumentList docs = IndexerQuery.query(opts.getString("solrIdCore", "vdk_id"), query);
      Iterator<SolrDocument> iter = docs.iterator();
      while (iter.hasNext()) {
        if (jobData.isInterrupted()) {
          LOGGER.log(Level.INFO, "INDEXER INTERRUPTED");
          break;
        }
        SolrDocument resultDoc = iter.next();

        boolean bohemika = false;
        if (resultDoc.getFieldValue("bohemika") != null) {
          bohemika = (Boolean) resultDoc.getFieldValue("bohemika");
        } else {
          bohemika = Bohemika.isBohemika((String) resultDoc.getFieldValue("xml"));
        }

        sb.append(transformXML((String) resultDoc.getFieldValue("xml"),
                uniqueCode,
                (String) resultDoc.getFieldValue("code_type"),
                (String) resultDoc.getFieldValue("id"),
                bohemika));

        total++;

      }
      sb.append("</add>");
      SolrIndexerCommiter.postData(sb.toString());
      removeDocOffers(uniqueCode);
      indexDocOffers(uniqueCode);
      SolrIndexerCommiter.postData("<commit/>");
      LOGGER.log(Level.INFO, "REINDEX FINISHED. Total docs: {0}", total);
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Error in reindex", ex);
    }
  }

  public void removeDoc(String identifier) throws Exception {
    String url = String.format("%s/%s/update",
            opts.getString("solrHost", "http://localhost:8983/solr"),
            opts.getString("solrIdCore", "vdk_id"));
    SolrIndexerCommiter.postData("<delete><id>" + identifier + "</id></delete>");
    SolrIndexerCommiter.postData("<commit/>");
  }

  private boolean isInDemandsCache(String code) {
    if (demandsCache == null) {
      try {
        SolrQuery query = new SolrQuery("poptavka_ext:[* TO *]");
        query.addField("id,doc_code,poptavka_ext,title");
        query.setRows(1000);
        demandsCache = new HashMap<>();
        SolrDocumentList docs = IndexerQuery.query(query);
        Iterator<SolrDocument> iter = docs.iterator();
        while (iter.hasNext()) {
          SolrDocument doc = iter.next();
          demandsCache.put((String) doc.getFieldValue("doc_code"), doc);

        }
      } catch (SolrServerException | IOException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
      }
    }
    return demandsCache.containsKey(code);
  }

  public void store(String id, String code, String codeType, boolean bohemika, String xml) throws Exception {

    StringBuilder sb = new StringBuilder();
    try {
      LOGGER.log(Level.FINE, "Storing document " + id);
      sb.append("<add>");

      sb.append(doSorlXML(xml,
              code,
              codeType,
              id,
              bohemika));
      sb.append("</add>");

      SolrIndexerCommiter.indexXML(sb.toString(), opts.getString("solrIdCore", "vdk_id"));

//            String url = String.format("%s/%s/update",
//                    opts.getString("solrHost", "http://localhost:8983/solr"),
//                    opts.getString("solrIdCore", "vdk_id"));
//            SolrIndexerCommiter.postData(url, sb.toString());
      if (total % 1000 == 0) {
        SolrIndexerCommiter.indexXML("<commit/>", opts.getString("solrIdCore", "vdk_id"));
        //  SolrIndexerCommiter.postData(url, "<commit/>");
        //  logger.log(Level.INFO, "Current stored docs: {0}", total);
      }

      total++;
      checkDemand(code, id);
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Error storing doc with " + sb.toString(), ex);
    }
  }

  private void checkDemand(String code, String id) {
//        try {
    if (isInDemandsCache(code)) {
      sendDemandMail(demandsCache.get(code), id);
    }
//            SolrQuery query = new SolrQuery("code:\"" + code + "\"");
//            query.addFilterQuery("poptavka_ext:[* TO *]");
//            query.addField("id,code,poptavka_ext,title");
//            query.setRows(1000);
//            SolrDocumentList docs = IndexerQuery.query(query);
//            Iterator<SolrDocument> iter = docs.iterator();
//            while (iter.hasNext()) {
//                sendDemandMail(iter.next(), id);
//            }
//        } catch (SolrServerException | IOException ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
  }

  private void sendDemandMail(SolrDocument resultDoc, String id) {
    try {
      String title = (String) resultDoc.getFieldValues("title").toArray()[0];
      String pop = (String) resultDoc.getFieldValue("poptavka_ext");
      JSONObject j = new JSONObject(pop);

      String from = opts.getString("admin.email");
      User kn = User.byCode(j.getString("knihovna"));
      String to = kn.getEmail();
      String zaznam = j.optString("zaznam");
      String code = j.optString("code");
      String exemplar = j.optString("exemplar");
      try {
        Properties properties = System.getProperties();
        Session session = Session.getDefaultInstance(properties);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO,
                new InternetAddress(to));

        message.setSubject(opts.getString("admin.email.demand.subject"));

        String link = opts.getString("app.url") + "/original?id=" + id;
        String body = opts.getString("admin.email.demand.body")
                .replace("${demand.title}", title)
                .replace("${demand.url}", link);
        message.setText(body);

        Transport.send(message);
        LOGGER.fine("Sent message successfully....");
      } catch (MessagingException ex) {
        LOGGER.log(Level.SEVERE, "Error sending email to: {0}, from {1} ", new Object[]{to, from});
        LOGGER.log(Level.SEVERE, null, ex);
      }
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }

  private JSONObject index() throws Exception {

    JSONObject json = update(null);
    Date date = new Date();

    String to = sdf.format(date);

    statusJson.put(LAST_UPDATE, to);
    writeStatus();
    return json;
  }

  public void run() throws Exception {

    readStatus();
    if (jobData.getBoolean("full_index", false)) {
      reindex();
    } else {
      if (statusJson.has(LAST_UPDATE)) {
        update(statusJson.getString(LAST_UPDATE));
      } else {
        update(null);
      }
      if (jobData.getBoolean("reindex_offers", false)) {
        removeAllOffers();
        indexAllOffers();
      }
      if (jobData.getBoolean("reindex_demands", false)) {
        removeAllDemands();
        indexAllDemands();
      }
      if (jobData.getBoolean("reindex_wanted", false)) {
        removeAllWanted();
        indexAllWanted();
      }
      if (!"".equals(jobData.getString("identifier", ""))) {
        LOGGER.log(Level.INFO, "----- Reindexing doc {0} ...", jobData.getString("identifier"));

        SolrQuery query = new SolrQuery("id:\"" + jobData.getString("identifier") + "\"");
        query.addField("id,code");
        query.setRows(1000);
        SolrDocumentList docs = IndexerQuery.query(opts.getString("solrIdCore", "vdk_id"), query);
        Iterator<SolrDocument> iter = docs.iterator();
        while (iter.hasNext()) {
          SolrDocument resultDoc = iter.next();
          String uniqueCode = (String) resultDoc.getFieldValue("code");
          reindexDoc(uniqueCode, jobData.getString("identifier"));
        }

      }
    }
    writeStatus();

  }

  private void readStatus() throws IOException {
    LOGGER.log(Level.INFO, "reading status file {0}", statusFileName);
    File statusFile = new File(statusFileName);
    if (statusFile.exists()) {
      statusJson = new JSONObject(FileUtils.readFileToString(statusFile, "UTF-8"));
    } else {
      statusJson = new JSONObject();
    }

  }

  private void writeStatus() throws FileNotFoundException, IOException {
    statusJson.put(LAST_MESSAGE,
            String.format("Docs indexed: %d\nOffers indexed: %d", total, offerIndexed));
    if (!errorMsg.equals("")) {
      statusJson.put(LAST_MESSAGE,
              statusJson.getString(LAST_MESSAGE) + String.format("\nError: %s", errorMsg));

    }
    File statusFile = new File(statusFileName);
    FileUtils.writeStringToFile(statusFile, statusJson.toString(), "UTF-8");

  }

  private JSONObject update(String fq) throws Exception {
    JSONObject json = new JSONObject();
    try {
      StorageBrowser docs = new StorageBrowser();
      docs.setWt("json");
      docs.setFl("id,code,code_type,bohemika,xml,timestamp");
      if (fq != null) {
        docs.setStart(fq);
      }
      Iterator it = docs.iterator();
      StringBuilder sb = new StringBuilder();
      sb.append("<add>");
      while (it.hasNext()) {
        if (jobData.isInterrupted()) {
          LOGGER.log(Level.INFO, "INDEXER INTERRUPTED");
          break;
        }
        JSONObject doc = (JSONObject) it.next();

        if (!jobData.getBoolean("full_index", false)) {
          reindexDoc(doc.getString("code"), doc.getString("id"));
          if (doc.has("timestamp")) {
            statusJson.put(LAST_UPDATE, doc.getString("timestamp"));
            writeStatus();
          } else {
            LOGGER.log(Level.INFO, "TIMESTAMP MISSING!!!!");

          }
        } else {
          boolean bohemika;
          if (doc.has("bohemika")) {
            bohemika = (Boolean) doc.getBoolean("bohemika");
          } else {
            bohemika = Bohemika.isBohemika(doc.getString("xml"));
          }

          sb.append(transformXML((String) doc.optString("xml", ""),
                  doc.getString("code"),
                  (String) doc.getString("code_type"),
                  (String) doc.getString("id"),
                  bohemika));
          if (!doc.optString("timestamp").equals("")) {
            statusJson.put(LAST_UPDATE, doc.getString("timestamp"));
          }
          if (total % 1000 == 0) {
            sb.append("</add>");
            SolrIndexerCommiter.postData(sb.toString());
            SolrIndexerCommiter.postData("<commit/>");
            sb = new StringBuilder();
            sb.append("<add>");
            LOGGER.log(Level.INFO, "Current indexed docs: {0}", total);
            writeStatus();
          }

          total++;
        }
      }
      sb.append("</add>");
      SolrIndexerCommiter.postData(sb.toString());

      SolrIndexerCommiter.postData("<commit/>");
      LOGGER.log(Level.INFO, "REINDEX FINISHED. Total docs: {0}", total);
      json.put("total", total);

      writeStatus();
    } catch (Exception ex) {
      errorMsg += ex.toString();
      json.put("error", errorMsg);
      LOGGER.log(Level.SEVERE, "Error in reindex", ex);
    }
    return json;
  }

  public void processXML(File file) throws Exception {
    LOGGER.log(Level.FINE, "Sending {0} to index ...", file.getAbsolutePath());
    StreamResult destStream = new StreamResult(new StringWriter());
    transformer.transform(new StreamSource(file), destStream);
    StringWriter sw = (StringWriter) destStream.getWriter();
    SolrIndexerCommiter.postData(sw.toString());
  }

  public void processXML(Document doc) throws Exception {
    LOGGER.log(Level.FINE, "Sending to index ...");
    StreamResult destStream = new StreamResult(new StringWriter());
    transformer.transform(new DOMSource(doc), destStream);
    StringWriter sw = (StringWriter) destStream.getWriter();
    SolrIndexerCommiter.postData(sw.toString());
  }

  private String doSorlXML(String xml, String uniqueCode, String codeType, String identifier, boolean bohemika) throws Exception {
    LOGGER.log(Level.FINE, "Transforming {0} ...", identifier);
    StreamResult destStream = new StreamResult(new StringWriter());
    trId.setParameter("uniqueCode", uniqueCode);
    trId.setParameter("codeType", codeType);
    trId.setParameter("bohemika", Boolean.toString(bohemika));
    trId.setParameter("sourceXml", xml);
    trId.transform(new StreamSource(new StringReader(xml)), destStream);
    StringWriter sw = (StringWriter) destStream.getWriter();
    return sw.toString();
  }

  private String transformXML(String xml, String uniqueCode, String codeType, String identifier, boolean bohemika) throws Exception {
    LOGGER.log(Level.FINE, "Transforming {0} ...", identifier);
    StreamResult destStream = new StreamResult(new StringWriter());
    transformer.setParameter("uniqueCode", uniqueCode);
    transformer.setParameter("codeType", codeType);
    transformer.setParameter("bohemika", Boolean.toString(bohemika));
    transformer.transform(new StreamSource(new StringReader(xml)), destStream);
    LOGGER.log(Level.FINE, "Sending to index ...");
    StringWriter sw = (StringWriter) destStream.getWriter();
    return sw.toString();
  }

  public void processXML(String xml, String uniqueCode, String codeType, String identifier, boolean bohemika) throws Exception {
    LOGGER.log(Level.FINE, "Transforming {0} ...", identifier);
    StreamResult destStream = new StreamResult(new StringWriter());
    transformer.setParameter("uniqueCode", uniqueCode);
    transformer.setParameter("bohemika", Boolean.toString(bohemika));
    transformer.transform(new StreamSource(new StringReader(xml)), destStream);
    LOGGER.log(Level.FINE, "Sending to index ...");
    StringWriter sw = (StringWriter) destStream.getWriter();
    SolrIndexerCommiter.postData(sw.toString());
  }

  public static void main(String[] args) throws SQLException {
    Connection conn = null;
    try {
//            org.postgresql.Driver dr;
//            conn = DbUtils.getConnection("org.postgresql.Driver",
//                    "jdbc:postgresql://localhost:5432/vdk",
//                    "vdk",
//                    "vdk");
      Indexer indexer = new Indexer();
      //indexer.reindex();
      indexer.removeAllDemands();
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }

}
