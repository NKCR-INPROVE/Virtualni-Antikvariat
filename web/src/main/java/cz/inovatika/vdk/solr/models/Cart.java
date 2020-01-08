package cz.inovatika.vdk.solr.models;

import com.alibaba.fastjson.JSON;
import cz.inovatika.vdk.common.MD5;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.beans.Field;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class Cart {

  final static Logger LOGGER = Logger.getLogger(Cart.class.getName());
  @Field
  public String id;
  @Field
  public String user;
  @Field
  public List<String> libraries;
  @Field
  public String status;
  @Field
  public String item;
  @Field
  public String doprava;
  @Field
  public Date created;

  public static Cart fromJSON(JSONObject json) {
    Cart o = JSON.parseObject(json.toString(), Cart.class);
    if (o.id == null || o.id.trim().isEmpty()) {
      o.id = MD5.generate(new String[]{o.user, o.item});
    }
    if (o.created == null) {
      //Instant instant = Instant.now();
      o.created = Date.from(Instant.now());
    }
    if (o.libraries == null) {
      o.libraries = new ArrayList<>();
    }
    return o;
  }

}
