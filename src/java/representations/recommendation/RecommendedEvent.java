/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package representations.recommendation;

import java.util.ArrayList;
import representations.Event;

/**
 *
 * @author seb
 */
public class RecommendedEvent {
    public int recommendedEventID;
    public ArrayList<Integer> recommendedBecause;
    public double dist;
    public int rating;

    public RecommendedEvent(int recommendedEventID) {
        this.recommendedEventID = recommendedEventID;
        this.recommendedBecause = new ArrayList<Integer>();
        this.dist = 0.0;
        this.rating = 0;
    }

    public Event getEvent() {
        return Event.findById(recommendedEventID);
    }

    public Double getDist() {
        return this.dist;
    }

    public Integer getRating() {
        return rating;
    }
}
