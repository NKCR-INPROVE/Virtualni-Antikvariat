package cz.inovatika.vdk.solr;

import cz.inovatika.vdk.Options;
import cz.inovatika.vdk.common.SolrIndexerCommiter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Iterator;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author alberto
 */
public class IndexerQuery {

    public static SolrDocumentList query(SolrQuery query) throws SolrServerException, IOException {
        SolrClient server = SolrIndexerCommiter.getServer();
        QueryResponse rsp = server.query(query);
        return rsp.getResults();
    }

    public static SolrDocumentList query(String core, SolrQuery query) throws SolrServerException, IOException {
        SolrClient server = SolrIndexerCommiter.getServer(core);
        QueryResponse rsp = server.query(query);
        return rsp.getResults();
    }

    public static SolrDocumentList queryOneField(String q, String[] fields, String[] fq) throws SolrServerException, IOException {
        SolrClient server = SolrIndexerCommiter.getServer();
        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        query.setFilterQueries(fq);
        query.setFields(fields);
        query.setRows(100);
        QueryResponse rsp = server.query(query);
        return rsp.getResults();
    }
    
    public static String xml(String q) throws MalformedURLException, IOException {
        SolrQuery query = new SolrQuery(q);
        query.set("indent", true);

        return xml(query);
    }
    
    public static String xml(SolrQuery query) throws MalformedURLException, IOException {
        
        query.set("indent", true);
        query.set("wt", "xml");
        
        String urlQueryString = query.toQueryString();
        Options opts = Options.getInstance();
        String solrURL = String.format("%s/%s/select",
                opts.getString("solrHost", "http://localhost:8983/solr"),
                opts.getString("solrCore", "vdk_md5"));
        URL url = new URL(solrURL + urlQueryString);

        // use org.apache.commons.io.IOUtils to do the http handling for you
        String xmlResponse = IOUtils.toString(url, "UTF-8");

        return xmlResponse;
    }
    
    
    
    public static String terms(SolrQuery query) throws MalformedURLException, IOException {
        
        query.set("indent", true);
        query.set("wt", "json");
        
        String urlQueryString = query.toQueryString();
        Options opts = Options.getInstance();
        String solrURL = String.format("%s/%s/terms",
                opts.getString("solrHost", "http://localhost:8983/solr"),
                opts.getString("solrCore", "vdk_md5"));
        URL url = new URL(solrURL + urlQueryString);

        // use org.apache.commons.io.IOUtils to do the http handling for you
        String xmlResponse = IOUtils.toString(url, "UTF-8");

        return xmlResponse;
    }
    
    public static String json(SolrQuery query) throws MalformedURLException, IOException {
        
        query.set("wt", "json");

        String urlQueryString =query.toQueryString();
        Options opts = Options.getInstance();
        String solrURL = String.format("%s/%s/select",
                opts.getString("solrHost", "http://localhost:8983/solr"),
                opts.getString("solrCore", "vdk_md5"));
        URL url = new URL(solrURL + urlQueryString);

        return IOUtils.toString(url, "UTF-8");
    }
    
    public static String json(SolrQuery query, String core) throws MalformedURLException, IOException {
        
        query.set("wt", "json");

        String urlQueryString =query.toQueryString();
        Options opts = Options.getInstance();
        String solrURL = String.format("%s/%s/select",
                opts.getString("solrHost", "http://localhost:8983/solr"),
                opts.getString(core, core));
        URL url = new URL(solrURL + urlQueryString);

        return IOUtils.toString(url, "UTF-8");
    }
    
    public static String getOriginalXml(String id) throws SQLException {
        
        try {
            Options opts = Options.getInstance();
            SolrQuery query = new SolrQuery("id:\"" + id + "\"");
            query.addField("xml");
            query.setRows(1);
            SolrDocumentList docs = IndexerQuery.query(opts.getString("solrIdCore", "vdk_id"), query);
            Iterator<SolrDocument> iter = docs.iterator();
            if (iter.hasNext()) {
                SolrDocument resultDoc = iter.next();
                return (String) resultDoc.getFieldValue("xml");
            }else {
                return "<xml/>";
            }

        } catch (Exception ex) {
            return ex.toString();
        } 
    }

}
