package es.upm.etsisi.cf4j.recommender.knn.userToUserMetrics;


import es.upm.etsisi.cf4j.data.DataModel;
import es.upm.etsisi.cf4j.data.User;

/**
 * Implements traditional Pearson Correlation Constrained as CF similarity metric.
 * 
 * @author Fernando Ortega
 */
public class CorrelationConstrained extends UserToUserMetric {

	/**
	 * Median of the ratings of the datamodel
	 */
	private double median;
	
	/**
	 * Constructor of the similarity metric
	 * @param median Median of the ratings of the dataset
	 */
	public CorrelationConstrained (DataModel datamodel, double[][] similarities, double median) {
		super(datamodel, similarities);
		this.median = median;
	}
	
	@Override
	public double similarity(User user, User otherUser) {

		int i = 0, j = 0, common = 0; 
		double num = 0d, denActive = 0d, denTarget = 0d;
		
		while (i < user.getNumberOfRatings() && j < otherUser.getNumberOfRatings()) {
			if (user.getItem(i) < otherUser.getItem(j)) {
				i++;
			} else if (user.getItem(i) > otherUser.getItem(j)) {
				j++;
			} else {
				double fa = user.getRatingAt(i) - this.median;
				double ft = otherUser.getRatingAt(j) - this.median;
				
				num += fa * ft;
				denActive += fa * fa;
				denTarget += ft * ft;
				
				common++;
				i++;
				j++;
			}	
		}

		// If there is not items in common, similarity does not exists
		if (common == 0) return Double.NEGATIVE_INFINITY;

		// Denominator can not be zero
		if (denActive == 0 || denTarget == 0) return Double.NEGATIVE_INFINITY;

		// Return similarity
		return num / Math.sqrt(denActive * denTarget);
	}
}
