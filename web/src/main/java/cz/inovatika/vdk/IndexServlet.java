package cz.inovatika.vdk;

import cz.inovatika.vdk.common.DbUtils;
import cz.inovatika.vdk.solr.models.User;
import cz.inovatika.vdk.common.SolrIndexerCommiter;
import cz.inovatika.vdk.common.VDKJobData;
import cz.inovatika.vdk.solr.Indexer;
import cz.inovatika.vdk.solr.IndexerQuery;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
@WebServlet(value = "/index/*") 
public class IndexServlet extends HttpServlet {

  public static final Logger LOGGER = Logger.getLogger(IndexServlet.class.getName());
  public static final String ACTION_NAME = "action";
  static boolean isLocalhost = false;

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
   * methods.
   *
   * @param req servlet request
   * @param resp servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {

    try {
      String actionNameParam = req.getPathInfo().substring(1);
      if (actionNameParam != null) {
        Set<String> localAddresses = new HashSet<>();
        localAddresses.add(InetAddress.getLocalHost().getHostAddress());
        for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
          localAddresses.add(inetAddress.getHostAddress());
        }
        if (localAddresses.contains(req.getRemoteAddr())) {
          LOGGER.log(Level.INFO, "running from local address");
          isLocalhost = true;
        }

        Actions actionToDo = Actions.valueOf(actionNameParam.toUpperCase());
        if (UsersController.isLogged(req) || isLocalhost) {
          actionToDo.doPerform(req, resp);
        } else {
          resp.setContentType("application/json;charset=UTF-8");

          JSONObject json = new JSONObject();
          json.put("error", "not logged");
          resp.getWriter().print(json.toString());
        }
      } else {
        PrintWriter out = resp.getWriter();
        out.print("actionNameParam -> " + actionNameParam);
      }
    } catch (IOException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
      PrintWriter out = resp.getWriter();
      out.print(e1.toString());
    } catch (SecurityException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    } catch (Exception e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      PrintWriter out = resp.getWriter();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
      out.print(e1.toString());
    }
  }

  private static void removeAllDemands() throws Exception {
    SolrQuery query = new SolrQuery("poptavka:[* TO *]");
    query.addField("code");
    SolrDocumentList docs = IndexerQuery.query(query);
    Iterator<SolrDocument> iter = docs.iterator();
    while (iter.hasNext()) {
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
    }
  }

  enum Actions {

    INDEXWANTED {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
          Indexer indexer = new Indexer(f);
          indexer.indexWanted(Integer.parseInt(req.getParameter("id")));
          json.put("message", "Reakce pridana.");
        } catch (Exception ex) {
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    REMOVEALLWANTED {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
          Indexer indexer = new Indexer(f);
          indexer.removeAllWanted();
        } catch (Exception ex) {
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    INDEXALLWANTED {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {

          String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
          Indexer indexer = new Indexer(f);
          indexer.removeAllWanted();
          indexer.indexAllWanted();
        } catch (Exception ex) {
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    INDEXALLOFFERS {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          String f = System.getProperty("user.home") + File.separator
                  + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
          Indexer indexer = new Indexer(f);
          indexer.removeAllOffers();
          indexer.indexAllOffers();
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    INDEXOFFER {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
          Indexer indexer = new Indexer(f);
          indexer.indexOffer(req.getParameter("id"));
        } catch (Exception ex) {
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    REMOVEOFFER {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
          Indexer indexer = new Indexer(f);
          indexer.removeOffer(Integer.parseInt(req.getParameter("id")));
        } catch (Exception ex) {
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    REMOVEALLOFFERS {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
          Indexer indexer = new Indexer(f);
          indexer.removeAllOffers();
        } catch (Exception ex) {
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    ADDDEMAND {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          User kn = UsersController.toKnihovna(req);
          if (kn == null) {
            json.put("error", "rights.notlogged");
          } else {
            String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
            Indexer indexer = new Indexer(f);
            indexer.indexDemand(
                    req.getParameter("id"),
                    kn.getCode(),
                    req.getParameter("docCode"),
                    req.getParameter("zaznam"),
                    req.getParameter("ex"));
          }
        } catch (Exception ex) {
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    REMOVEDEMAND {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          User kn = UsersController.toKnihovna(req);
          if (kn == null) {
            json.put("error", "rights.notlogged");
          } else {
            String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
            Indexer indexer = new Indexer(f);
            indexer.removeDemand(
                    req.getParameter("id"),
                    kn.getCode(),
                    req.getParameter("docCode"),
                    req.getParameter("zaznam"),
                    req.getParameter("ex"));
          }
        } catch (Exception ex) {
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    INDEXALLDEMANDS {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
            User kn = UsersController.toKnihovna(req);
            if (isLocalhost || kn.hasRole(DbUtils.Roles.ADMIN)) {
              String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
              Indexer indexer = new Indexer(f);
              indexer.indexAllDemands();
          }else {
              json.put("error", "rights.insuficient");
            }
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    REMOVEALLDEMANDS {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          User kn = UsersController.toKnihovna(req);
          if (kn == null) {
            json.put("error", "rights.notlogged");
          } else {
            String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
            Indexer indexer = new Indexer(f);
            indexer.removeAllDemands();
          }
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    REINDEXDOCS {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          User kn = UsersController.toKnihovna(req);
          if (kn == null) {
            json.put("error", "rights.notlogged");
          } else {

            if (kn.hasRole(DbUtils.Roles.ADMIN)) {
              String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
              Indexer indexer = new Indexer(f);
              indexer.reindex();
            } else {
              json.put("error", "rights.insuficient");
            }
          }
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    REINDEX {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
            User kn = UsersController.toKnihovna(req);
            if (isLocalhost || kn.hasRole(DbUtils.Roles.ADMIN)) {
              String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
              Indexer indexer = new Indexer(f);
              indexer.reindex();
//                                    indexAllOffers(DbUtils.getConnection());
//                                    indexAllDemands(DbUtils.getConnection());
//                                    indexAllWanted(DbUtils.getConnection());
            } else {
              json.put("error", "rights.insuficient");
            }
          
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    },
    REINDEXDOC {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject json = new JSONObject();
        try {
          User kn = UsersController.toKnihovna(req);
            if (isLocalhost || kn.hasRole(DbUtils.Roles.ADMIN)) {

              String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
              Indexer indexer = new Indexer(f);

              indexer.reindexDocByIdentifier(req.getParameter("code"));
              SolrIndexerCommiter.closeClients();
            } else {
              json.put("error", "rights.insuficient");
            }
          
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        out.println(json.toString());
      }
    };

    abstract void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception;
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
