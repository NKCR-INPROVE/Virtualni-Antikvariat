/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.inovatika.vdk.xml;

import cz.inovatika.vdk.common.MD5;
import cz.inovatika.vdk.common.UTFSort;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;

/**
 *
 * @author alberto
 */
public class XSLFunctions {
  final static Logger LOGGER = Logger.getLogger(XSLFunctions.class.getName());
    
    UTFSort utf_sort;
    
    public XSLFunctions() throws IOException{
        
        utf_sort = UTFSort.getInstance();
    }
    
    public int validYear(String year){
        try{
            int y = Integer.parseInt(year);
            if(y>999 && y<2050){
                return y;
            }else{
                return 0;
            }
        }catch(Exception ex){
            
            return 0;
        }
    }
    
    public String prepareCzechLower(String s) throws Exception {
        //return removeDiacritic(s).toLowerCase().replace("ch", "hz");
        return utf_sort.translate(s.toLowerCase());
    }
    
    public String prepareCzech(String s) throws Exception {
        //return removeDiacritic(s).toLowerCase().replace("ch", "hz");
        return utf_sort.translate(s);
    }

    public String encode(String url) throws URIException {
        return URIUtil.encodeQuery(url);
    }
    public String generateNormalizedMD5(String s) {
        return MD5.generate(new String[]{s});
    }
    
    public String md5FromNodeSet(org.w3c.dom.NodeList nodes){
        String[] sb = new String[nodes.getLength()];
        for(int i = 0; i<nodes.getLength(); i++){
            Node node = nodes.item(i);
            sb[i]=node.getNodeValue();
        }
        
        return MD5.generate(sb);
    }
    
    
    public String strongNormalizedMD5(String s) throws IOException {
        
        s = utf_sort.translate(s).toLowerCase().replaceAll("[| ]", "");
        return MD5.generate(s);
    }
    
    public String escapeRegex(String s) {
      return Pattern.quote(s);
    }
    
    public String join(org.w3c.dom.NodeList n1, org.w3c.dom.NodeList n2, org.w3c.dom.NodeList n3, org.w3c.dom.NodeList n4) {
      String joined = joinM(n1, n2, n3, n4);
//      LOGGER.log(Level.INFO, joined);
//      if (escape) {
//        joined = Pattern.quote(joined);
//      }
//      LOGGER.log(Level.INFO, joined);
      return joined;
    }
    
    public String joinM(org.w3c.dom.NodeList... nodesList) {
      //StringBuilder sb = new StringBuilder();
      ArrayList<String> sb = new ArrayList<>();
      for(org.w3c.dom.NodeList nodes : nodesList) {
        
        for(int i = 0; i<nodes.getLength(); i++){
            Node node = nodes.item(i);
            String val = node.getTextContent();
            if (val != null) {
              val = val.trim();
              if (val.endsWith(",")) {
                val = val.substring(0, val.length()-1);
              }
              sb.add("\""+val+"\"");
              //sb.append("\""+val+"\"");
            }
        }
      }
      
      return StringUtils.join(sb, ",");
    }
    
}
