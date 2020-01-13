package cz.inovatika.vdk;

import au.com.bytecode.opencsv.CSVReader;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import cz.inovatika.vdk.common.Slouceni;
import cz.inovatika.vdk.common.SolrIndexerCommiter;
import cz.inovatika.vdk.solr.Indexer;
import cz.inovatika.vdk.solr.models.Offer;
import cz.inovatika.vdk.solr.models.OfferRecord;
import cz.inovatika.vdk.solr.models.User;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.util.NamedList;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
@WebServlet(name = "OffersServlet", urlPatterns = {"/offers/*"})
public class OffersServlet extends HttpServlet {

  public static final Logger LOGGER = Logger.getLogger(OffersServlet.class.getName());
  static boolean isLocalhost = false;

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
   * methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0
    response.setDateHeader("Expires", 0); // Proxies.
    PrintWriter out = response.getWriter();

    try {
      String actionNameParam = request.getPathInfo().substring(1).split("/")[0];
      if (actionNameParam != null) {
        Set<String> localAddresses = new HashSet<>();
        localAddresses.add(InetAddress.getLocalHost().getHostAddress());
        for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
          localAddresses.add(inetAddress.getHostAddress());
        }
        if (localAddresses.contains(request.getRemoteAddr())) {
          LOGGER.log(Level.FINE, "running from local address");
          isLocalhost = true;
        }

        Actions actionToDo = Actions.valueOf(actionNameParam.toUpperCase());
        if (UsersController.isLogged(request) || isLocalhost) {
          out.print(actionToDo.doPerform(request, response).toString(2));
        } else {
          JSONObject json = new JSONObject();
          json.put("error", "not logged");
          out.print(json.toString());
        }

      } else {
        out.print("actionNameParam -> " + actionNameParam);
      }
    } catch (IOException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
      out.print(e1.toString());
    } catch (SecurityException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    } catch (Exception e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
      out.print(e1.toString());
    }
  }

  public static void processStream(Reader reader, String idOffer, String knihovna, JSONObject json) throws Exception {
    try {
      CSVReader parser = new CSVReader(reader, '\t', '\"', false);
      String[] parts = parser.readNext();
      int lines = 0;
      while (parts != null) {
        if (!(parts.length == 1 && parts[0].equals(""))) {

          JSONObject slouceni = Slouceni.fromCSVStringArray(parts);

          //LOGGER.log(Level.INFO, slouceni.toString());
          OfferRecord or = new OfferRecord();
          or.offer_id = idOffer;
          or.doc_code = slouceni.getString("docCode");
          or.knihovna = knihovna;
          StringBuilder title = new StringBuilder();
          // concat(marc:datafield[@tag=245]/marc:subfield[@code='a'],marc:datafield[@tag=245]/marc:subfield[@code='b'])
          title.append(slouceni.optString("245a", ""))
                  .append(slouceni.optString("245b", ""));
          or.title = title.toString();
          or.fields = slouceni.toString();
          or.generateId();
          JSONObject ret = new JSONObject(SolrIndexerCommiter.indexJSON(new JSONObject(JSON.toJSONString(or)), "offersCore"));

          //insertNabidka(null, null, docCode, idOffer, slouceni.toString());
          lines++;
        }
        parts = parser.readNext();
      }
      json.put("message", "imported " + lines + " lines to offer: " + idOffer);
    } catch (IOException | JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      json.put("error", "Error processing file: " + ex.toString());
      //throw new Exception("Not valid csv file. Separator must be tabulator and line must be ", ex);
    }
  }

  enum Actions {
    ADD {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {

        JSONObject jo = new JSONObject();
        try {
          JSONObject json;
          if (req.getMethod().equals("POST")) {
            String js = IOUtils.toString(req.getInputStream(), "UTF-8");
            json = new JSONObject(js);
          } else {
            json = new JSONObject(req.getParameter("json"));
          }

          JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
          Offer offer = Offer.fromJSON(json);
          jo = new JSONObject(JSON.toJSONString(offer, SerializerFeature.WriteDateUseDateFormat));
          SolrIndexerCommiter
                  .indexJSON(jo, "offersCore");

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }
        return jo;

      }
    },
    REMOVE {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {

        JSONObject jo = new JSONObject();
        try {

          Options opts = Options.getInstance();
          try (HttpSolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build()) {
            client.deleteById(opts.getString("offersCore", "offers"), req.getParameter("id"));
            client.commit(opts.getString("offersCore", "offers"));

            client.deleteByQuery(opts.getString("offersCore", "offers"), "offer_id:" + req.getParameter("id"));
            client.commit(opts.getString("offersCore", "offers"));
            Indexer indexer = new Indexer();
            indexer.removeOffer(req.getParameter("id"));

            jo.put("msg", "removed");
          } catch (SolrServerException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            jo.put("error", ex.toString());
          }

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }
        return jo;

      }
    },
    ADDRECORD {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {

        JSONObject jo = new JSONObject();
        try {
          JSONObject json;
          if (req.getMethod().equals("POST")) {
            String js = IOUtils.toString(req.getInputStream(), "UTF-8");
            json = new JSONObject(js);
          } else {
            json = new JSONObject(req.getParameter("json"));
          }
          OfferRecord or = OfferRecord.fromJSON(json);
          if (or.doc_code == null) {
            JSONObject f = new JSONObject(or.fields);
            Map<String, Object> map = new HashMap<String, Object>();
            Iterator<String> keys = f.keys();
            while (keys.hasNext()) {
              String key = keys.next();
              Object value = f.get(key);
              map.put(key, value);
            }
            JSONObject slouceni = Slouceni.fromMap(map);
            or.doc_code = slouceni.getString("docCode");
          }
          JSONObject ret = new JSONObject(SolrIndexerCommiter.indexJSON(new JSONObject(JSON.toJSONString(or)), "offersCore"));

          Indexer indexer = new Indexer();
          indexer.indexDocOffers(or.doc_code);
          return ret;

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }
        return jo;

      }
    },
    REMOVERECORD {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {

        JSONObject jo = new JSONObject();
        try {

          Options opts = Options.getInstance();
          try (HttpSolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build()) {
            client.deleteById(opts.getString("offersCore", "offers"), req.getParameter("id"));
            client.commit(opts.getString("offersCore", "offers"));

            jo.put("msg", "removed");
          } catch (SolrServerException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            jo.put("error", ex.toString());
          }

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }
        return jo;

      }
    },
    ADDWANTED {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {

        /*
        Add to offers record field wanted new code
        req.getParameter("id") id of the offerrecord
        req.getParameter("knihovna") the library code who wants
         */
        JSONObject jo = new JSONObject();
        try {
          JSONObject json;
          if (req.getMethod().equals("POST")) {
            String js = IOUtils.toString(req.getInputStream(), "UTF-8");
            json = new JSONObject(js);
          } else {
            json = new JSONObject(req.getParameter("json"));
          }
          OfferRecord or = OfferRecord.fromJSON(json);
          JSONObject ret = new JSONObject(SolrIndexerCommiter.indexJSON(new JSONObject(JSON.toJSONString(or)), "offersCore"));

//          Indexer indexer = new Indexer();
//          indexer.indexWanted(or.doc_code,
//                  UsersController.toKnihovna(req).getCode(),
//                  or.chci);
          return ret;

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }
        return jo;

      }
    },
    ALL {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject jo = new JSONObject();
        try {

          Options opts = Options.getInstance();
          SolrQuery query = new SolrQuery("*");
          User user = UsersController.toKnihovna(req);
//          if(user != null) {
//            query.addFilterQuery("knihovna:" + user.getCode());
//          }
          query.addFilterQuery("content_type:offer");
          query.setRows(1000);
          query.set("wt", "json");
          query.setFields("*,fields:[json]");
          try (HttpSolrClient client = new HttpSolrClient.Builder(opts.getString("solrHost")).build()) {
            QueryRequest qreq = new QueryRequest(query);

            NoOpResponseParser dontMessWithSolr = new NoOpResponseParser();
            dontMessWithSolr.setWriterType("json");
            client.setParser(dontMessWithSolr);
            NamedList<Object> qresp = client.request(qreq, opts.getString("offersCore", "offers"));
            JSONObject r = new JSONObject((String) qresp.get("response"));
            return r.getJSONObject("response");

          } catch (SolrServerException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            jo.put("error", ex.toString());
          }

        } catch (JSONException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }

        return jo;

      }
    },
    BYID {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject jo = new JSONObject();
        try {

          Options opts = Options.getInstance();
          SolrQuery query = new SolrQuery("id:" + req.getParameter("id"));
          query.set("wt", "json");
          query.setFields("*");
          try (HttpSolrClient client = new HttpSolrClient.Builder(opts.getString("solrHost")).build()) {
            QueryRequest qreq = new QueryRequest(query);

            NoOpResponseParser dontMessWithSolr = new NoOpResponseParser();
            dontMessWithSolr.setWriterType("json");
            client.setParser(dontMessWithSolr);
            NamedList<Object> qresp = client.request(qreq, opts.getString("offersCore", "offers"));
            JSONObject r = new JSONObject((String) qresp.get("response"));
            return r.getJSONObject("response");

          } catch (SolrServerException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            jo.put("error", ex.toString());
          }

        } catch (JSONException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }

        return jo;

      }
    },
    RECORDS {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject jo = new JSONObject();
        try {

          Options opts = Options.getInstance();
          SolrQuery query = new SolrQuery("offer_id:" + req.getParameter("id"));
          query.set("wt", "json");
          query.setFields("*,fields:[json]");
          query.setSort("title", SolrQuery.ORDER.asc);
          query.setRows(100);
          try (HttpSolrClient client = new HttpSolrClient.Builder(opts.getString("solrHost")).build()) {
            QueryRequest qreq = new QueryRequest(query);

            NoOpResponseParser dontMessWithSolr = new NoOpResponseParser();
            dontMessWithSolr.setWriterType("json");
            client.setParser(dontMessWithSolr);
            NamedList<Object> qresp = client.request(qreq, opts.getString("offersCore", "offers"));
            JSONObject r = new JSONObject((String) qresp.get("response"));
            return r.getJSONObject("response");

          } catch (SolrServerException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            jo.put("error", ex.toString());
          }

        } catch (JSONException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }

        return jo;

      }
    },
    ADDFILE {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        JSONObject json = new JSONObject();

        if (!UsersController.isLogged(req)) {
          json.put("error", "not logged");
          return json;
        }

        /// Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request
        List /* FileItem */ items = upload.parseRequest(req);

        Iterator iter = items.iterator();

        String idOffer = req.getParameter("offerId");
        String format = req.getParameter("format");
        while (iter.hasNext()) {
          FileItem item = (FileItem) iter.next();
          if (item.isFormField()) {
            LOGGER.log(Level.INFO, "------ {0} param value : {1}", new Object[]{item.getFieldName(), item.getString()});
            switch (item.getFieldName()) {
              case "id":
                idOffer = item.getString();
                break;
              case "fileFormat":
                format = item.getString();
                break;
            }
          }
        }
        if (idOffer == null) {
          json.put("error", "nabidka ne platna, no id");
          LOGGER.log(Level.WARNING, "Offer id missing");
        } else {
          iter = items.iterator();
          while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            if (item.isFormField()) {
              continue;
            }
            try (InputStream uploadedStream = item.getInputStream()) {
              byte[] bytes = IOUtils.toByteArray(uploadedStream);
              ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
              //uploadedStream.mark(uploadedStream.available());

              try {
                String knihovna = UsersController.get(req).getString("code");

                if (format.equals("ALEPH")) {
                  TransformerFactory tfactory = TransformerFactory.newInstance();
                  StreamSource xslt = new StreamSource(new File(Options.getInstance().getString("alephXSL", "aleph_to_csv.xsl")));
                  StringWriter sw = new StringWriter();
                  StreamResult destStream = new StreamResult(sw);
                  Transformer transformer = tfactory.newTransformer(xslt);
                  transformer.transform(new StreamSource(bais), destStream);

                  //json.put("cvs", sw.toString());
                  //out.println(sw.toString());
                  processStream(new StringReader(sw.toString()), idOffer, knihovna, json);
                } else {
                  processStream(new InputStreamReader(bais, "UTF-8"), idOffer, knihovna, json);
                }

              } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "import to offer failed", ex);
                json.put("error", ex.toString());
              }
            }
          }
        }
        return json;
      }
    },;

    abstract JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception;
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>

}
