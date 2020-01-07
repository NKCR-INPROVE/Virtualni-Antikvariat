package cz.inovatika.vdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import cz.inovatika.vdk.common.SolrIndexerCommiter;
import cz.inovatika.vdk.solr.models.Cart;
import cz.inovatika.vdk.solr.models.OfferRecord;
import cz.inovatika.vdk.solr.models.User;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
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
import javax.servlet.http.HttpServletRequest;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;
import org.json.JSONArray;
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
      User kn = getUser(jo.getString("code"));
      return kn;
    }
    return null;

  }

  public static JSONObject getCart(HttpServletRequest req) {
    if (isLogged(req)) {
      JSONObject jo = get(req);
      if (jo != null) {
        try {
          Options opts = Options.getInstance();
          SolrQuery query = new SolrQuery("user:" + jo.getString("code"));
          try (HttpSolrClient client = new HttpSolrClient.Builder(opts.getString("solrHost")).build()) {
            final QueryResponse response = client.query(opts.getString("cartCore"), query);
            List<Cart> cart = response.getBeans(Cart.class);
            LOGGER.log(Level.INFO, "cart {0}", JSON.toJSONString(cart));
            return new JSONObject().put("cart", new JSONArray(JSON.toJSONString(cart)));
            
          } catch (SolrServerException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
          }

        } catch (JSONException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
        }
      }
    }
    return new JSONObject().put("cart", new JSONArray());
  }

  public static JSONObject storeCart(JSONObject json) {
    try {
      Cart cart = Cart.fromJSON(json);
      JSONObject jo = new JSONObject(JSON.toJSONString(cart));
      SolrIndexerCommiter
              .indexJSON(jo, "cartCore");
      return jo;
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return new JSONObject().put("error", ex);
    }
  }

  public static JSONObject orderCart(JSONObject json) {
    try {
      
//       JSON in CART format 

      
      Cart cart = Cart.fromJSON(json);
      JSONObject jo = new JSONObject(JSON.toJSONString(cart, SerializerFeature.WriteDateUseDateFormat));
      SolrIndexerCommiter
              .indexJSON(jo, "cartCore");
      User user = JSON.parseObject(cart.user, User.class);

      // extract libaries and send mail.
      JSONObject doprava = new JSONObject(cart.doprava);
      List<OfferRecord> records = JSON.parseArray(cart.item, OfferRecord.class);
      Map<String, StringBuilder> libraries = new HashMap();
      for(OfferRecord record : records){
        if (!libraries.containsKey(record.knihovna)) {
          libraries.put(record.knihovna, new StringBuilder());
        }
        
        StringBuilder sb = libraries.get(record.knihovna);
        sb.append(record.title);
        
      }
      
      for (String key : libraries.keySet()) {
        sendCartMail(libraries.get(key).toString(), user, key);
      }
      
      return json;
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return new JSONObject().put("error", ex);
    }
  }

  public static void logout(HttpServletRequest req) {
    req.getSession().invalidate();
  }

  public static JSONObject getOne(String code, boolean pwd) {
    try {

      Options opts = Options.getInstance();
      SolrQuery query = new SolrQuery("code:" + code);
      // query.setFields("code", "username", "nazev", "role", "priorita", "telefon", "email", "sigla", "adresa");
      try (HttpSolrClient client = new HttpSolrClient.Builder(opts.getString("solrHost", "http://localhost:8983/solr")).build()) {
        final QueryResponse response = client.query(opts.getString("usersCore", "users"), query);
        User user = response.getBeans(User.class).get(0);
        if (!pwd) {
          user.heslo = null;
        }
        return new JSONObject(JSON.toJSONString(user));

      } catch (SolrServerException | IOException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
      }

    } catch (JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public static User getUser(String code) {
    try {

      Options opts = Options.getInstance();
      SolrQuery query = new SolrQuery("code:" + code);
      try (HttpSolrClient client = new HttpSolrClient.Builder(opts.getString("solrHost")).build()) {
        final QueryResponse response = client.query(opts.getString("usersCore", "users"), query);
        User user = response.getBeans(User.class).get(0);
        return user;

      } catch (SolrServerException | IOException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
      }

    } catch (JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public static JSONObject getAll() {
    try {

      Options opts = Options.getInstance();
      SolrQuery query = new SolrQuery("*");
      // query.setFields("code", "username", "nazev", "role", "priorita", "telefon", "email", "sigla", "adresa");
      try (HttpSolrClient client = new HttpSolrClient.Builder(opts.getString("solrHost", "http://localhost:8983/solr")).build()) {
        QueryRequest qreq = new QueryRequest(query);

        NoOpResponseParser dontMessWithSolr = new NoOpResponseParser();
        dontMessWithSolr.setWriterType("json");
        client.setParser(dontMessWithSolr);
        NamedList<Object> qresp = client.request(qreq, opts.getString("usersCore", "users"));
        JSONObject r = new JSONObject((String) qresp.get("response"));
        JSONObject resp = r.getJSONObject("response");
        for (int i = 0; i < resp.getJSONArray("docs").length(); i++) {
          resp.getJSONArray("docs").getJSONObject(i).remove("heslo");
          resp.getJSONArray("docs").getJSONObject(i).remove("_version_");
          resp.getJSONArray("docs").getJSONObject(i).remove("timestamp");
        }
        return resp;

      } catch (SolrServerException | IOException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
      }

    } catch (JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return null;
  }
  
  public static JSONObject getDopravy() {
    try {

      Options opts = Options.getInstance();
      SolrQuery query = new SolrQuery("role:LIBRARY");
      query.setFields("code", "username", "doprava", "platba", "cenik_osobni", "cenik_nadobirku", "cenik_predem");
      try (HttpSolrClient client = new HttpSolrClient.Builder(opts.getString("solrHost", "http://localhost:8983/solr")).build()) {
        QueryRequest qreq = new QueryRequest(query);

        NoOpResponseParser dontMessWithSolr = new NoOpResponseParser();
        dontMessWithSolr.setWriterType("json");
        client.setParser(dontMessWithSolr);
        NamedList<Object> qresp = client.request(qreq, opts.getString("usersCore", "users"));
        JSONObject r = new JSONObject((String) qresp.get("response"));
        JSONObject resp = r.getJSONObject("response");
        
        return resp;

      } catch (SolrServerException | IOException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
      }

    } catch (JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public static JSONObject login(HttpServletRequest req, String code, String pwd) {
    try {

      Options opts = Options.getInstance();
      SolrQuery query = new SolrQuery("username:\"" + code + "\"");
      query.addFilterQuery("active:true");
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
        // if (user.getHeslo().equals(MD5.generate(pwd))) {
        if (user.heslo.equals(pwd)) {
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
    } catch (JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return null;
    }
  }

  public static boolean isLogged(HttpServletRequest req) {
    if (req.getSession().getAttribute("login") != null) {
      return true;
    } else {
      try {
        // try Basic auth
        // Get Authorization header
        String auth = req.getHeader("Authorization");
        // Do we allow that user?
        if (!allowUser(req, auth)) {
          // Not allowed, so report he's unauthorized
          //res.setHeader("WWW-Authenticate", "BASIC realm=\"appuntivari test\"");
          //res.sendError(res.SC_UNAUTHORIZED);
          return false;
          // Could offer to add him to the allowed user list
        } else {
          // Allowed, so show him the secret stuff
          //out.println("Top-secret stuff");
          return true;
        }
      } catch (IOException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
        return false;
      }
    }
  }

  protected static boolean allowUser(HttpServletRequest req, String auth) throws IOException {

    if (auth == null) {
      LOGGER.log(Level.INFO, "No Auth");
      return false;
    }
    if (!auth.toUpperCase().startsWith("BASIC ")) {
      LOGGER.log(Level.INFO, "Only Accept Basic Auth");
      return false;
    }

    // Get encoded user and password, comes after "BASIC "  
    String userpassEncoded = auth.substring(6);

    // Decode it, using any base 64 decoder  
    byte[] decoded = Base64.getDecoder().decode(userpassEncoded);

    String userpassDecoded = new String(decoded);

    String account[] = userpassDecoded.split(":");

    return login(req, account[0], account[1]) != null;
  }

  public static JSONObject add(JSONObject json) {
    try {
      // generate code
      User user = User.fromJSON(json);
      JSONObject jo = new JSONObject(JSON.toJSONString(user));

      String jsonStr = SolrIndexerCommiter.indexJSON(jo, "usersCore");
      return new JSONObject(jsonStr);
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return new JSONObject().put("error", ex);
    }
  }

  public static JSONObject save(JSONObject json) {
    try {
      //Retreive pwd. It should be missed in request
      JSONObject orig = getOne(json.getString("code"), true);
      json.put("heslo", orig.get("heslo"));
      User user = User.fromJSON(json);
      JSONObject jo = new JSONObject(JSON.toJSONString(user));
      String jsonStr = SolrIndexerCommiter.indexJSON(jo, "usersCore");
      return new JSONObject(jsonStr);
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return new JSONObject().put("error", ex);
    }
  }

  public static JSONObject resetHeslo(JSONObject json) {
    try {
      JSONObject orig = getOne(json.getString("code"), true).getJSONArray("docs").getJSONObject(0);
      if (json.getString("oldheslo").equals(orig.getString("heslo"))) {
        orig.put("heslo", json.getString("newheslo"));
        String jsonStr = SolrIndexerCommiter.indexJSON(orig, "usersCore");
        return new JSONObject(jsonStr);
      } else {
        return (new JSONObject()).put("error", "heslo.nespravne_heslo");
      }
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return new JSONObject().put("error", ex);
    }
  }

  public static JSONObject exists(String username) {
    Options opts = Options.getInstance();
    SolrQuery query = new SolrQuery("username:\"" + username + "\"");
    try (HttpSolrClient client = new HttpSolrClient.Builder(opts.getString("solrHost", "http://localhost:8983/solr")).build()) {
      return new JSONObject().put("exists", client.query(opts.getString("usersCore", "users"), query).getResults().getNumFound() > 0);
    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return new JSONObject().put("error", ex);
    }
  }
  
  private static void sendCartMail(String msg, User user, String library) {
    try {
      Options opts = Options.getInstance();
      
      String from = opts.getString("admin.email");
      User kn = getUser(library);
      String to = kn.email;
      try {
        Properties properties = System.getProperties();
        Session session = Session.getDefaultInstance(properties);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO,
                new InternetAddress(to));

        message.setSubject(opts.getString("cart.email.subject"));

        String body = user.email + "\n" + msg;
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
}
