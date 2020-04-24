package es.upm.etsisi.cf4j.recommender.knn;

import es.upm.etsisi.cf4j.data.DataModel;
import es.upm.etsisi.cf4j.data.MockDataSet;
import es.upm.etsisi.cf4j.recommender.knn.itemSimilarityMetric.ItemSimilarityMetricMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemKNNTest {

    final private static int numberOfNeighbors = 2;

    final private static int testUserId = 1;
    final private static int testItemId = 1;

    private static DataModel datamodel;

    @BeforeAll
    static void initAll() {
        datamodel = new DataModel(new MockDataSet());
    }

    @Test
    void itemKNNTest() {

        //WEIGHTED_MEAN
        ItemKNN iKNN = new ItemKNN(datamodel,numberOfNeighbors,new ItemSimilarityMetricMock(),ItemKNN.AggregationApproach.WEIGHTED_MEAN);
        iKNN.fit();
        assertEquals(2.0, iKNN.predict(testUserId,testItemId));
        assertEquals(iKNN.predict(testUserId,testItemId), iKNN.predict(datamodel.getTestUser(testUserId))[testItemId]);

        //Mean
        iKNN = new ItemKNN(datamodel,numberOfNeighbors,new ItemSimilarityMetricMock(),ItemKNN.AggregationApproach.MEAN);
        iKNN.fit();
        assertEquals(2.0, iKNN.predict(testUserId,testItemId));
        assertEquals(iKNN.predict(testUserId,testItemId), iKNN.predict(datamodel.getTestUser(testUserId))[testItemId]);

    }
}
