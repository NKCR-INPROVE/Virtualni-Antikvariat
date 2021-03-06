package cz.inovatika.vdk;

import com.alibaba.fastjson.JSON;
import cz.inovatika.vdk.common.SolrIndexerCommiter;
import cz.inovatika.vdk.solr.IndexerQuery;
import cz.inovatika.vdk.solr.models.View;
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
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
@WebServlet(value = "/users/*")
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
          LOGGER.log(Level.FINE, "running from local address");
          isLocalhost = true;
        }

        Actions actionToDo = Actions.valueOf(actionNameParam.toUpperCase());
        //if (UsersController.isLogged(req) || isLocalhost) {
        out.print(actionToDo.doPerform(req, resp));
//        } else {
//          JSONObject json = new JSONObject();
//          json.put("error", "not logged");
//          out.print(json.toString());
//        }

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
    SAVE {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        JSONObject jo = new JSONObject();
        try {
          JSONObject js;
          if (req.getMethod().equals("POST")) {
            js = new JSONObject(IOUtils.toString(req.getInputStream(), "UTF-8"));
          } else {
            js = new JSONObject(req.getParameter("json"));
          }
          
          if (js.has("code")) {
            jo = UsersController.save(js);
          } else {
            jo = UsersController.add(js);
          }
          

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }
        return jo;

      }
    },
    RESETPWD {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        JSONObject jo = new JSONObject();
        try {
          JSONObject js;
          if (req.getMethod().equals("POST")) {
            js = new JSONObject(IOUtils.toString(req.getInputStream(), "UTF-8"));
          } else {
            js = new JSONObject(req.getParameter("json"));
          }
          jo = UsersController.resetHeslo(js);

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
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
          String user = req.getParameter("username");
          String pwd = req.getParameter("password");
          if (req.getMethod().equals("POST")) {
            JSONObject js = new JSONObject(IOUtils.toString(req.getInputStream(), "UTF-8"));
            user = js.getString("username");
            pwd = js.getString("password");
          }

          if (user != null) {
            JSONObject j = UsersController.login(req, user, pwd);
            if (j != null) {
              jo.put("logged", true);
              jo.put("user", j);

            } else {
              //resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              jo.put("logged", false);
              jo.put("error", "invalid user name or password");
            }

          } else {
            //resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jo.put("logged", false);
            jo.put("error", "invalid user name or password");
          }

        } catch (Exception ex) {
          //resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
    CENIK {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        JSONObject jo = UsersController.getDopravy();
        return jo;

      }
    },
    CART {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        return UsersController.getCart(req);

      }
    },
    STORECART {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json;
          if (req.getMethod().equals("POST")) {
            String js = IOUtils.toString(req.getInputStream(), "UTF-8");
            json = new JSONObject(js);
          } else {
            json = new JSONObject(req.getParameter("json"));
          }
        return UsersController.storeCart(json);

      }
    },
    ORDERCART {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json;
          if (req.getMethod().equals("POST")) {
            String js = IOUtils.toString(req.getInputStream(), "UTF-8");
            json = new JSONObject(js);
          } else {
            json = new JSONObject(req.getParameter("json"));
          }
        return UsersController.orderCart(json);

      }
    },
    PROCESSORDER {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json;
          if (req.getMethod().equals("POST")) {
            String js = IOUtils.toString(req.getInputStream(), "UTF-8");
            json = new JSONObject(js);
          } else {
            json = new JSONObject(req.getParameter("json"));
          }
        return UsersController.processOrder(json);

      }
    },
    ORDERS {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        
        return new JSONObject().put("orders", UsersController.getOrders(req));

      }
    },
    CHECK {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject jo = UsersController.exists(req.getParameter("username"));
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
    },
    SAVE_VIEW {
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
          View v = View.fromJSON(json);

          return new JSONObject(SolrIndexerCommiter
                  .indexJSON(new JSONObject(JSON.toJSONString(v)), "viewsCore"));

        } catch (Exception ex) {
          jo.put("error", ex.toString());
        }
        return jo;

      }
    },
    GET_VIEW {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject jo = new JSONObject();
        try {

          SolrQuery query = new SolrQuery("id:" + req.getParameter("id"));
          try (HttpSolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build()) {
            QueryRequest qreq = new QueryRequest(query);

            return new JSONObject(IndexerQuery.json(query, "viewsCore"));

          } catch (IOException ex) {
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
    INFO {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject jo = new JSONObject();
        try {
          jo = UsersController.getOne(req.getParameter("code"), false);
        } catch (JSONException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          jo.put("error", ex.toString());
        }
        return jo;
      }
    },
    VIEWS {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject jo = new JSONObject();
        try {

          SolrQuery query = new SolrQuery("*");
          query.setFields("*,params:[json]");
          return new JSONObject(IndexerQuery.json(query, "viewsCore"));
        } catch (JSONException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
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
