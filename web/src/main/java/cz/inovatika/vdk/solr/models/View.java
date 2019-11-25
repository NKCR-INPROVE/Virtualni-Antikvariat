
package cz.inovatika.vdk.solr.models;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import cz.inovatika.vdk.common.MD5;
import java.util.Date;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.beans.Field;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class View {
  final static Logger LOGGER = Logger.getLogger(View.class.getName());
  @Field
  public String id;
  @Field
  public String name;
  @Field
  public String user;
  @Field
  public boolean global;
  @Field
  public String params;
  
  public static View fromJSON(JSONObject json) {
    View obj = JSON.parseObject(json.toString(), View.class);
    if (obj.id == null || obj.id.isBlank()) {
      obj.id = MD5.generate(new String[]{obj.name, obj.user, obj.global+""});
    }
    return obj;
  }
  
}
