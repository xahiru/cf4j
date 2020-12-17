package es.upm.etsisi.cf4j.examples.plot;

import java.io.IOException;

import es.upm.etsisi.cf4j.data.DataModel;
import es.upm.etsisi.cf4j.data.GenreReader;
import es.upm.etsisi.cf4j.data.RandomSplitDataSet;
import es.upm.etsisi.cf4j.data.TestUser;
import es.upm.etsisi.cf4j.recommender.Recommender;
import es.upm.etsisi.cf4j.recommender.knn.UserKNN;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.UW;
import es.upm.etsisi.cf4j.util.plot.ScatterPlot;

/**
 * In this example we build an ScatterPlot comparing the number of ratings of
 * each test user with his/her averaged prediction error using BiasedMF as
 * recommender.
 */
public class ScatterPlotUW {

	public static void main(String[] args) throws IOException {

//		String datasetname = "ml-latest-small";
//
//		String filename = "/Users/xahiru/.surprise_data/ml-latest-small/ratingsnoheader.csv";
//		String gereItem_filename = "/Users/xahiru/.surprise_data/ml-latest-small/moviesnoheader.csv";
//		String separator = ",";
//		String genre_separator = "|";
//		String item_separator = separator;
		
		String datasetname = "ml-1M";
		String filename = "/Users/xahiru/.surprise_data/ml-1m/ml-1m/ratings.dat";
		String gereItem_filename = "/Users/xahiru/.surprise_data/ml-1m/ml-1m/movies.dat";
		String separator = "::";
		String genre_separator = "\\|";
		String item_separator = separator;
		
////		ml-100k
//		String datasetname = "ml-100k";
//		String filename = "/Users/xahiru/.surprise_data/ml-100k/ml-100k/u.data";
//		String gereItem_filename = "/Users/xahiru/.surprise_data/ml-100k/ml-100k/u.item";
//		String separator = "\t";
//		String genre_separator = "|";
//		String item_separator = genre_separator;

//		train-test split ratio
		long seed = 101;
		double testUsersPercent = 0.2;
		double testItemsPercent = 0.2;

		RandomSplitDataSet dtmDataset = new RandomSplitDataSet(filename, testUsersPercent, testItemsPercent, separator,
				seed);
		DataModel datamodel = new DataModel(dtmDataset);
		GenreReader gr = new GenreReader(gereItem_filename, item_separator, genre_separator);

		
		Recommender knn = new UserKNN(datamodel, 50, new UW(gr, datamodel), UserKNN.AggregationApproach.DEVIATION_FROM_MEAN);
		
		knn.fit();

		ScatterPlot plot = new ScatterPlot("Number of ratings", "Averaged user prediction error");

		for (TestUser testUser : datamodel.getTestUsers()) {
			double[] predictions = knn.predict(testUser);

			double sum = 0;

			for (int pos = 0; pos < testUser.getNumberOfTestRatings(); pos++) {
				double rating = testUser.getTestRatingAt(pos);
				double prediction = predictions[pos];
				sum += Math.pow(rating - prediction, 2);
			}

			double userError = sum / testUser.getNumberOfTestRatings();

			plot.addPoint(testUser.getNumberOfRatings(), userError);
		}

		plot.draw();
		plot.exportData("exports/"+datasetname+"knnuw-scatter-plot-data.csv");
		plot.printData("0", "0.00");
		plot.exportPlot("exports/"+datasetname+"knnuw-scatter-plot.png");
	}
}
