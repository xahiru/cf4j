package es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric;

import es.upm.etsisi.cf4j.data.User;

public class UWPIP extends UserSimilarityMetric {
	
	protected UW uw;
	protected PIP pip;
	protected int mixmode = 0;
	protected Jaccard jaccard;
	

	public UWPIP(UW uw, PIP pip, Jaccard jaccard, int mixmode) {
		super();
		this.uw = uw;
		this.pip = pip;
		this.jaccard = jaccard;
		this.mixmode = mixmode;
	}


	@Override
	public double similarity(User user, User otherUser) {
		double sim = 0;
		double suw = 0;
		double spip = 0;
		if (this.mixmode == 0) {
			 suw = this.uw.similarity(user, otherUser);
			 spip = this.pip.similarity(user, otherUser);
			 sim = suw + spip;
		}
		if (this.mixmode == 1) {
			 suw = this.uw.similarity(user, otherUser);
			 spip = this.jaccard.similarity(user, otherUser);
			 sim = suw + spip;
		}
		return sim;
	}

}
