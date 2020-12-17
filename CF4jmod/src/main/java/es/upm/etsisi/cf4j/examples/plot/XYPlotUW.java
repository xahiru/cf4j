package es.upm.etsisi.cf4j.examples.plot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.cf4j.data.DataModel;
import es.upm.etsisi.cf4j.data.GenreReader;
import es.upm.etsisi.cf4j.data.RandomSplitDataSet;
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.Precision;
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.Recall;
import es.upm.etsisi.cf4j.recommender.Recommender;
import es.upm.etsisi.cf4j.recommender.knn.UserKNN;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.CJMSD;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.Correlation;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.Cosine;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.Jaccard;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.MSD;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.PIP;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.UW;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.UserSimilarityMetric;
import es.upm.etsisi.cf4j.util.plot.XYPlot;

/**
 * In this example we compare the Precision score (y axis) and the Recall score
 * (x axis) for PMF and NMF recommenders using an XYPlot.
 */
public class XYPlotUW {

	public static void main(String[] args) throws IOException {
//    DataModel datamodel = BenchmarkDataModels.MovieLens100K();
//
		String datasetname = "ml-1M";
		String filename = "/Users/xahiru/.surprise_data/ml-1m/ml-1m/ratings.dat";
		String gereItem_filename = "/Users/xahiru/.surprise_data/ml-1m/ml-1m/movies.dat";
		String separator = "::";
		String genre_separator = "\\|";
		String item_separator = separator;
//
////		ml-100k
//		String datasetname = "ml-100k";
//		String filename = "/Users/xahiru/.surprise_data/ml-100k/ml-100k/u.data";
//		String gereItem_filename = "/Users/xahiru/.surprise_data/ml-100k/ml-100k/u.item";
//		String separator = "\t";
//		String genre_separator = "|";
//		String item_separator = genre_separator;

//		String datasetname = "ml-latest-small";
//
//		String filename = "/Users/xahiru/.surprise_data/ml-latest-small/ratingsnoheader.csv";
//		String gereItem_filename = "/Users/xahiru/.surprise_data/ml-latest-small/moviesnoheader.csv";
//		String separator = ",";
//		String genre_separator = "|";
//		;
//		String item_separator = separator;
//		

		// train-test split ratio
		long seed = 101;
		double testUsersPercent = 0.2;
		double testItemsPercent = 0.2;

		RandomSplitDataSet dtmDataset = new RandomSplitDataSet(filename, testUsersPercent, testItemsPercent, separator,
				seed);
		DataModel datamodel = new DataModel(dtmDataset);
		GenreReader gr = new GenreReader(gereItem_filename, item_separator, genre_separator);

		int[] numberOfRecommendations = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

		String[] labels = new String[numberOfRecommendations.length];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = String.valueOf(numberOfRecommendations[i]);
		}

		XYPlot plot = new XYPlot(labels, "Recall", "Precision");

		List<UserSimilarityMetric> metrics = new ArrayList<UserSimilarityMetric>();
//	    metrics.add(new AdjustedCosine());
		UW uw = new UW(gr, datamodel);
		metrics.add(uw);
		metrics.add(new CJMSD());
		metrics.add(new Correlation());
		metrics.add(new Cosine());
		Jaccard jaccard = new Jaccard();
		metrics.add(jaccard);
//	    metrics.add(new JMSD());
		metrics.add(new MSD());
		PIP pip = new PIP();
		metrics.add(pip);

		for (UserSimilarityMetric metric : metrics) {
			String metricName = metric.getClass().getSimpleName();

			Recommender knn = new UserKNN(datamodel, 50, metric, UserKNN.AggregationApproach.DEVIATION_FROM_MEAN);
			knn.fit();
			plot.addSeries(metricName);
			plot.setLabelsVisible(metricName);

			for (int N : numberOfRecommendations) {
				Precision precision = new Precision(knn, N, 4.0);
				double precisionScore = precision.getScore();

				Recall recall = new Recall(knn, N, 4.0);
				double recallScore = recall.getScore();

				plot.setXY(metricName, String.valueOf(N), precisionScore, recallScore);
			}
		}

		plot.draw();
		plot.exportData("exports/" + datasetname + "knnuw2-xy-plot-data.csv");
		plot.printData();
		plot.exportPlot("exports/" + datasetname + "knnuw2-xy-plot.png");
	}
}
