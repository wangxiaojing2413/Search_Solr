package com.lijie.solr;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Collation;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Correction;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrMain {

    /**
     * solr http�����ַ
     */
    public static String SOLR_URL;

    /**
     * solr��core
     */
    public static String SOLR_CORE;

    static {
        Properties properties = new Properties();
        String path = SolrMain.class.getResource("/").getFile().toString()
                + "solr.properties";
        try {
            //FileInputStream fis = new FileInputStream(new File(path));
           //properties.load(fis);
            //SOLR_URL = properties.getProperty("SOLR_URL");
            //SOLR_CORE = properties.getProperty("SOLR_CORE");
            SOLR_URL = "http://192.168.21.130:8983/solr/";
            SOLR_CORE = "person";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ���������
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 1.���Բ����ĵ�
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", "00001");
        map.put("name", "lijie");
        map.put("age", "24");
        map.put("addr", "����");
        addDocument(map, SOLR_CORE);

        // 2.ͨ��bean���document
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("00002", "lisi", 25, "����"));
        persons.add(new Person("00003", "wangwu", 26, "�Ϻ�"));
        addDocumentByBean(persons, SOLR_CORE);

        // 3.����id����ɾ������
        List<String> ids = new ArrayList<String>();
        ids.add("00001");
        ids.add("00002");
        ids.add("00003");
        deleteDocumentByIds(ids, SOLR_CORE);

        // 4.��ѯ
        getDocument(SOLR_CORE);

        // 5.spell����
        getSpell(SOLR_CORE);

    }

    /**
     * ��ȡsolr����
     * 
     * @return
     */
    private static HttpSolrClient getSolrClient(String core) {
        HttpSolrClient hsc = new HttpSolrClient("http://192.168.21.130:8983/solr/person");
        return hsc;
    }

    /**
     * ����ĵ�
     * 
     * @param map
     * @param core
     * @throws Exception
     */
    private static void addDocument(Map<String, String> map, String core)
            throws Exception {
        SolrInputDocument sid = new SolrInputDocument();
        for (Entry<String, String> entry : map.entrySet()) {
            sid.addField(entry.getKey(), entry.getValue());
        }
        HttpSolrClient solrClient = getSolrClient("/" + core);
        solrClient.add(sid);
        commitAndCloseSolr(solrClient);
    }

    /**
     * ����ĵ���ͨ��bean��ʽ
     * 
     * @param persons
     * @param core
     * @throws Exception
     */
    private static void addDocumentByBean(List<Person> persons, String core)
            throws Exception {
        HttpSolrClient solrClient = getSolrClient("/" + core);
        solrClient.addBeans(persons);
        commitAndCloseSolr(solrClient);
    }

    /**
     * ����id����ɾ������
     * 
     * @param ids
     * @param core
     * @throws Exception
     */
    private static void deleteDocumentByIds(List<String> ids, String core)
            throws Exception {
        HttpSolrClient solrClient = getSolrClient("/" + core);
        solrClient.deleteById(ids);
        commitAndCloseSolr(solrClient);
    }

    private static void getDocument(String core) throws Exception {
        HttpSolrClient solrClient = getSolrClient("/" + core);
        SolrQuery sq = new SolrQuery();

        // q��ѯ
        sq.set("q", "id:00003");

        // filter��ѯ
        sq.addFilterQuery("id:[0 TO 00003]");

        // ����
        sq.setSort("id", SolrQuery.ORDER.asc);

        // ��ҳ �ӵ�0����ʼȡ��ȡһ��
        sq.setStart(0);
        sq.setRows(1);

        // ���ø���
        sq.setHighlight(true);

        // ���ø������ֶ�
        sq.addHighlightField("name");

        // ���ø�������ʽ
        sq.setHighlightSimplePre("<font color='red'>");
        sq.setHighlightSimplePost("</font>");

        QueryResponse result = solrClient.query(sq);

        // ������Դ�result��ò�ѯ����(���ַ�ʽ����)

        // 1.��ȡdocument����
        System.out.println("1.��ȡdocument����-------------------------");
        SolrDocumentList results = result.getResults();
        // ��ȡ��ѯ������
        System.out.println("һ����ѯ��" + results.getNumFound() + "����¼");
        for (SolrDocument solrDocument : results) {
            System.out.println("id:" + solrDocument.get("id"));
            System.out.println("name:" + solrDocument.get("name"));
            System.out.println("age:" + solrDocument.get("age"));
            System.out.println("addr:" + solrDocument.get("addr"));
        }

        // 2.��ȡ������Ϣ,��Ҫ�����Ӧ�������class
        System.out.println("2.��ȡ������Ϣ,��Ҫ�����Ӧ�������class-----------");
        List<Person> persons = result.getBeans(Person.class);
        System.out.println("һ����ѯ��" + persons.size() + "����¼");
        for (Person person : persons) {
            System.out.println(person);
        }
        commitAndCloseSolr(solrClient);
    }

    /**
     * ��ѯʹ��spell�ӿڣ��������solr���Ը��������
     * 
     * @param core
     * @throws Exception
     */
    private static void getSpell(String core) throws Exception {
        HttpSolrClient solrClient = getSolrClient("/" + core);
        SolrQuery sq = new SolrQuery();
        sq.set("qt", "/spell");

        // ԭ����lisi������ƴд���󣬲���solr���ؽ������
        sq.set("q", "liss");
        QueryResponse query = solrClient.query(sq);
        SolrDocumentList results = query.getResults();

        // ��ȡ��ѯ����
        long count = results.getNumFound();

        // �ж��Ƿ��ѯ��
        if (count == 0) {
            SpellCheckResponse spellCheckResponse = query
                    .getSpellCheckResponse();
            List<Collation> collatedResults = spellCheckResponse
                    .getCollatedResults();
            for (Collation collation : collatedResults) {
                long numberOfHits = collation.getNumberOfHits();
                System.out.println("��������Ϊ:" + numberOfHits);

                List<Correction> misspellingsAndCorrections = collation
                        .getMisspellingsAndCorrections();
                for (Correction correction : misspellingsAndCorrections) {
                    String source = correction.getOriginal();
                    String current = correction.getCorrection();
                    System.out.println("�Ƽ�����Ϊ��" + current + "ԭʼ������Ϊ��" + source);
                }
            }
        } else {
            for (SolrDocument solrDocument : results) {
                // ��ȡkey����
                Collection<String> fieldNames = solrDocument.getFieldNames();

                // ����key�������value
                for (String field : fieldNames) {
                    System.out.println("key: " + field + ",value: "
                            + solrDocument.get(field));
                }
            }
        }

        // �ر�����
        commitAndCloseSolr(solrClient);
    }

    /**
     * �ύ�Լ��رշ���
     * 
     * @param solrClient
     * @throws Exception
     */
    private static void commitAndCloseSolr(HttpSolrClient solrClient)
            throws Exception {
        solrClient.commit();
        solrClient.close();
    }

}