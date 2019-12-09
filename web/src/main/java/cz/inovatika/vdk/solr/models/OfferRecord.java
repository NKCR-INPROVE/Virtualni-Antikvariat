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
public class OfferRecord {
  final static Logger LOGGER = Logger.getLogger(OfferRecord.class.getName());
  @Field
  public String id;
  @Field
  public String content_type = "doc";
  @Field
  public String offer_id;
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
  public String fields;
  @Field
  public String[] chci;
  @Field
  public String[] nechci;
  @Field
  public boolean isVA;
  @Field
  public int cena;
  @Field
  public String comment;
  
  public static OfferRecord fromJSON(JSONObject json) {
    OfferRecord ofr = JSON.parseObject(json.toString(), OfferRecord.class);
    if (ofr.id == null || ofr.id.trim().isEmpty()) {
      ofr.id = MD5.generate(new String[]{ofr.offer_id, ofr.doc_code, ofr.knihovna, ofr.zaznam, ofr.exemplar, ofr.fields});
    }
    return ofr;
  }
  
  
}
