
package cz.inovatika.vdk;

import cz.inovatika.vdk.common.Knihovna;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class LoginController {
  
  public static JSONObject get(HttpServletRequest req){
    JSONObject jo = new JSONObject();
    Object session = req.getSession().getAttribute("login");
    if(session != null){
      return (JSONObject) session;
    } else {
      return null;
    }
  }
  
  public static Knihovna toKnihovna(HttpServletRequest req) {
    JSONObject jo = get(req);
    if(jo != null) {
      try {
        Knihovna kn = new Knihovna(jo.getString("name"));
        return kn;
      } catch (NamingException | SQLException ex) {
        Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return null;
    
  }
  
  public static void logout(HttpServletRequest req){
    req.getSession().invalidate();
  }
  
  public static boolean login(HttpServletRequest req, String user, String pwd){
    try {
      JSONObject jo = new JSONObject();
      Options opts = Options.getInstance();
      JSONObject users = opts.getJSONObject("users");
      if(users.has(user) && users.getJSONObject(user).getString("pwd").equals(pwd)){
        req.getSession().setAttribute("login", users.getJSONObject(user));
        return true;
      }
      return false;
    } catch (IOException | JSONException ex) {
      Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
  }

  static boolean isLogged(HttpServletRequest req) {
    return req.getSession().getAttribute("login") != null;
  }
}
