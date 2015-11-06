package dparish.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.Iterator;
import java.util.Map;

/**
 * @author dparish
 */
public class SparkTest {
    private static Logger logger = LoggerFactory.getLogger(SparkTest.class);

    @Test
    public void filterToNewElastic() {
        SparkConf conf = new SparkConf().setAppName("test").setMaster("local[2]");
        conf.set("es.index.auto.create", "true");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<Map<String, Object>> esRDD =
                JavaEsSpark.esRDD(sc, "restbpm/task").values();

        JavaRDD<Map<String, Object>> filtered = esRDD.filter(doc -> {
            Long id = (Long) doc.get("instanceId");
            if (id != null && id == 480l) {
                return true;
            }
            return false;
        });
        JavaEsSpark.saveToEs(filtered, "filtered/task");
        System.out.print("size:" + filtered.count());
    }

    @Test
    public void filterToPrint() {
        SparkConf conf = new SparkConf().setAppName("test").setMaster("local[2]");
        conf.set("es.index.auto.create", "true");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<Map<String, Object>> esRDD =
                JavaEsSpark.esRDD(sc, "restbpm/task").values();

        JavaRDD<Map<String, Object>> filtered = esRDD.filter(doc -> {
            Long id = (Long) doc.get("instanceId");
            if (id != null && id == 480l) {
                return true;
            }
            return false;
        });

        int i=0;
        Iterator<Map<String, Object>> mapIterator = filtered.toLocalIterator();
        while (mapIterator.hasNext()) {
            logger.info("found item " + ++i);
            Map<String, Object> next = mapIterator.next();
            dumpMap(next);
        }
    }

    @Test
    public void taskFrequency() {
        SparkConf conf = new SparkConf().setAppName("test").setMaster("local[2]");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<Map<String, Object>> esRDD =
                JavaEsSpark.esRDD(sc, "restbpm/task").values();
        JavaPairRDD<String, Integer> pairs = esRDD.mapToPair(s -> new Tuple2(s.get("name"), 1));
        JavaPairRDD<String, Integer> counts = pairs.reduceByKey((a, b) -> a + b);
        Iterator<Tuple2<String, Integer>> totals = counts.toLocalIterator();
        StringBuilder builder = new StringBuilder();
        while (totals.hasNext()) {
            Tuple2<String, Integer> task = totals.next();
            builder.append(task._1() + " : " + task._2() + "\n");
        }
        System.out.println("RESULTS:----------------");
        System.out.println(builder);
    }

    private void dumpMap(Map<String, Object> map) {
        for (Map.Entry entry : map.entrySet()) {
            logger.info(entry.getKey() + ": " + String.valueOf(entry.getValue()));
        }
    }

}
