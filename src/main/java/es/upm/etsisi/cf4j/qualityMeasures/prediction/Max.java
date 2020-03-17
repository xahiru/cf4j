package es.upm.etsisi.cf4j.qualityMeasures.prediction;

import es.upm.etsisi.cf4j.data.TestUser;
import es.upm.etsisi.cf4j.qualityMeasures.QualityMeasure;
import es.upm.etsisi.cf4j.recommender.Recommender;

public class Max extends QualityMeasure {

	public Max(Recommender recommender) {
		super(recommender);
	}

	@Override
	public double getScore(TestUser testUser, double[] predictions) {

		double max = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < testUser.getNumberOfTestRatings(); i++) {
			if (!Double.isNaN(predictions[i])) {
				double error = Math.abs(predictions[i] - testUser.getTestRating(i));
				if (error > max) {
					max = error;
				}
			}
		}
		
		return (Double.isInfinite(max)) ? Double.NaN : max;
	}
}
