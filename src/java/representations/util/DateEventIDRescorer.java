/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations.util;

import java.util.List;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import representations.Rating;
import representations.User;
import representations.Event;

/**
 * This class represents an IDRescorer that allows to filter which event should
 * be recommended, or not. It allows to filter the recommendations based on date
 * @author seb
 */
public class DateEventIDRescorer implements IDRescorer {

    // no filter by default
    public FastIDSet enabledEventsID;
    public static final DateEventIDRescorer NO_RESCORING = new DateEventIDRescorer();

    public DateEventIDRescorer() {
        this.enabledEventsID = null;
    }

    public DateEventIDRescorer(FastIDSet enabledEventsID) {
        this.enabledEventsID = enabledEventsID;
    }

    public static DateEventIDRescorer generateFilter(String date) {
        return generateFilter(null, date);
    }

    public static DateEventIDRescorer generateFilter(Event e, String date) {
        // default = no filter (can recommend any event)
        FastIDSet enabledEventsID = new FastIDSet();

        /* iterate on all rated events (others are not usefull for they will
         * not be recommended anyway by CF RS) taking place at the given date
         */
        List<Event> eventList;
        if (e != null) {
            eventList = Event.find("date='" + date + "' AND id!=" + e.getId());
        } else {
            eventList = Event.find("date='" + date + "'");
        }
        for (Event currentEvent : eventList) {
            enabledEventsID.add(Long.parseLong(currentEvent.getId().toString()));
        }

        // TODO remove these lines after test
        if (enabledEventsID.isEmpty()) {
            System.err.println("EventIDRescorer Empty FastIDSet, might be an error");
        }

        return (enabledEventsID != null) ? new DateEventIDRescorer(enabledEventsID) : NO_RESCORING;
    }

    // TODO maybe use user's profile and favorite venues ?
    @Override
    public double rescore(long eventID, double originalScore) {
        return originalScore;
    }

    /**
     * returns true if the event should *NOT* be recommended to the user
     * @param eventID event ID
     * @return true if not in the list of recommendable events
     */
    @Override
    public boolean isFiltered(long eventID) {
        return (enabledEventsID != null && !enabledEventsID.contains(eventID));
    }
}
