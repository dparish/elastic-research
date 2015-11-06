package dparish.elastic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import dparish.model.ICD;
import dparish.model.Medicine;
import dparish.model.Task;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.junit.Assert.assertTrue;

/**
 * @author dparish
 */
public class ElasticTest {

    private ObjectMapper mapper = new ObjectMapper();
    private Node node;

    private static final Logger logger = LoggerFactory.getLogger(ElasticTest.class);

    @Before
    public void open() {
        node = nodeBuilder().clusterName("dparish.elasticsearch").client(true).node();
    }

    @After
    public void close() {
        if (node != null) {
            node.close();
        }
    }

    @Test
    public void joinTest() throws IOException {
        // Embedded node clients behave just like standalone nodes,
        // which means that they will leave the HTTP port open!
        Client client = node.client();
        List<ICD> icdList = getICD();
        assertTrue(icdList.size() > 5);
        client.prepareIndex("medical", "icd").setSource(mapper.writeValueAsString(icdList.get(0))).execute().actionGet();
    }

    @Test
    public void deleteIndex() {
        Client client = node.client();
        client.admin().indices().prepareDelete("medical").execute().actionGet();
    }

    @Test
    public void insertMeds() throws IOException {
        Client client = node.client();

        // update the mapping for icd first
        CreateIndexRequestBuilder requestBuilder = client.admin().indices().prepareCreate("medical");

        URL resource = this.getClass().getResource("/icd_mapping.json");
        String text = Resources.toString(resource, StandardCharsets.UTF_8);

        requestBuilder.addMapping("icd", text);
        requestBuilder.execute().actionGet();
        BulkRequestBuilder builder = client.prepareBulk();
        List<ICD> icdList = getICD();
        for (ICD icd : icdList) {
            builder.add(
                    client.prepareIndex("medical", "icd")
                    .setSource(mapper.writeValueAsBytes(icd))
            );
        }
        builder.execute().actionGet();

        builder = client.prepareBulk();
        for (Medicine med : getMedicine()) {
            builder.add(
                    client.prepareIndex("medical", "medicine")
                            .setSource(mapper.writeValueAsBytes(med))
            );
        }
        builder.execute().actionGet();
    }

    @Test
    public void insertTasks() throws IOException {
        Client client = node.client();

        // update the mapping for icd first
        CreateIndexRequestBuilder requestBuilder = client.admin().indices().prepareCreate("restbpm");

        URL resource = this.getClass().getResource("/task_mapping.json");
        String text = Resources.toString(resource, StandardCharsets.UTF_8);

        requestBuilder.addMapping("task", text);
        requestBuilder.execute().actionGet();
        BulkRequestBuilder builder = client.prepareBulk();
        logger.info("start get tasks");
        List<Task> tasks = getTasks();
        logger.info("end get tasks");

        logger.info("start add tasks");
        for (Task task : tasks) {
            builder.add(
                    client.prepareIndex("restbpm", "task")
                            .setSource(mapper.writeValueAsBytes(task))
            );
        }
        builder.execute().actionGet();
        logger.info("end add tasks");

    }

    private List<ICD> getICD() throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream("/icd.json")) {
            List<ICD> icdList = mapper.readValue(is, new TypeReference<List<ICD>>(){});
            return icdList;
        }
    }

    private List<Medicine> getMedicine() throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream("/medicine.json")) {
            List<Medicine> medicines = mapper.readValue(is, new TypeReference<List<Medicine>>(){});
            return medicines;
        }
    }

    private List<Task> getTasks() throws IOException {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(isoFormat);
        try (InputStream is = this.getClass().getResourceAsStream("/taskdata.json")) {
            return mapper.readValue(is, new TypeReference<List<Task>>(){});
        }
    }

}
