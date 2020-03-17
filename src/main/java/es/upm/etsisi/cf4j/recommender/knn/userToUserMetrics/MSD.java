package es.upm.etsisi.cf4j.recommender.knn.userToUserMetrics;

import es.upm.etsisi.cf4j.data.DataModel;
import es.upm.etsisi.cf4j.data.User;

/**
 * Implements traditional MSD as CF similarity metric. The returned value is 1 - MSD.
 * 
 * @author Fernando Ortega
 */
public class MSD extends UserToUserMetric {

	/**
	 * Maximum difference between the ratings
	 */
	private double maxDiff;

	public MSD(DataModel datamodel, double[][] similarities) {
		super(datamodel, similarities);
		this.maxDiff = super.datamodel.getMaxRating() - super.datamodel.getMinRating();
	}
	
	@Override
	public double similarity(User user, User otherUser) {

		int i = 0, j = 0, common = 0; 
		double msd = 0d;
		
		while (i < user.getNumberOfRatings() && j < otherUser.getNumberOfRatings()) {
			if (user.getItem(i) < otherUser.getItem(j)) {
				i++;
			} else if (user.getItem(i) > otherUser.getItem(j)) {
				j++;
			} else {
				double diff = (user.getRatingAt(i) - otherUser.getRatingAt(j)) / this.maxDiff;
				msd += diff * diff;				
				
				common++;
				i++; 
				j++;
			}	
		}

		// If there is not items in common, similarity does not exists
		if (common == 0) return Double.NEGATIVE_INFINITY;

		// Return similarity
		return 1d - (msd / common);
	}
}
