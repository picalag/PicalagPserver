/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender;

import java.util.ArrayList;
import representations.util.HashMapTopNEvents;
import representations.Event;
import representations.util.ModelListenerEventDistanceStream;
import representations.util.ModelListenerEventToUserProfileDistanceStream;
import java.util.HashMap;
import representations.User;
import representations.recommendation.RecommendedEvent;

/**
 *
 * @author seb
 */
public class ContentBased {
    // TODO exclude if already known events

    // THRESHOLD is the minimal similarity to consider that events can be neighbours
    public static final double THRESHOLD = 0.1;

    private ContentBased() {
    }

    /**
     * This method recommends *nb_recs* events similar to event *e*, taking place the same day
     * @param e         events will be recommended if they are similar to this Event
     * @param nb_recs   this method will return the top nb_recs recommendations
     */
    public static ArrayList<RecommendedEvent> recommendationEventsSameDay(Event e, int nb_recs) {
        return recommendationEvents(e, e.get("date").toString(), nb_recs);
    }


    
    /**
     * Recommends *nb_recs* events similar to a given event e and taking place at date *date*
     * @param e         base event which we are looking for similar events
     * @param date      we are looking for recommended events taking place date *date*
     * @param nb_recs   maximum size for the list of recommendation
     * @return ArrayList of RecommendedEvents explained with their respective cosine distance to param event *e* (Double)
     */
    public static ArrayList<RecommendedEvent> recommendationEvents(Event e, String date, int nb_recs) {
        HashMapTopNEvents hm = new HashMapTopNEvents(nb_recs, THRESHOLD);

        // this line fills hm with top nb_recs events (<Event, Double> where double is the distance to the Event e)
        Event.find("date='" + date + "' AND id!=" + e.getId(), new ModelListenerEventDistanceStream(e, hm));

        ArrayList<RecommendedEvent> recommendedEvents = new ArrayList<RecommendedEvent>();

        for (Event key : hm.keySet()) {
            RecommendedEvent re = new RecommendedEvent((Integer) key.getId());
            re.dist = hm.get(key);
            recommendedEvents.add(re);
        }

        return recommendedEvents;
    }

    /**
     * Recommends *nb_recs* events similar to a given user's (*u*) profile and taking place at date *date*
     * @param u         user to whome events should be recommended
     * @param date      we are looking for recommended events taking place date *date*
     * @param nb_recs   maximum size for the list of recommendation
     * @return HashMap of Events with their respective cosine distance to param event *e* (Double)
     */
    public static ArrayList<RecommendedEvent> recommendationEventsToUserProfile(User u, String date, int nb_recs) {
        HashMapTopNEvents hm = new HashMapTopNEvents(nb_recs, THRESHOLD);

        // this line fills hm with top nb_recs events (<Event, Double> where double is the distance to the User u's profile)
        Event.find("date='" + date + "' AND id NOT IN (SELECT event_id FROM ratings WHERE user_id="+u.getId()+")", new ModelListenerEventToUserProfileDistanceStream(u, hm));
        


        ArrayList<RecommendedEvent> recommendedEvents = new ArrayList<RecommendedEvent>();

        for (Event key : hm.keySet()) {
            RecommendedEvent re = new RecommendedEvent((Integer) key.getId());
            re.dist = hm.get(key);
            recommendedEvents.add(re);
        }

        return recommendedEvents;
    }
}
