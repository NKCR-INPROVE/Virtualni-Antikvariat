/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class Demand {
  
  final static Logger LOGGER = Logger.getLogger(Demand.class.getName());
  
  @Field
  public String id;
  @Field
  public String title;
  @Field
  public String doc_code;
  @Field
  public String knihovna;
  @Field
  public String zaznam;
  @Field
  public String exemplar;
  @Field
  public String comment;
  @Field
  public Date date;
  
  public static Demand fromJSON(JSONObject json) {
    Demand ofr = JSON.parseObject(json.toString(), Demand.class);
    if (ofr.id == null || ofr.id.trim().isEmpty()) {
      ofr.id = MD5.generate(new String[]{ofr.doc_code, ofr.knihovna, ofr.zaznam, ofr.exemplar, ofr.comment});
    }
    return ofr;
  }
  
}
