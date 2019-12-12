package cz.inovatika.vdk.solr.models;

import cz.inovatika.vdk.Options;
import cz.inovatika.vdk.common.DbUtils;
import cz.inovatika.vdk.common.MD5;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class User {

  final static Logger LOGGER = Logger.getLogger(User.class.getName());
  @Field
  private String code;
  @Field
  private String username;
  @Field
  private String nazev;
  @Field
  private String heslo;
  @Field
  private String sigla;
  @Field
  private String adresa;
  @Field
  private String role;
  @Field
  private int priorita;
  @Field
  private String email;
  @Field
  private String telefon;
  

  public static User fromJSONx(JSONObject json) {
    
    User user = new User();
    user.code = json.getString("code");
    user.username = json.getString("username");
    user.heslo = MD5.generate(json.getString("heslo"));
    user.nazev = json.getString("nazev");
    user.role = json.getString("role");
    user.priorita = json.getInt("priorita");
    user.telefon = json.getString("telefon");
    user.email = json.getString("email");
    user.sigla = json.getString("sigla");
    user.adresa = json.getString("adresa");
    return user;
  }

  public static User byCode(String code) {
    try {
      SolrQuery query = new SolrQuery("code:\"" + code + "\"");
      return query(query);
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return null;
    }
  }

  private static User query(SolrQuery query) throws IOException {

    Options opts = Options.getInstance();
    try (SolrClient client = new HttpSolrClient.Builder(String.format("%s/%s/",
            opts.getString("solrHost", "http://localhost:8983/solr"),
            opts.getString("usersCore", "users")))
            .build()) {

      final QueryResponse response = client.query(query);
      return response.getBeans(User.class).get(0);

    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return null;
    }
  }

//  public JSONObject getJsonx() throws JSONException {
//    JSONObject j = new JSONObject();
//    j.put("name", nazev);
//    j.put("code", code);
//    j.put("priorita", priorita);
//    j.put("role", role);
//    j.put("telefon", telefon);
//    j.put("email", email);
//    j.put("sigla", getSigla());
//    j.put("adresa", getAdresa());
//    return j;
//  }

  /**
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * @param code the code to set
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the nazev
   */
  public String getNazev() {
    return nazev;
  }

  /**
   * @param nazev the nazev to set
   */
  public void setNazev(String nazev) {
    this.nazev = nazev;
  }

  /**
   * @return the heslo
   */
  public String getHeslo() {
    return heslo;
  }

  /**
   * @param heslo the heslo to set
   */
  public void setHeslo(String heslo) {
    this.heslo = heslo;
  }

  /**
   * @return the roles
   */
  public String getRole() {
    return role;
  }

  public boolean hasRole(DbUtils.Roles role) {
    return role.equals(role.toString());
  }

  public boolean isSourceLib() {
    return hasRole(DbUtils.Roles.SOURCELIB);
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param email the email to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return the telefon
   */
  public String getTelefon() {
    return telefon;
  }

  /**
   * @param telefon the telefon to set
   */
  public void setTelefon(String telefon) {
    this.telefon = telefon;
  }


  /**
   * @return the priorita
   */
  public int getPriorita() {
    return priorita;
  }

  /**
   * @param priorita the priorita to set
   */
  public void setPriorita(int priorita) {
    this.priorita = priorita;
  }

  /**
   * @return the sigla
   */
  public String getSigla() {
    return sigla;
  }

  /**
   * @param sigla the sigla to set
   */
  public void setSigla(String sigla) {
    this.sigla = sigla;
  }

  /**
   * @return the adresa
   */
  public String getAdresa() {
    return adresa;
  }

  /**
   * @param adresa the adresa to set
   */
  public void setAdresa(String adresa) {
    this.adresa = adresa;
  }

}
