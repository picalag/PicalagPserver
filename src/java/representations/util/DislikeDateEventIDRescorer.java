/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations.util;

import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import representations.Rating;
import representations.User;
import representations.Event;

/**
 * This class represents an IDRescorer that allows to filter which event should
 * be recommended, or not. It allows to filter the recommendations based on date
 * and previously disliked (or similar to disliked) events
 * @author seb
 */
public class DislikeDateEventIDRescorer implements IDRescorer {

    // filter threshold: maximal similarity with disliked events to be recommendable
    // TODO set this threshold correctly
    public static final double FILTER_THRESHOLD = 0.5;

    // no filter by default
    public FastIDSet enabledEventsID;
    public static final DislikeDateEventIDRescorer NO_RESCORING = new DislikeDateEventIDRescorer();

    public DislikeDateEventIDRescorer() {
        this.enabledEventsID = null;
    }

    public DislikeDateEventIDRescorer(FastIDSet enabledEventsID) {
        this.enabledEventsID = enabledEventsID;
    }

    public static DislikeDateEventIDRescorer generateFilter(User u, String date) {
        // default = no filter (can recommend any event)
        FastIDSet enabledEventsID = new FastIDSet();

        /* iterate on all rated events (others are not usefull for they will
         * not be recommended anyway by CF RS) taking place at the given date
         */
        //Event.findWith(new ModelListenerDislikeEventIDRescoringStream(enabledEventsID, u, FILTER_THRESHOLD), "date='" + date + "' AND id IN (SELECT DISTINCT event_id FROM ratings)");
        Event.findWith(new ModelListenerDislikeEventIDRescoringStream(enabledEventsID, u, FILTER_THRESHOLD), "date='" + date + "'");

        // TODO remove these lines after test
        if (enabledEventsID.isEmpty()) {
            System.err.println("EventIDRescorer Empty FastIDSet, might be an error");
        }

        return (enabledEventsID != null) ? new DislikeDateEventIDRescorer(enabledEventsID) : NO_RESCORING;
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
