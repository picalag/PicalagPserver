/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations;

import activejdbc.Base;
import activejdbc.Model;
import item_analyser.EventAnalyser;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author seb
 */
public class User extends Model {

    /**
     * This method adds or updates a user, based on his calagator ID
     * @param id_user calagator user's id
     * @param male    true if male, false if female
     * @param dob     date of birth
     * @return true if success
     */
    public static boolean addOrUpdateUser(int id_user, boolean male, String dob) {
        try {

            User u = User.findFirst("id_calag=" + id_user);
            if (u == null) {
                u = User.createIt("id_calag", id_user, "viewed", 0);
            }
            u.set("male", male);

            dob = EventAnalyser.dateConversion(dob);
            Date dateOfBirth = Date.valueOf(dob);
            u.setDate("dob", dateOfBirth);

            u.saveIt();
        } catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
        return true;
    }

    public int getRating(Event e) {
        return Rating.getRating(this, e);
    }

    /**
     * This method updates user's profile (i.e. features vector)
     * Formalism:
     * W'u = (Nu*Wu + We)/(Nu+1)
     * where:
     *      W'u = the new user's vector
     *      Wu  = the old user's vector
     *      We  = event's features vector
     *      Nu  = the number of events the user checked before the call (i.e. before calling this.view() to increase Nu)
     *
     * It also updates event's feature vector similarly
     * W'e = (Ne*We + Wu)/(Ne+1)
     *
     * @param e     the viewed event
     * @return true if no exception is raised
     */
    public boolean viewEventUpdateProfileAndEventVector(Event e) {
        try {
            double rating = this.getRating(e);
            if (rating <= Rating.NEUTRAL) {
                this.rateEvent(e, Rating.VIEWED);
                rating = Rating.VIEWED;
            }
            if (rating == Rating.NEUTRAL) {
                rating = 1;
            } else if (rating == Rating.ADDED) {
                rating = 1.5;
            } else if ((rating == Rating.LIKED) || (rating == Rating.SHARED)) {
                rating = 2;
            }
            double view_u = this.view() - 1; // view_u = Nu
            double view_e = e.view() - 1;  // view_e = Ne

            // get event's vector We
            HashMap<Integer, Double> eventWeigtedFeaturesVector = e.getWeightedFeaturesHashMap();
            // transform boolean features into weighted features (having average weight)
            HashSet<Integer> eventBooleanFeaturesVector = e.getBooleanFeaturesHashSet();
            double avg_weight = 0;
            try {
                Connection dbConnection = Base.connection();
                Statement statement = dbConnection.createStatement();

                ResultSet rs = statement.executeQuery("SELECT AVG(weight) AS avg FROM  events_weighted_features WHERE event_id =" + e.getId());
                rs.next();
                avg_weight = rs.getDouble(1);
                rs.close();
                statement.close();
            } catch (SQLException ex) {
                System.err.println(ex);
                avg_weight = 0;
            }
            if (avg_weight > 0) {
                for (int ebf : eventBooleanFeaturesVector) {
                    if (!eventWeigtedFeaturesVector.containsKey(ebf)) {
                        eventWeigtedFeaturesVector.put(ebf, avg_weight);
                    } else {
                        if ((Double) eventWeigtedFeaturesVector.get(ebf) < avg_weight) {
                            eventWeigtedFeaturesVector.put(ebf, avg_weight);
                        }
                    }
                }
            }
            // get old user's vector Wu
            HashMap<Integer, Double> userWeigtedFeaturesVector = this.getWeightedFeaturesHashMap();


            // update user profile vector
            for (int wei_id : eventWeigtedFeaturesVector.keySet()) {
                double wu = 0;
                double we = eventWeigtedFeaturesVector.get(wei_id);
                if (userWeigtedFeaturesVector.containsKey(wei_id)) {
                    wu = userWeigtedFeaturesVector.get(wei_id);
                }
                double weight = (view_u * wu + rating * we) / (view_u + 1);
                this.updateOrAddExistingWeightedFeature(wei_id, weight);
            }
            for (int wui_id : userWeigtedFeaturesVector.keySet()) {
                double wu = userWeigtedFeaturesVector.get(wui_id);
                if (eventWeigtedFeaturesVector.containsKey(wui_id)) {
                    // already treated
                } else {
                    double weight = (view_u * wu) / (view_u + 1);
                    this.updateOrAddExistingWeightedFeature(wui_id, weight);
                }
            }
            // update user vector with the new profile
            userWeigtedFeaturesVector = this.getWeightedFeaturesHashMap();
            // we are sure that every features in We are also in Wu as we just added them in Wu
            // that is to say that Wu has at least as many features as We
            for (int wui_id : userWeigtedFeaturesVector.keySet()) {
                double wu = userWeigtedFeaturesVector.get(wui_id);
                double we = 0;
                if (eventWeigtedFeaturesVector.containsKey(wui_id)) {
                    we = eventWeigtedFeaturesVector.get(wui_id);
                }
                double weight = (view_e * we + wu) / (view_e + 1);
                e.updateOrAddExistingWeightedFeature(wui_id, weight, 0);
            }

//            for (int wei_id : eventWeigtedFeaturesVector.keySet()) {
//                double new_weight_e = (view_e - 1) * eventWeigtedFeaturesVector.get(wei_id);
//
//                if (userWeigtedFeaturesVector.containsKey(wei_id)) {
//                    // Wu also contains this feature
//                    new_weight_e += userWeigtedFeaturesVector.get(wei_id);
//                } else {
//                    // We is the only vector that contains the feature
//                    double new_weight_u = eventWeigtedFeaturesVector.get(wei_id) / view_u;
//                    this.addExistingWeightedFeature(wei_id, new_weight_u);
//                }
//                new_weight_e /= view_e;
//                e.updateOrAddExistingWeightedFeature(wei_id, new_weight_e, 0);
//            }
//            for (int wui_id : userWeigtedFeaturesVector.keySet()) {
//                double new_weight_u = (view_u - 1) * userWeigtedFeaturesVector.get(wui_id);
//
//                if (eventWeigtedFeaturesVector.containsKey(wui_id)) {
//                    // We also contains this feature
//                    new_weight_u += eventWeigtedFeaturesVector.get(wui_id);
//                } else {
//                    // Wu is the only vector that contains the feature
//                    double new_weight_e = userWeigtedFeaturesVector.get(wui_id) / view_e;
//                    e.addExistingWeightedFeature(wui_id, new_weight_e, 0);
//                }
//                new_weight_u /= view_u;
//                this.updateOrAddExistingWeightedFeature(wui_id, new_weight_u);
//            }

        } catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
        return true;
    }

    /**
     * Add a viewed event to counter
     */
    public int view() {
        int view = this.getInteger("viewed");
        this.set("viewed", view + 1);
        this.saveIt();
        return view + 1;
    }

    /**
     * Adds a weighted feature entry in the database for this user
     * @param term      the feature name
     * @param weight    corresponding weight
     */
    public void addWeightedFeature(String term, double weight) {
        Feature feature = Feature.findOrCreateItByName(term);
        UserWeightedFeature uwf = UserWeightedFeature.createIt();
        uwf.set("weight", weight);
        uwf.saveIt();
        this.add(uwf);
        feature.add(uwf);
        uwf.saveIt();
    }

    /**
     * Adds an existing weighted feature entry in the database for this user
     * @param feature_id    the feature id
     * @param weight        corresponding weight
     */
    public void addExistingWeightedFeature(int feature_id, double weight) {
        Feature feature = Feature.findById(feature_id);
        UserWeightedFeature uwf = UserWeightedFeature.createIt();
        uwf.set("weight", weight);
        uwf.saveIt();
        this.add(uwf);
        feature.add(uwf);
        uwf.saveIt();
    }

    /**
     * Adds a new weighted feature entry if the feature does not already exist as a weighted feature
     * or updates weight and frequency if it does
     * @param term      the feature name
     * @param weight    corresponding weight
     */
    public void updateOrCreateWeightedFeature(String term, double weight) {
        Feature feature = Feature.findOrCreateItByName(term);
        UserWeightedFeature uwf = UserWeightedFeature.findFirst("user_id = " + this.getId() + " AND feature_id = " + feature.getId());
        if (uwf == null) {
            addWeightedFeature(term, weight);
        } else {
            uwf.set("weight", weight);
            uwf.saveIt();
        }
    }

    /**
     * Adds a new weighted feature entry if the feature does not already exist as a weighted feature
     * or updates weight and frequency if it does
     * @param feature_id    the feature id
     * @param weight        corresponding weight
     */
    public void updateOrAddExistingWeightedFeature(int feature_id, double weight) {
        UserWeightedFeature uwf = UserWeightedFeature.findFirst("user_id = " + this.getId() + " AND feature_id = " + feature_id);
        if (uwf == null) {
            this.addExistingWeightedFeature(feature_id, weight);
        } else {
            uwf.set("weight", weight);
            uwf.saveIt();
        }
    }

    /**
     * getWeightedFeaturesHashMap
     * @return an HashMap that maps the weighted features id (Integer) to their respective weights (Double)
     */
    public HashMap<Integer, Double> getWeightedFeaturesHashMap() {
        HashMap<Integer, Double> hm = new HashMap<Integer, Double>();

        List<UserWeightedFeature> ewf_list = this.getAll(UserWeightedFeature.class);
        for (UserWeightedFeature wf : ewf_list) {
            hm.put((Integer) wf.get("feature_id"), wf.getDouble("weight"));
        }
        return hm;
    }

    /**
     * Decrease weight of weighted features of event *e* from user's profile
     * @param e event to delete from user's profile
     * @return true if job is done
     */
    public boolean neverAgain(Event e) {
        try {
            // get event's weighted features vector
            List<EventWeightedFeature> ewf_list = e.getWeightedFeaturesList();
            for (EventWeightedFeature ewf : ewf_list) {
                UserWeightedFeature uwf = UserWeightedFeature.findFirst("user_id=" + this.getId() + " AND feature_id=" + ewf.get("feature_id"));
                if (uwf != null) {
                    // decrease weight
                    double coeff = e.getDouble("viewed") / this.getDouble("viewed");
                    if (coeff > 1) {
                        coeff = 1;
                    }
                    double new_weight = Double.parseDouble(uwf.get("weight").toString()) - 2 * coeff * Double.parseDouble(ewf.get("weight").toString());
                    if (ewf.getDouble("frequency") > 0) {
                        new_weight = new_weight / 2.0;
                    }
                    if (new_weight <= 0) {
                        uwf.delete();
                    } else {
                        uwf.set("weight", new_weight);
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
        return true;
    }

    /**
     * Fetches the event *event_id* from db then call neverAgain(Event e) if the event actually exists
     * @param event_id ID of the event
     * @return true if job is done
     */
    public boolean neverAgain(int event_id) {
        Event e = Event.findById(event_id);
        boolean done = false;
        if (e != null) {
            done = this.neverAgain(e);
        }
        return done;
    }

    /**
     * Rates an event
     * @param e         the event to rate
     * @param rating    the rating, can be picked up in the Rating.* final static int
     * @return true if success
     */
    public boolean rateEvent(Event e, int rating) {
        try {
            Rating.rateIt(this, e, rating);
        } catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
        return true;
    }

    /**
     * Adds a venue to favorite venues list
     * @param v Venue
     * @return true if no exception is raised
     */
    public boolean addVenueToFavorite(Venue v) {
        try {
            UserFavoriteVenue.addToFavorite(this, v);
        } catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
        return true;
    }

    /**
     * Adds a venue to favorite venues list, creates the venue if does not exist in DB
     * @param venue_id_calag calagator Venue's id
     */
    public void addVenueToFavorite(int venue_id_calag) {
        Venue v = Venue.findOrCreateByCalagId(venue_id_calag);
        UserFavoriteVenue.addToFavorite(this, v);
    }

    /**
     * Removes a venue from user's favorite venues list
     * @param v venue
     * @return true if no exception raised
     */
    public boolean removeVenueFromFavorite(Venue v) {
        try {
            UserFavoriteVenue.removeFromFavorite(this, v);
        } catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
        return true;
    }

    /**
     * Returns a User using an id_calag
     * @param id    User ID in calagator
     * @return  the user if it exists (null otherwise)
     */
    public static User findFirstByCalagId(int id) {
        return User.findFirst("id_calag = ?", id);
    }

    /**
     * get the user's favorite venues list
     * @return an ArrayList of user's favorite venues
     */
    public ArrayList<Venue> getFavoriteVenuesList() {
        return UserFavoriteVenue.getUserFavoriteVenuesList(this);
    }
}
