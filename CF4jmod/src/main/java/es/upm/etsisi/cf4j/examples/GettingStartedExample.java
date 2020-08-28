package es.upm.etsisi.cf4j.examples;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.cf4j.data.DataModel;
import es.upm.etsisi.cf4j.data.GenreReader;
import es.upm.etsisi.cf4j.data.RandomSplitDataSet;
import es.upm.etsisi.cf4j.qualityMeasure.QualityMeasure;
import es.upm.etsisi.cf4j.qualityMeasure.prediction.Coverage;
import es.upm.etsisi.cf4j.qualityMeasure.prediction.MAE;
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.Precision;
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.Recall;
import es.upm.etsisi.cf4j.recommender.Recommender;
//import es.upm.etsisi.cf4j.recommender.knn.UserCat;
import es.upm.etsisi.cf4j.recommender.knn.UserKNN;
//import es.upm.etsisi.cf4j.recommender.knn.UserCat;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.AdjustedCosine;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.CJMSD;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.Correlation;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.Cosine;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.Jaccard;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.MSD;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.PIP;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.Singularities;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.SpearmanRank;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.UW;
import es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric.UserSimilarityMetric;
import es.upm.etsisi.cf4j.util.Range;
import es.upm.etsisi.cf4j.util.plot.LinePlot;

/**
 * In this example we analyze how the Mean Squared Error (MSE) varies according
 * to the value of the regularization term in Probabilistic Matrix Factorization
 * (PMF). This is the example included in the Getting Started section of the
 * readme.md.
 */
public class GettingStartedExample {
	private static final int[] numNeighbors = Range.ofIntegers(100, 50, 5);
	private static final es.upm.etsisi.cf4j.recommender.knn.UserKNN.AggregationApproach AGGREGATION_APPROACH =
			  UserKNN.AggregationApproach.DEVIATION_FROM_MEAN;
	public static void main(String[] args) throws IOException {

//    DataModel datamodel = BenchmarkDataModels.MovieLens100K();
		
//		//ml-1M
//		String filename = "/Users/xahiru/.surprise_data/ml-1m/ml-1m/ratings.dat";
//		String gereItem_filename = "/Users/xahiru/.surprise_data/ml-1m/ml-1m/movies.dat";
//		String separator = "::";
//		String genre_separator = "\\|";
//		String item_separator = separator;
		
		//ml-100k
//		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
//		InputStream filename = classloader.getResourceAsStream("/datasets/ml100k.data");
		String filename = "/Users/xahiru/.surprise_data/ml-100k/ml-100k/u.data";
		String gereItem_filename = "/Users/xahiru/.surprise_data/ml-100k/ml-100k/u.item";
		String separator = "\t";
		String genre_separator = "|";
		String item_separator = genre_separator;
		
		
		//train-test split ratio
		long seed = 101;
		double testUsersPercent = 0.2;
		double testItemsPercent = 0.2;
				
	  RandomSplitDataSet dtmDataset = new RandomSplitDataSet(filename, testUsersPercent, testItemsPercent, separator, seed);
//	  for (Iterator<DataSetEntry> it =  dtmDataset.getRatingsIterator(); it.hasNext();) {
//		  DataSetEntry entry = it.next();
//		  System.out.println("Ratings");
//		  System.out.println(entry.rating);
//	}

	  
    double[] regValues = {
      0.000, 0.025, 0.05, 0.075, 0.100, 0.125, 0.150, 0.175, 0.200, 0.225, 0.250
    };
    LinePlot plot = new LinePlot(regValues, "regularization", "MSE");

    plot.addSeries("PMF");
    
    DataModel datamodel = new DataModel(dtmDataset);
	GenreReader gr = new GenreReader(gereItem_filename, item_separator, genre_separator);
	
	  
	System.out.println("\nminimum rating ===========");
	
	System.out.println(datamodel.getItem(1).getId());
	System.out.println(datamodel.getMaxRating());
	System.out.println(datamodel.getMinRating());
	System.out.println(datamodel.getMinTestRating());
	System.out.println(datamodel.getMaxTestRating());
	System.out.println(datamodel.getNumberOfUsers());
	System.out.println(datamodel.getNumberOfItems());
	System.out.println(datamodel.getNumberOfTestUsers());
	System.out.println(datamodel.getNumberOfTestItems());
	System.out.println(datamodel.getNumberOfRatings());
	System.out.println(datamodel.getNumberOfTestRatings());


    subKKN(datamodel, gr);
  }
public static void  subKKN(DataModel datamodel, GenreReader gr) {
		
		// To store results
	    LinePlot maePlot = new LinePlot(numNeighbors, "Number of neighbors", "MAE");
	    LinePlot coveragePlot = new LinePlot(numNeighbors, "Number of neighbors", "Coverage");
	    LinePlot precisionPlot = new LinePlot(numNeighbors, "Number of neighbors", "Precision");
	    LinePlot recallPlot = new LinePlot(numNeighbors, "Number of neighbors", "Recall");

	    // Create similarity metrics
	    List<UserSimilarityMetric> metrics = new ArrayList<UserSimilarityMetric>();
	    metrics.add(new AdjustedCosine());
//	    userWieghts.similarity(datamodel.getUser(0), datamodel.getUser(0));
	    metrics.add(new UW(gr, datamodel));
	    metrics.add(new CJMSD());
	    metrics.add(new Correlation());
	    metrics.add(new Cosine());
	    metrics.add(new Jaccard());
//	    metrics.add(new JMSD());
	    metrics.add(new MSD());
	    metrics.add(new PIP());
//	    metrics.add(new Singularities(new double[] {3, 4, 5}, new double[] {1, 2}));
//	    metrics.add(new SpearmanRank());

	    // Evaluate UserKNN recommender
	    for (UserSimilarityMetric metric : metrics) {
	      String metricName = metric.getClass().getSimpleName();

	      maePlot.addSeries(metricName);
	      coveragePlot.addSeries(metricName);
	      precisionPlot.addSeries(metricName);
	      recallPlot.addSeries(metricName);

	      for (int k : numNeighbors) {
	        Recommender knn = new UserKNN(datamodel, k, metric, AGGREGATION_APPROACH);
//	        Recommender knn = new UserCat(datamodel, k, metric, AGGREGATION_APPROACH);
	        knn.fit();

	        QualityMeasure mae = new MAE(knn);
	        double maeScore = mae.getScore();
	        maePlot.setValue(metricName, k, maeScore);

	        QualityMeasure coverage = new Coverage(knn);
	        double coverageScore = coverage.getScore();
	        coveragePlot.setValue(metricName, k, coverageScore);

	        QualityMeasure precision = new Precision(knn, 10, 4);
	        double precisionScore = precision.getScore();
	        precisionPlot.setValue(metricName, k, precisionScore);

	        QualityMeasure recall = new Recall(knn, 10, 4);
	        double recallScore = recall.getScore();
	        recallPlot.setValue(metricName, k, recallScore);
	      }
	    }

	    // Print results
	    maePlot.printData("0", "0.0000");
	    coveragePlot.printData("0", "0.0000");
	    precisionPlot.printData("0", "0.0000");
	    recallPlot.printData("0", "0.0000");
	    maePlot.draw();
	    coveragePlot.draw();
	    precisionPlot.draw();
	    recallPlot.draw();
	  }
}
