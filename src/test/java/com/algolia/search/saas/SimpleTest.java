package com.algolia.search.saas;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SimpleTest {
    private static final String indexName = "test_java";

    private static APIClient client;
    private static Index index;

    @BeforeClass
    public static void init() {
        String applicationID = System.getenv("ALGOLIA_APPLICATION_ID");
        String apiKey = System.getenv("ALGOLIA_API_KEY");
        Assume.assumeFalse("You must set environement variables ALGOLIA_APPLICATION_ID and ALGOLIA_API_KEY to run the tests.", applicationID == null || apiKey == null);
        client = new APIClient(applicationID, apiKey);
        index = client.initIndex(indexName);
    }

    @Test
    public void test01_deleteIndexIfExists() {
        try {
            client.deleteIndex(indexName);
        } catch (AlgoliaException e) {
            // not fatal
        }
    }

    @Test
    public void test02_pushObject() throws AlgoliaException, JSONException {
        JSONObject obj = index.addObject(new JSONObject().put("i", 42).put("s", "foo").put("b", true));
        index.waitTask(obj.getString("taskID"));
    }

    @Test
    public void test03_search() throws AlgoliaException, JSONException {
        JSONObject res = index.search(new Query("foo"));
        assertEquals(1, res.getJSONArray("hits").length());
        assertEquals("foo", res.getJSONArray("hits").getJSONObject(0).getString("s"));
        assertEquals(42, res.getJSONArray("hits").getJSONObject(0).getLong("i"));
        assertEquals(true, res.getJSONArray("hits").getJSONObject(0).getBoolean("b"));
    }

    @Test
    public void test04_saveObject() throws AlgoliaException, JSONException {
        JSONObject res = index.search(new Query("foo"));
        assertEquals(1, res.getJSONArray("hits").length());
        res = index.saveObject(new JSONObject().put("s", "bar"), res.getJSONArray("hits").getJSONObject(0).getString("objectID"));
        index.waitTask(res.getString("taskID"));
    }

    @Test
    public void test05_searchUpdated() throws AlgoliaException, JSONException {
        JSONObject res = index.search(new Query("foo"));
        assertEquals(0, res.getJSONArray("hits").length());

        res = index.search(new Query("bar"));
        assertEquals(1, res.getJSONArray("hits").length());
        assertEquals("bar", res.getJSONArray("hits").getJSONObject(0).getString("s"));
    }

    @Test
    public void test06_searchAll() throws AlgoliaException, JSONException {
        JSONObject res = index.search(new Query(""));
        assertEquals(1, res.getJSONArray("hits").length());
        res = index.search(new Query("*"));
        assertEquals(1, res.getJSONArray("hits").length());
    }

}