/*
 * Copyright (C) 2013-2015 Alberto Hernandez
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.inovatika.vdk.solr;

import cz.inovatika.vdk.solr.models.User;
import cz.inovatika.vdk.UsersController;
import cz.inovatika.vdk.Options;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;

//import org.apache.velocity.tools.config.DefaultKey;
//import org.apache.velocity.tools.view.ViewToolContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SolrSearcher {

  public static final Logger LOGGER = Logger.getLogger(SolrSearcher.class.getName());

  private HttpServletRequest req;
  private Options opts;
  private boolean hasFilters = false;

  public SolrSearcher(HttpServletRequest req) {
    try {
      this.req = req;

      opts = Options.getInstance();

    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (JSONException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public JSONArray getSuggest() {
    try {
      String q = req.getParameter("term");
      SolrQuery query = new SolrQuery();
      if (q == null || q.equals("")) {
        return new JSONArray();
      }

      query.setParam(CommonParams.QT, "/terms");
      query.setTerms(true);
      query.setTermsPrefix(q.toUpperCase());
      query.setTermsLowerInclusive(true);
      query.addTermsField("title_suggest");
      JSONArray ja = new JSONObject(IndexerQuery.terms(query)).getJSONObject("terms").getJSONArray("title_suggest");
      JSONArray ret = new JSONArray();
      for (int i = 0; i < ja.length(); i = i + 2) {
        String val = ja.getString(i);
        ret.put(new JSONObject().put("value", val).put("label", val.substring(val.indexOf("##") + 2)));
      }

      return ret;
    } catch (IOException ex) {
      Logger.getLogger(SolrSearcher.class.getName()).log(Level.SEVERE, null, ex);
      return new JSONArray();
    }
  }

  private SolrQuery doQuery() {
    try {

      String q = req.getParameter("q");
      SolrQuery query = new SolrQuery();
      if (q == null || q.equals("")) {
        q = "*:*";
        query.setSort("_version_", SolrQuery.ORDER.desc);
      }
      query.setQuery(q);
      query.set("q.op", "AND");
      query.setFacet(true);
      query.set("json.nl", "arrntv"); 
      query.setFields("*", "ex:[json]", "author:[json]", "poptavka_ext:[json]", "nabidka_ext:[json]" ); 
      
      query.setStart(getStart());
      query.setRows(getRows());

      if (UsersController.isLogged(req)) {
        query.addFacetField(opts.getStrings("user_facets"));
      }
      query.addFacetField(opts.getStrings("facets"));

      query.setFacetMinCount(1);

      JSONObject others = opts.getJSONObject("otherParams");
      Iterator keys = others.keys();
      while (keys.hasNext()) {
        String key = (String) keys.next();
        Object val = others.get(key);
        if (val instanceof Integer) {
          query.set(key, (Integer) val);
        } else if (val instanceof String) {
          query.set(key, (String) val);
        } else if (val instanceof Boolean) {
          query.set(key, (Boolean) val);
        }

      }
      addFilters(query);
      return query;

    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return null;
    }
  }

  public String json() {
    try {
      SolrQuery query = doQuery();
      if (query != null) {
        query.set("wt", "json");
        String jsonResponse;
        try (HttpSolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build()) {
          QueryRequest qreq = new QueryRequest(query);
          NoOpResponseParser dontMessWithSolr = new NoOpResponseParser();
          dontMessWithSolr.setWriterType("json");
          client.setParser(dontMessWithSolr);
          NamedList<Object> qresp = client.request(qreq, opts.getString("solrCore", "catalog"));
          jsonResponse = (String) qresp.get("response");
          return jsonResponse;
        } catch (SolrServerException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          return null;
        }

      } else {
        return null;
      }
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return null;
    }
  }

  public String getAsXML() throws JSONException {
    try {
      SolrQuery query = doQuery();
      if (query != null) {
        return IndexerQuery.xml(query);
      } else {
        return null;
      }
    } catch (IOException ex) {
      Logger.getLogger(SolrSearcher.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  private void addFilters(SolrQuery query) throws IOException {
    //Dame pryc periodika
    query.addFilterQuery("-leader_format:s");
    if (req.getParameterValues("zdroj") != null) {
      for (String zdroj : req.getParameterValues("zdroj")) {
        if (zdroj.startsWith("-")) {
          query.addFilterQuery("{!tag=zdr}-zdroj:\"" + zdroj.substring(1) + "\"");
        } else {
          query.addFilterQuery("{!tag=zdr}zdroj:\"" + zdroj + "\"");
        }
      }
      hasFilters = true;
    }

    //Neprihlasene uzivatele vidi jen zaznamy ze zdroju
    if (!UsersController.isLogged(req)) {
      query.addFilterQuery("id:[* TO *]");
    }

    if (req.getParameterValues("exs") != null) {
      for (String ex : req.getParameterValues("exs")) {
        query.addFilterQuery("-" + ex);
      }
      hasFilters = true;
    }
    if (req.getParameterValues("fq") != null) {
      for (String fq : req.getParameterValues("fq")) {
        String[] parts = fq.split(":");
        //query.addFilterQuery(parts[0] + ":" + ClientUtils.escapeQueryChars(parts[1]));
        query.addFilterQuery(parts[0] + ":" + parts[1]);
        if (req.getParameterValues("zdroj") != null && fq.contains("pocet_exemplaru")) {

          for (String zdroj : req.getParameterValues("zdroj")) {
            if (!zdroj.startsWith("-")) {
              query.addFilterQuery("pocet_ex_" + zdroj + ":" + parts[1]);
            }
          }
        }
      }
      hasFilters = true;
    }

    if (req.getParameter("pocet_exemplaru") != null && !req.getParameter("pocet_exemplaru").isBlank()) {
      query.addFilterQuery("pocet_exemplaru:" + req.getParameter("pocet_exemplaru"));
    }

    if (req.getParameter("status") != null && !req.getParameter("status").isBlank()) {
      query.addFilterQuery("status:" + req.getParameter("status"));
    }

    if (req.getParameter("bohemika") != null) {
      query.addFilterQuery("bohemika:" + req.getParameter("bohemika"));
    }

    if (req.getParameter("matches") != null) {
      query.addFilterQuery("nabidka:[* TO *]");
      query.addFilterQuery("poptavka:" + UsersController.toKnihovna(req).getCode());
      hasFilters = true;
    }

    if (req.getParameter("offers") != null) {
//      if (UsersController.isLogged(req)) {
//        Date d = new Date();
//        User kn = UsersController.toKnihovna(req);
//        int exp = Options.getInstance().getInt("expirationDays", 7);
//        int from_days = kn.getPriorita() * exp;
//        int to_days = (kn.getPriorita() - 1) * exp;
//        query.addFilterQuery("nabidka_datum:[NOW-" + from_days + "DAYS TO NOW-" + to_days + "DAYS]");
//
//      }
      query.addFilterQuery("nabidka:[* TO *]");
      hasFilters = true;
    }

    if (req.getParameterValues("nabidka") != null) {
      for (String offer : req.getParameterValues("nabidka")) {
        query.addFilterQuery("nabidka:" + offer);
      }
      hasFilters = true;
    }

    if (req.getParameter("demands") != null) {
      query.addFilterQuery("poptavka:[* TO *]");
      hasFilters = true;
    }

    if (req.getParameterValues("demand") != null) {
      for (String demand : req.getParameterValues("demand")) {
        query.addFilterQuery("poptavka:" + demand);
      }
      hasFilters = true;
    }

    if (req.getParameter("wanted") != null) {
      query.addFilterQuery("chci:" + UsersController.toKnihovna(req).getCode());
      hasFilters = true;
    }

//        if (req.getParameter("nowanted") != null) {
//            query.addFilterQuery("nechci:" + UsersController.knihovna(req).getId());
//            hasFilters = true;
//        }
    if (req.getParameter("title") != null && !req.getParameter("title").equals("")) {
      query.addFilterQuery("title:" + req.getParameter("title"));
      hasFilters = true;
    }

    if (req.getParameter("author") != null && !req.getParameter("author").equals("")) {
      query.addFilterQuery("author:" + req.getParameter("author"));
      hasFilters = true;
    }

    if (req.getParameter("rok") != null && !req.getParameter("rok").equals("")) {
      query.addFilterQuery("rokvydani:" + req.getParameter("rok"));
      hasFilters = true;
    }

    if (req.getParameter("isbn") != null && !req.getParameter("isbn").equals("")) {
      query.addFilterQuery("isbn:" + req.getParameter("isbn") + "*");
      hasFilters = true;
    }

    if (req.getParameter("issn") != null && !req.getParameter("issn").equals("")) {
      query.addFilterQuery("issn:" + req.getParameter("issn") + "*");
      hasFilters = true;
    }

    if (req.getParameter("ccnb") != null && !req.getParameter("ccnb").equals("")) {
      query.addFilterQuery("ccnb:\"" + req.getParameter("ccnb") + "\"");
      hasFilters = true;
    }

    if (req.getParameter("vydavatel") != null && !req.getParameter("vydavatel").equals("")) {
      query.addFilterQuery("vydavatel:" + req.getParameter("vydavatel"));
      hasFilters = true;
    }

    if (req.getParameter("title_suggest") != null && !req.getParameter("title_suggest").equals("")) {
      query.addFilterQuery("title_suggest:" + req.getParameter("title_suggest"));
      hasFilters = true;
    }

  }

  public boolean getHasFilters() {
    return hasFilters;
  }

  private int getStart() throws UnsupportedEncodingException {

    String start = req.getParameter("start_export");
    if (start == null || start.equals("")) {
      start = req.getParameter("offset");
      if (start == null || start.equals("")) {
        start = "0";
      }
    }
    return Integer.parseInt(start);
  }

  private int getRows() throws UnsupportedEncodingException {
    if (req.getParameter("export") != null) {
      String rows = req.getParameter("rows_export");
      if (rows == null || rows.equals("")) {
        rows = "40";
      }
      return Integer.parseInt(rows);
    } else {
      String rows = req.getParameter("hits");
      if (rows == null || rows.equals("")) {
        rows = "40";
      }
      return Integer.parseInt(rows);
    }
  }

}
