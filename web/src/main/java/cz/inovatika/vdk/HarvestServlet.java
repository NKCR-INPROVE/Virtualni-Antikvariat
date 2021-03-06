
package cz.inovatika.vdk;

import cz.inovatika.vdk.solr.models.User;
import cz.inovatika.vdk.common.VDKJobData;
import cz.inovatika.vdk.oai.HarvesterJob;
import cz.inovatika.vdk.oai.HarvesterJobData;
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
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
@WebServlet(value = "/harvest/*") 
public class HarvestServlet extends HttpServlet {

  public static final Logger LOGGER = Logger.getLogger(HarvestServlet.class.getName());
  
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
      String actionNameParam = request.getPathInfo().substring(1);
      if (actionNameParam != null) {
        
        Actions actionToDo = Actions.valueOf(actionNameParam.toUpperCase());
        
        Set<String> localAddresses = new HashSet<>();
        localAddresses.add(InetAddress.getLocalHost().getHostAddress());
        for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
          localAddresses.add(inetAddress.getHostAddress());
        }
        if (localAddresses.contains(request.getRemoteAddr())) {
          LOGGER.log(Level.INFO, "running from local address");
          isLocalhost = true;
        }
        
        JSONObject json = actionToDo.doPerform(request, response);
//        LOGGER.log(Level.INFO, "[SEARCH] response={0} payload='{'{1}'}' user='{'{2}'}'",
//                new Object[]{json.toString(), request.getQueryString(), user});
        out.println(json.toString(2));
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

    FULL {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        
        JSONObject json = new JSONObject();
        try {
          User kn = (User) req.getSession().getAttribute("knihovna");
          if (kn == null && !isLocalhost) {
            json.put("error", "rights.notlogged");
          } else {
            if (isLocalhost || kn.role.equals("ADMIN")) {
              
              HarvesterJob hj = new HarvesterJob();
              JSONObject runParams = new JSONObject();
              if (req.getParameter("todisk") != null) {
                runParams.put("saveToDisk", true);
              }
              runParams.put("fullIndex", true);
              HarvesterJobData jobdata = new HarvesterJobData(new VDKJobData(req.getParameter("conf"), runParams));

              hj.harvestScheduled(jobdata);

              json.put("message", "harvest finished.");
            } else {
              json.put("error", "rights.insuficient");
            }
          }
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
          json.put("error", ex.toString());
        }

        return json;
      }
    },
    FACET {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json = new JSONObject();
        return json;
      }
    };

    abstract JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception;
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
