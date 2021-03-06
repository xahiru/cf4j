package es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric;

import es.upm.etsisi.cf4j.data.Item;
import es.upm.etsisi.cf4j.data.User;

/** Implements traditional Adjusted Cosine as CF similarity metric. */
public class AdjustedCosine extends UserSimilarityMetric {

  @Override
  public double similarity(User user, User otherUser) {

    int i = 0, j = 0, common = 0;
    double num = 0d, denActive = 0d, denTarget = 0d;

    while (i < user.getNumberOfRatings() && j < otherUser.getNumberOfRatings()) {
      if (user.getItemAt(i) < otherUser.getItemAt(j)) {
        i++;
      } else if (user.getItemAt(i) > otherUser.getItemAt(j)) {
        j++;
      } else {
        int itemIndex = user.getItemAt(i);
        Item item = super.datamodel.getItem(itemIndex);
        double avg = item.getRatingAverage();

        double fa = user.getRatingAt(i) - avg;
        double ft = otherUser.getRatingAt(j) - avg;

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
