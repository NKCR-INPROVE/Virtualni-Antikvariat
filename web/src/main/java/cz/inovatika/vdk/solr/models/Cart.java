
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
public class Cart {
  final static Logger LOGGER = Logger.getLogger(Cart.class.getName());
  @Field
  public String id;
  @Field
  public String user;
  @Field
  public String status;
  @Field
  public String item;
  @Field
  public Date created;
  
  public static Cart fromJSON(JSONObject json) {
    Cart o = JSON.parseObject(json.toString(), Cart.class);
    if (o.id == null || o.id.trim().isEmpty()) {
      o.id = MD5.generate(new String[]{o.user, o.item});
    }
    if (o.created == null) {
      o.created = new Date();
    }
    return o;
  }
  
}
