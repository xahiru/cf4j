package es.upm.etsisi.cf4j.examples.plotting;

import es.upm.etsisi.cf4j.data.BenchmarkDataModels;
import es.upm.etsisi.cf4j.data.DataModel;
import es.upm.etsisi.cf4j.plot.LinePlot;
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.F1;
import es.upm.etsisi.cf4j.recommender.matrixFactorization.NMF;
import es.upm.etsisi.cf4j.recommender.matrixFactorization.PMF;

import java.io.IOException;

public class LinePlotExample {

    public static void main(String[] args) throws IOException  {
        DataModel datamodel = BenchmarkDataModels.MovieLens1M();

        int[] numberOfRecommendations = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        LinePlot plot = new LinePlot(numberOfRecommendations, "Number of recommendations", "F1");

        PMF pmf = new PMF(datamodel,10, 50, 43);
        pmf.fit();
        plot.addSeries("PMF");

        for (int N : numberOfRecommendations) {
            F1 f1 = new F1(pmf, N, 4.0);
            double score = f1.getScore();
            plot.setValue("PMF", N, score);
        }

        NMF nmf = new NMF(datamodel, 10, 50, 43);
        nmf.fit();
        plot.addSeries("NMF");

        for (int N : numberOfRecommendations) {
            F1 f1 = new F1(nmf, N, 4.0);
            double score = f1.getScore();
            plot.setValue("NMF", N, score);

        }

        plot.draw();
        plot.exportPlot("exports/line-plot.png");
        plot.printData("0", "0.000");
        plot.exportData("exports/line-plot-data.csv");
    }
}
