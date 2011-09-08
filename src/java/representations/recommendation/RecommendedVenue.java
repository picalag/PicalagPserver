/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package representations.recommendation;

import java.util.ArrayList;
import representations.Venue;

/**
 * This class encapsulates the id of recommended venue and an arraylist of
 * venues used to generate the recommendation
 * @author seb
 */
public class RecommendedVenue {
    public int recommendedVenueID;
    public ArrayList<Integer> recommendedBecause;
    public int nb_fans;

    public RecommendedVenue(int recommendedVenueID) {
        this.recommendedVenueID = recommendedVenueID;
        this.recommendedBecause = new ArrayList<Integer>();
        this.nb_fans = 0;
    }

    public Venue getVenue() {
        return Venue.findById(recommendedVenueID);
    }

    public Integer getNbFans() {
        return nb_fans;
    }
}
