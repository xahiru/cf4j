package es.upm.etsisi.cf4j.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DataModelTest {
    final private static String serializedFilename = "src/test/resources/dataset.save";
    final private static String serializedResultString = "\n" +
            "Number of users: 4\n" +
            "Number of test users: 2\n" +
            "Number of items: 4\n" +
            "Number of test items: 2\n" +
            "Number of ratings: 11\n" +
            "Min rating: 0.0\n" +
            "Max rating: 4.0\n" +
            "Average rating: 2.4545454545454546\n" +
            "Number of test ratings: 4\n" +
            "Min test rating: 1.0\n" +
            "Max test rating: 2.0\n" +
            "Average test rating: 1.5";

    private static DataModel dataModel;

    @BeforeAll
    static void initAll(){
        dataModel = new DataModel(new MockDataSet());
    }

    @Test

    void findIndexes () {
        //Note: elements should be located following insertion order.
        //(taking into account that the test ones are inserted first)
        //Users
        assertEquals(0, dataModel.findUserIndex("Tim"));
        assertEquals(1, dataModel.findUserIndex("Kim"));
        assertEquals(2, dataModel.findUserIndex("Laurie"));
        assertEquals(3, dataModel.findUserIndex("Mike"));
        //Items
        assertEquals(0, dataModel.findItemIndex("Milk"));
        assertEquals(1, dataModel.findItemIndex("Potatoad"));
        assertEquals(2, dataModel.findItemIndex("Yeah,IsWired"));
        assertEquals(3, dataModel.findItemIndex("WiredThing"));
        //TestUsers
        assertEquals(0, dataModel.findTestUserIndex("Tim"));
        assertEquals(1, dataModel.findTestUserIndex("Kim"));
        //TestItems
        assertEquals(0, dataModel.findTestItemIndex("Milk"));
        assertEquals(1, dataModel.findTestItemIndex("Potatoad"));
    }

    @Test
    void numberOfRatings () {
        //Users
        assertEquals(4, dataModel.getNumberOfUsers());
        assertEquals(dataModel.getUsers().length, dataModel.getNumberOfItems());
        assertEquals(2, dataModel.getUser(dataModel.findUserIndex("Tim")).getNumberOfRatings());
        assertEquals(2, dataModel.getUser(dataModel.findUserIndex("Kim")).getNumberOfRatings());
        assertEquals(3, dataModel.getUser(dataModel.findUserIndex("Laurie")).getNumberOfRatings());
        assertEquals(4, dataModel.getUser(dataModel.findUserIndex("Mike")).getNumberOfRatings());
        //Items
        assertEquals(4, dataModel.getNumberOfItems());
        assertEquals(dataModel.getItems().length, dataModel.getNumberOfItems());
        assertEquals(2, dataModel.getItem(dataModel.findItemIndex("Milk")).getNumberOfRatings());
        assertEquals(2, dataModel.getItem(dataModel.findItemIndex("Potatoad")).getNumberOfRatings());
        assertEquals(3, dataModel.getItem(dataModel.findItemIndex("WiredThing")).getNumberOfRatings());
        assertEquals(4, dataModel.getItem(dataModel.findItemIndex("Yeah,IsWired")).getNumberOfRatings());
        //TestUsers
        assertEquals(2, dataModel.getNumberOfTestUsers());
        assertEquals(dataModel.getTestUsers().length, dataModel.getNumberOfTestUsers());
        assertEquals(2, dataModel.getTestUser(dataModel.findTestUserIndex("Tim")).getNumberOfRatings());
        assertEquals(2, dataModel.getTestUser(dataModel.findTestUserIndex("Kim")).getNumberOfRatings());
        //TestItems
        assertEquals(2, dataModel.getNumberOfTestItems());
        assertEquals(dataModel.getTestItems().length, dataModel.getNumberOfTestItems());
        assertEquals(2, dataModel.getTestItem(dataModel.findTestItemIndex("Milk")).getNumberOfRatings());
        assertEquals(2, dataModel.getTestItem(dataModel.findTestItemIndex("Potatoad")).getNumberOfRatings());
    }

    @Test
    void generalMetrics () {
        assertEquals(0, dataModel.getMinRating());
        assertEquals(4.0, dataModel.getMaxRating());
        assertTrue(Math.abs(dataModel.getRatingAverage() - 2.4545454545454546) <= Math.ulp(2.4545454545454546));

        assertEquals(1.0, dataModel.getMinTestRating());
        assertEquals(2.0, dataModel.getMaxTestRating());
        assertTrue(Math.abs(dataModel.getTestRatingAverage() - 1.5) <= Math.ulp(1.5) );
    }

    @Test
    void serializeMethods () throws IOException, ClassNotFoundException{
        dataModel.save(serializedFilename);
        DataModel auxDataModel = DataModel.load(serializedFilename);
        assertEquals( auxDataModel.toString(), serializedResultString);
    }
}