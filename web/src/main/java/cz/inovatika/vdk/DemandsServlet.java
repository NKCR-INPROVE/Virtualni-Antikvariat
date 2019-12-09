package cz.inovatika.vdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import cz.inovatika.vdk.common.SolrIndexerCommiter;
import cz.inovatika.vdk.solr.Indexer;
import cz.inovatika.vdk.solr.models.Demand;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@WebServlet(name = "DemandsServlet", urlPatterns = {"/demands/*"})
public class DemandsServlet extends HttpServlet {

  public static final Logger LOGGER = Logger.getLogger(DemandsServlet.class.getName());
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
          Demand d = Demand.fromJSON(json);

          JSONObject ret = new JSONObject(SolrIndexerCommiter
                  .indexJSON(new JSONObject(JSON.toJSONString(d, SerializerFeature.WriteDateUseDateFormat)), "demandsCore"));

          Indexer indexer = new Indexer();
          indexer.indexDemand(d.id, d.knihovna, d.doc_code, d.zaznam, d.exemplar);
          return ret;

        } catch (Exception ex) {
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
            
            JSONObject json;
            if (req.getMethod().equals("POST")) {
              String js = IOUtils.toString(req.getInputStream(), "UTF-8");
              json = new JSONObject(js);
            } else {
              json = new JSONObject(req.getParameter("json"));
            }
            
            JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
            Demand d = Demand.fromJSON(json);
            client.deleteById(opts.getString("demandsCore", "demands"), d.id);
            client.commit(opts.getString("demandsCore", "demands"));
            Indexer indexer = new Indexer();
            indexer.removeDemand(d.id, d.knihovna, d.doc_code, d.zaznam, d.exemplar);
            
            jo.put("msg", "removed");
          } catch (SolrServerException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            jo.put("error", ex.toString());
          }

        } catch (Exception ex) {
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
          try (HttpSolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build()) {
            QueryRequest qreq = new QueryRequest(query);

            NoOpResponseParser dontMessWithSolr = new NoOpResponseParser();
            dontMessWithSolr.setWriterType("json");
            client.setParser(dontMessWithSolr);
            NamedList<Object> qresp = client.request(qreq, opts.getString("demandsCore", "demands"));
            JSONObject r = new JSONObject((String) qresp.get("response"));
            return r.getJSONObject("response");

          } catch (SolrServerException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            jo.put("error", ex.toString());
          }

        } catch (IOException | JSONException ex) {
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
          try (HttpSolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build()) {
            QueryRequest qreq = new QueryRequest(query);

            NoOpResponseParser dontMessWithSolr = new NoOpResponseParser();
            dontMessWithSolr.setWriterType("json");
            client.setParser(dontMessWithSolr);
            NamedList<Object> qresp = client.request(qreq, opts.getString("demandsCore", "demands"));
            JSONObject r = new JSONObject((String) qresp.get("response"));
            return r.getJSONObject("response");

          } catch (SolrServerException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            jo.put("error", ex.toString());
          }

        } catch (IOException | JSONException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }

        return jo;

      }
    };

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
