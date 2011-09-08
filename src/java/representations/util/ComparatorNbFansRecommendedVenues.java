package representations.util;

import java.util.Comparator;
import representations.recommendation.RecommendedVenue;

/**
 *
 * @author seb
 */
public class ComparatorNbFansRecommendedVenues implements Comparator<RecommendedVenue> {

    @Override
    public int compare(RecommendedVenue o1, RecommendedVenue o2) {
        return o1.getNbFans().compareTo(o2.getNbFans());
    }
}
