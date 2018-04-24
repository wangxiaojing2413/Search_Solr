package com.cpf.service;  
  
import java.io.IOException;  
import java.net.MalformedURLException;  
import java.util.ArrayList;  
import java.util.Iterator;  
import java.util.List;  
import java.util.Map;  
  
  
import org.apache.solr.client.solrj.SolrQuery;  
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;  
import org.apache.solr.common.SolrDocument;  
import org.apache.solr.common.SolrDocumentList;  
import org.apache.solr.common.SolrInputDocument;  
  
  
  
public class SolrService {  
     private static HttpSolrServer solrServer;  
     public  SolrService(String solrUrl){  
    	 solrServer= new HttpSolrServer(solrUrl); 
     }  
       
     public void createIndexs(List<Good> list){  
         List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();  
         for(Good good :list){  
             SolrInputDocument doc= new SolrInputDocument();   
             doc.addField("id", good.getId());  
             doc.addField("merchant_id", good.getMerchantId());  
             doc.addField("region_id", good.getRegionId());  
             doc.addField("good_name", good.getName());  
             doc.addField("good_no", good.getNo());  
             doc.addField("barcode", good.getBarcode());  
             doc.addField("good_desc", good.getDesc());  
             doc.addField("price", good.getPrice());  
             doc.addField("num", good.getNum());  
             doc.addField("typeId", good.getTypeId());  
             doc.addField("typeName", good.getTypeName());  
             docs.add(doc);  
         }  
         try {  
            solrServer.add(docs);  
            solrServer.optimize();  
            solrServer.commit();  
        } catch (Exception e) {  
          
            e.printStackTrace();  
        }   
           
     }  
     public List<Good> search(String key) throws IOException{  
         List<Good> goodList= new ArrayList<Good>();  
         SolrQuery query= new SolrQuery();  
         query.setQuery("good_desc:"+key);  
         query.setRows(20);  
         query.setHighlight(true);  
            // 设置高亮区域  
         query.addHighlightField("good_desc");  
            // 设置高亮区域前缀  
         query.setHighlightSimplePre("<font color=\"red\">");  
            // 设置高亮区域后缀  
         query.setHighlightSimplePost("</font>");  
        // query.setFacet(true);  
         QueryResponse rsp = null;  
            try {  
                rsp = solrServer.query(query);  
              
            } catch (SolrServerException e) {  
                e.printStackTrace();  
            }  
            SolrDocumentList documentList=rsp.getResults();  
            @SuppressWarnings("unused")  
            Map<String, Map<String,List<String>>> map=rsp.getHighlighting();  
              
            Iterator<SolrDocument> list=documentList.iterator();  
              
            while(list.hasNext()){  
                SolrDocument doucment=list.next();  
                Good good= new Good();  
                String goodId=String.valueOf(doucment.get("id"));  
                String goodName=(String) doucment.get("good_name");  
                String goodDesc=(String) doucment.get("good_desc");  
                if(map.get(goodId)!=null && map.get(goodId).size()>0){  
                    if(map.get(goodId).get("good_desc")!=null){  
                        goodDesc=map.get(goodId).get("good_desc").get(0);  
                    }  
                }  
                good.setId(Integer.parseInt(goodId));  
                good.setName(goodName);  
                good.setDesc(goodDesc);  
                goodList.add(good);  
            }  
              
         return goodList;  
     }  
     public void delIndex(){  
         try {  
            solrServer.deleteByQuery("*:*");  
            solrServer.commit();//要记得提交数据  
        } catch (Exception e) {  
            e.printStackTrace();  
        }   
     }  
    public static HttpSolrServer getSolrServer() {  
        return solrServer;  
    }  
    public static void setSolrServer(HttpSolrServer solrServer) {  
        SolrService.solrServer = solrServer;  
    }  
       
}  
  
