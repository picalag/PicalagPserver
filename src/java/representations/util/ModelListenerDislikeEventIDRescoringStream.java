/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations.util;

import activejdbc.ModelListener;
import java.util.List;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import pserver.functions.CosineDistance;
import representations.Event;
import representations.Rating;
import representations.User;

/**
 *
 * @author seb
 */
public class ModelListenerDislikeEventIDRescoringStream implements ModelListener<Event> {

    public FastIDSet enabledEventsID;
    public User u;
    public double threshold;

    public ModelListenerDislikeEventIDRescoringStream(FastIDSet enabledEventsID, User u, double threshold) {
        this.enabledEventsID = enabledEventsID;
        this.u = u;
        this.threshold = threshold;
    }

    @Override
    public void onModel(Event e) {
        // test if the event should be recommended
        // NB: we already are sure that the date is correct, now we have to test
        // if this event is not too similar with user's dislikes
        List<Rating> dislikes = Rating.find("user_id=" + u.getId() + " AND rating < 0");
        double maxSim = 0;
        for (Rating r : dislikes) {
            Event dislikedEvent = Event.findById(r.get("event_id"));
            if (dislikedEvent != null) {
                double sim = CosineDistance.cosineSimilarity(e, dislikedEvent);
                if (sim > maxSim) {
                    maxSim = sim;
                }
            }
        }

        // the event is not too similar in comparison with disliked events
        // we can recommend it
        if(maxSim < this.threshold) {
            this.enabledEventsID.add(Long.parseLong(e.getId().toString()));
        }
    }
}
