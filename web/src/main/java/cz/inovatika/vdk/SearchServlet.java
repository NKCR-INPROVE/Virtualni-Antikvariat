package cz.inovatika.vdk;

import static cz.inovatika.vdk.common.SolrIndexerCommiter.getServer;
import cz.inovatika.vdk.solr.SolrSearcher;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
@WebServlet(value = "/search/*") 
public class SearchServlet extends HttpServlet {

  public static final Logger LOGGER = Logger.getLogger(SearchServlet.class.getName());
  public static final String ACTION_NAME = "action";

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
    resp.setContentType("application/json;charset=UTF-8");
    resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
    resp.setHeader("Pragma", "no-cache"); // HTTP 1.0
    resp.setDateHeader("Expires", 0); // Proxies.
    PrintWriter out = resp.getWriter();
    try {
      String actionNameParam = req.getPathInfo().substring(1);
      if (actionNameParam != null) {
        Actions actionToDo = Actions.valueOf(actionNameParam.toUpperCase());
        String json = actionToDo.doPerform(req, resp);
        String user = "";
//        if (req.getUserPrincipal() != null) {
//          user = req.getUserPrincipal().getName();
//        }
//        LOGGER.log(Level.INFO, "[SEARCH] response={0} payload='{'{1}'}' user='{'{2}'}'",
//                new Object[]{json.toString(), req.getQueryString(), user});
        out.println(json);
      } else {

        out.print("actionNameParam -> " + actionNameParam);
      }
    } catch (IOException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
      out.print(e1.toString());
    } catch (SecurityException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    } catch (Exception e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
      out.print(e1.toString());
    }
  }
  
    enum Actions {

    QUERY {
      @Override
      String doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // JSONObject json = new JSONObject();
        SolrSearcher searcher = new SolrSearcher((req));
        return searcher.json();
        
      }
    },
    FACET {
      @Override
      String doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json = new JSONObject();
        String xml = "<add><doc>\n" +
"    <field name=\"code\">111</field>\n" +
"    <field name=\"md5\">t1</field></doc>\n" +
"  <doc>\n" +
"    <field name=\"code\">333</field>\n" +
"    <field name=\"md5\">t22</field></doc></add>";
        DirectXmlRequest xmlreq = new DirectXmlRequest( "/update", xml );
        SolrClient client = getServer("catalog");
        
        client.request(xmlreq);
        // client.commit();
        
        client.close();
        return json.toString();
      }
    };

    abstract String doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception;
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
