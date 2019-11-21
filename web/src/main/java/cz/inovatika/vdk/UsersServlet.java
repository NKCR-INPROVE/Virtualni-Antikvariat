package cz.inovatika.vdk;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class UsersServlet extends HttpServlet {

  public static final Logger LOGGER = Logger.getLogger(UsersServlet.class.getName());
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

    resp.setContentType("application/json;charset=UTF-8");
    resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
    resp.setHeader("Pragma", "no-cache"); // HTTP 1.0
    resp.setDateHeader("Expires", 0); // Proxies.
    PrintWriter out = resp.getWriter();
    
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
          out.print(actionToDo.doPerform(req, resp));
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
    ADD {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        JSONObject jo = new JSONObject();
        try {
          if (req.getMethod().equals("POST")) {
            String js = IOUtils.toString(req.getInputStream(), "UTF-8");
            jo = UsersController.add(new JSONObject(js));
          } else {
            jo = UsersController.add(new JSONObject(req.getParameter("json")));
          }
          
          

        } catch (Exception ex) {
          jo.put("logged", false);
          jo.put("error", ex.toString());
        }
        return jo;

      }
    },
    LOGIN {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        JSONObject jo = new JSONObject();
        try {

          String user = req.getParameter("user");
          if (user != null) {

            if (UsersController.login(req, user, req.getParameter("pwd"))) {
              jo.put("logged", true);

            } else {
              jo.put("logged", false);
              jo.put("error", "invalid user name or password");
            }

          } else {
            jo.put("logged", false);
            jo.put("error", "invalid user name or password");
          }

        } catch (Exception ex) {
          jo.put("logged", false);
          jo.put("error", ex.toString());
        }
        return jo;

      }
    },
    LOGOUT {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        
        JSONObject jo = new JSONObject();
        try {
          req.getSession().invalidate();
          jo.put("msg", "logged out");

        } catch (Exception ex) {
          jo.put("error", ex.toString());
        }
        return jo;
        
      }
    },
    ALL {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        
        JSONObject jo = UsersController.getAll();
        return jo;
        
      }
    },
    TESTLOGIN {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        
        JSONObject jo = new JSONObject();
        try {
          jo = (JSONObject) UsersController.get(req);
          if (jo == null) {
            jo = new JSONObject();
            jo.put("error", "nologged");
          }

        } catch (Exception ex) {
          jo.put("error", ex.toString());
        }
        return jo;
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
