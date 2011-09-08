/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations.util;

import java.util.HashSet;
import java.util.List;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.common.LongPair;
import representations.Event;

/**
 * This class represents an IDRescorer that allows to filter which event should
 * be recommended, or not. It allows to filter the recommendations based on date
 * @author seb
 */
public class EventToEventRecommendationDateEventIDRescorer implements Rescorer<LongPair> {

    // no filter by default
    public HashSet<LongPair> enabledEventsID;
    public static final EventToEventRecommendationDateEventIDRescorer NO_RESCORING = new EventToEventRecommendationDateEventIDRescorer();

    public EventToEventRecommendationDateEventIDRescorer() {
        this.enabledEventsID = null;
    }

    public EventToEventRecommendationDateEventIDRescorer(HashSet<LongPair> enabledEventsID) {
        this.enabledEventsID = enabledEventsID;
    }

    public static EventToEventRecommendationDateEventIDRescorer generateFilter(Event e, String date) {
        // default = no filter (can recommend any event)
        HashSet<LongPair> enabledEventsID = new HashSet<LongPair>();
        long e_Id = Long.parseLong(e.getId().toString());

        /* iterate on all rated events (others are not usefull for they will
         * not be recommended anyway by CF RS) taking place at the given date
         */
        List<Event> eventList = Event.find("date='" + date + "' AND id!=" + e_Id);
        for (Event currentEvent : eventList) {
            long ce_Id = Long.parseLong(currentEvent.getId().toString());
            enabledEventsID.add(new LongPair(e_Id, ce_Id));
            enabledEventsID.add(new LongPair(ce_Id, e_Id));
            //eventList.remove(currentEvent);
        }

        // TODO remove these lines after test
        if (enabledEventsID.isEmpty()) {
            System.err.println("EventIDRescorer Empty FastIDSet, might be an error");
        }

        return (enabledEventsID != null) ? new EventToEventRecommendationDateEventIDRescorer(enabledEventsID) : NO_RESCORING;
    }

    // TODO maybe use user's profile and favorite venues ?
    @Override
    public double rescore(LongPair eventsID, double originalScore) {
        return originalScore;
    }

    /**
     * returns true if the event should *NOT* be recommended to the user
     * @param eventID event ID
     * @return true if not in the list of recommendable events
     */
    @Override
    public boolean isFiltered(LongPair eventsID) {
        return (enabledEventsID != null && !enabledEventsID.contains(eventsID));
    }
}
