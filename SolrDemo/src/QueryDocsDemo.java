import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

public class QueryDocsDemo {
//	public static final String SOLR_URL = "http://192.168.230.128:8983/solr";
	public static final String SOLR_URL = "http://172.168.63.233:8983/solr";

	public static void main(String[] args) throws SolrServerException, IOException {
		HttpSolrServer server = new HttpSolrServer(SOLR_URL);
		server.setMaxRetries(1);
		server.setMaxRetries(1); // defaults to 0. > 1 not recommended.
		server.setConnectionTimeout(5000); // 5 seconds to establish TCP
		//��������£����²�����������
		//ʹ���ϰ汾solrj�����°汾��solrʱ����Ϊ�����汾��javabin incompatible,������Ҫ����Parser
		server.setParser(new XMLResponseParser());
		server.setSoTimeout(1000); // socket read timeout
		server.setDefaultMaxConnectionsPerHost(100);
		server.setMaxTotalConnections(100);
		server.setFollowRedirects(false); // defaults to false
		// allowCompression defaults to false.
		// Server side must support gzip or deflate for this to have any effect.
		server.setAllowCompression(true);

		//ʹ��ModifiableSolrParams���ݲ���
//		ModifiableSolrParams params = new ModifiableSolrParams();
//		// 192.168.230.128:8983/solr/select?q=video&fl=id,name,price&sort=price asc&start=0&rows=2&wt=json
//		// ���ò�����ʵ������URL�еĲ�������
//		// ��ѯ�ؼ���
//		params.set("q", "video");
//		// ������Ϣ
//		params.set("fl", "id,name,price,score");
//		// ����
//		params.set("sort", "price asc");
//		// ��ҳ,start=0���Ǵ�0��ʼ,rows=5��ǰ����5����¼,�ڶ�ҳ���Ǳ仯start���ֵΪ5�Ϳ�����
//		params.set("start", 2);
//		params.set("rows", 2);
//		// ���ظ�ʽ
//		params.set("wt", "javabin");
//		QueryResponse response = server.query(params);

		//ʹ��SolrQuery���ݲ�����SolrQuery�ķ�װ�Ը���
		server.setRequestWriter(new BinaryRequestWriter());
		SolrQuery query = new SolrQuery();
		query.setQuery("video");
		query.setFields("id","name","price","score");
		query.setSort("price", ORDER.asc);
		query.setStart(0);
		query.setRows(2);
//		query.setRequestHandler("/select");
		QueryResponse response = server.query( query );
		
		
		
		// �����õ��Ľ����
		System.out.println("Find:" + response.getResults().getNumFound());
		// ������
		int iRow = 1;
		for (SolrDocument doc : response.getResults()) {
			System.out.println("----------" + iRow + "------------");
			System.out.println("id: " + doc.getFieldValue("id").toString());
			System.out.println("name: " + doc.getFieldValue("name").toString());
			System.out.println("price: "
					+ doc.getFieldValue("price").toString());
			System.out.println("score: " + doc.getFieldValue("score"));
			iRow++;
		}
	}
}