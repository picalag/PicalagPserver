package representations.util;

import java.util.Comparator;
import representations.recommendation.RecommendedEvent;

/**
 *
 * @author seb
 */
public class ComparatorDistRecommendedEvents implements Comparator<RecommendedEvent> {

    @Override
    public int compare(RecommendedEvent o1, RecommendedEvent o2) {
        return o1.getDist().compareTo(o2.getDist());
    }
}
