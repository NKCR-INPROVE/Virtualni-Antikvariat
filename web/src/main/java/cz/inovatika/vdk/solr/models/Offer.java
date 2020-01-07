
package cz.inovatika.vdk.solr.models;

import com.alibaba.fastjson.JSON;
import cz.inovatika.vdk.common.MD5;
import java.util.Date;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.beans.Field;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class Offer {
  final static Logger LOGGER = Logger.getLogger(Offer.class.getName());
  @Field
  public String id;
  @Field
  public String content_type = "offer";
  @Field
  public String nazev;
  @Field
  public String knihovna;
  @Field
  public boolean expired;
  @Field
  public boolean closed;
  @Field
  public Date created;
  @Field
  public Date expires;
  
  public static Offer fromJSON(JSONObject json) {
    Offer offer = JSON.parseObject(json.toString(), Offer.class);
    if (offer.id == null || offer.id.trim().isEmpty()) {
      offer.id = MD5.generate(new String[]{offer.nazev, offer.knihovna, offer.created.toString()});
    }
    return offer;
  }
  
}
