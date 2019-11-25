package cz.inovatika.vdk;

import com.alibaba.fastjson.JSON;
import cz.inovatika.vdk.common.MD5;
import cz.inovatika.vdk.common.SolrIndexerCommiter;
import cz.inovatika.vdk.solr.JSONUpdateRequest;
import cz.inovatika.vdk.solr.models.User;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.util.NamedList;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class UsersController {

  final static Logger LOGGER = Logger.getLogger(UsersController.class.getName());

  public static JSONObject get(HttpServletRequest req) {
    JSONObject jo = new JSONObject();
    Object session = req.getSession().getAttribute("login");
    if (session != null) {
      return (JSONObject) session;
    } else {
      return null;
    }
  }

  public static User toKnihovna(HttpServletRequest req) {
    JSONObject jo = get(req);
    if (jo != null) {
      User kn = User.byCode(jo.getString("name"));
      return kn;
    }
    return null;

  }

  public static void logout(HttpServletRequest req) {
    req.getSession().invalidate();
  }

  public static JSONObject getAll() {
    try {

      Options opts = Options.getInstance();
      SolrQuery query = new SolrQuery("*");
      query.setFields("code", "nazev", "role", "priorita", "telefon", "email", "sigla", "adresa");
      try (HttpSolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build()) {
          QueryRequest qreq = new QueryRequest(query);

        NoOpResponseParser dontMessWithSolr = new NoOpResponseParser();
        dontMessWithSolr.setWriterType("json");
        client.setParser(dontMessWithSolr);
        NamedList<Object> qresp = client.request(qreq, opts.getString("usersCore", "users"));
        JSONObject r = new JSONObject((String) qresp.get("response"));
        return r.getJSONObject("response");

      } catch (SolrServerException | IOException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
      }

    } catch (IOException | JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public static JSONObject login(HttpServletRequest req, String code, String pwd) {
    try {

      Options opts = Options.getInstance();
      SolrQuery query = new SolrQuery("code:\"" + code + "\"");
      try (SolrClient client = new HttpSolrClient.Builder(String.format("%s/%s/",
              opts.getString("solrHost", "http://localhost:8983/solr"),
              opts.getString("usersCore", "users")))
              .build()) {

        final QueryResponse response = client.query(query);
        if (response.getResults().getNumFound() == 0) {
          LOGGER.log(Level.INFO, "Invalid username {0}", code);
          return null;
        }
        User user = response.getBeans(User.class).get(0);
        if (user.getHeslo().equals(MD5.generate(pwd))) {
          JSONObject json = new JSONObject(JSON.toJSONString(user));
          json.remove("heslo");
          req.getSession().setAttribute("login", json);
          return json;
        } else {
          LOGGER.log(Level.INFO, "Invalid password");
        }

      } catch (SolrServerException | IOException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
        return null;
      }

      return null;
    } catch (IOException | JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return null;
    }
  }
  

  public static boolean isLogged(HttpServletRequest req) {
    return req.getSession().getAttribute("login") != null;
  }

  public static JSONObject add(JSONObject json) {
    try {
      Options opts = Options.getInstance();
      String jsonStr = SolrIndexerCommiter.indexJSON(json, "usersCore");
      
//      SolrClient solr = new HttpSolrClient.Builder(String.format("%s/%s/",
//              opts.getString("solrHost", "http://localhost:8983/solr"),
//              opts.getString("usersCore", "users")))
//              .build();
//
//      JSONUpdateRequest request = new JSONUpdateRequest(json);
//      UpdateResponse response = request.setCommitWithin(100).process(solr);
//      LOGGER.log(Level.INFO, response.jsonStr());
      
      // User u = JSON.parseObject(json.toString(), User.class); 
      // solr.addBean(u, 1000);

//      solr.close();
      return new JSONObject(jsonStr);
    } catch (IOException | JSONException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return new JSONObject().put("error", ex);
    }
  }
}
