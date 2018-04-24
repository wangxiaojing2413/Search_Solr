package com.cpf.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;


@SuppressWarnings("deprecation")
public class SolrHttpServer {  
    //private Logger logger = LoggerFactory.getLogger(this.getClass());  
    private final static String URL = "http://localhost:8080/solr";  
    private final static Integer SOCKE_TTIMEOUT = 1000; // socket read timeout  
    private final static Integer CONN_TIMEOUT = 100;  
    private final static Integer MAXCONN_DEFAULT = 100;  
    private final static Integer MAXCONN_TOTAL = 100;  
    private final static Integer MAXRETRIES = 1;  
    private static HttpSolrServer  server = null;  
    private final static String ASC = "asc";  
      
    public void init() throws MalformedURLException {  
            server = new HttpSolrServer( URL );  
            //server.setParser(new XMLResponseParser());  
            server.setSoTimeout(SOCKE_TTIMEOUT);  
            server.setConnectionTimeout(CONN_TIMEOUT);  
            server.setDefaultMaxConnectionsPerHost(MAXCONN_DEFAULT);  
            server.setMaxTotalConnections(MAXCONN_TOTAL);  
            server.setFollowRedirects(false);  
            server.setAllowCompression(true);  
            server.setMaxRetries(MAXRETRIES);  
    }  
      
    public static SolrDocumentList query(Map<String, String> property, Map<String, String> compositor, Integer pageSize) throws Exception {  
        SolrQuery query = new SolrQuery();  
        // 设置搜索字段  
        if(null == property) {  
            throw new Exception("搜索字段不可为空!");  
        } else {  
            for(Object obj : property.keySet()) {  
                StringBuffer sb = new StringBuffer();  
                sb.append(obj.toString()).append(":");  
                sb.append(property.get(obj));  
                String sql = (sb.toString()).replace("AND", " AND ").replace("OR", " OR ").replace("NOT", " NOT ");  
                query.setQuery(sql);  
            }  
        }  
        // 设置结果排序  
        if(null != compositor) {  
            for(Object obj : compositor.keySet()) {  
                if(ASC == compositor.get(obj) || ASC.equals(compositor.get(obj))) {  
                    query.addSort(obj.toString(), SolrQuery.ORDER.asc);  
                } else {  
                    query.addSort(obj.toString(), SolrQuery.ORDER.desc);  
                }  
            }  
        }  
        if(null != pageSize && 0 < pageSize) {  
            query.setRows(pageSize);  
        }  
        QueryResponse qr = server.query(query);  
        SolrDocumentList docList = qr.getResults();  
        return docList;  
    }  
      
    public static Map<String, String> getQueryProperty(Object obj) throws Exception {  
        Map<String, String> result = new TreeMap<String, String>();  
        // 获取实体类的所有属性，返回Fields数组  
        Field[] fields = obj.getClass().getDeclaredFields();  
        for(Field f : fields) {  
            String name = f.getName();// 获取属性的名字  
            String type = f.getGenericType().toString();  
            if("class java.lang.String".equals(type)) {// 如果type是类类型，则前面包含"class "，后面跟类名  
                Method me = obj.getClass().getMethod("get" + UpperCaseField(name));  
                String tem = (String) me.invoke(obj);  
                if(null != tem) {  
                    result.put(name, tem);  
                }  
            }  
        }  
        return result;  
    }  
      
    public static List<Object> querySolrResult(Object propertyObj, Object compositorObj, Integer pageSize) throws Exception {  
        Map<String, String> propertyMap = new TreeMap<String, String>();  
        Map<String, String> compositorMap = new TreeMap<String, String>();  
        propertyMap = getQueryProperty(propertyObj);  
        compositorMap = getQueryProperty(compositorObj);  
        SolrDocumentList docList = query(propertyMap, compositorMap, pageSize);  
        List<Object> list = new ArrayList<Object>();  
        for(Object obj : docList) {  
            list.add(obj);  
        }  
        return list;  
    }  
      
    private static String UpperCaseField(String name) {  
        return name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());  
    }  
  
    public HttpSolrServer getServer() {  
        return server;  
    }  
  
    public void setServer(HttpSolrServer server) {  
        this.server = server;  
    }  
  
}  