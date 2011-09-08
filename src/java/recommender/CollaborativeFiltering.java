/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import org.apache.mahout.cf.taste.common.TasteException;
import representations.User;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import representations.Event;
import representations.Rating;
import representations.recommendation.RecommendedEvent;
import representations.util.DateEventIDRescorer;
import representations.util.EventToEventRecommendationDateEventIDRescorer;

/**
 *
 * @author seb
 */
public class CollaborativeFiltering {

    private CollaborativeFiltering() {
    }

    /**
     * Makes recommendations of events at date *date* for user *u*
     * @param u     user
     * @param date  date of events
     */
    public static ArrayList<RecommendedEvent> userBasedRecommendations(User u, String date, int nb_recs) {
        ArrayList<RecommendedEvent> recommendedEvents = new ArrayList<RecommendedEvent>();

        try {
            DataModel model;
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/picalag_pserver");
            model = new MySQLJDBCDataModel(ds, "ratings", "user_id", "event_id", "rating", "");
            UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(5, userSimilarity, model);
            // get user's neighborhood IDs
            long[] userNeighborhoodIds = neighborhood.getUserNeighborhood(Long.parseLong(u.getId().toString()));
            Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, userSimilarity);
            Recommender cachingRecommender = new CachingRecommender(recommender);

            DateEventIDRescorer dateIDRescorer = DateEventIDRescorer.generateFilter(date);

            List<RecommendedItem> recommendations = cachingRecommender.recommend(Long.parseLong(u.getId().toString()), nb_recs, dateIDRescorer);
            for (RecommendedItem recommendedItem : recommendations) {
                RecommendedEvent re = new RecommendedEvent((int) recommendedItem.getItemID());
                // try to get grades from neighborhood for this event
                for (long neighborId : userNeighborhoodIds) {
                    Rating r = Rating.findFirst("user_id=" + neighborId + " AND event_id=" + recommendedItem.getItemID());
                    if (r != null) {
                        re.recommendedBecause.add(r.getInteger("rating"));
                    }
                }

                recommendedEvents.add(re);
            }
        } catch (TasteException ex) {
            Logger.getLogger(CollaborativeFiltering.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(CollaborativeFiltering.class.getName()).log(Level.SEVERE, null, ex);
        }

        return recommendedEvents;
    }

    /**
     * Recommends max *nb_recs* events similar to a given event *e* according to users ratings, taking place at a given *date*
     * @param e         the event
     * @param date      date
     * @param nb_recs   maximum number of recommendations
     * @return a List of recommended events
     */
    public static ArrayList<RecommendedEvent> eventToEventRecommendations(Event e, String date, int nb_recs) {
        // see http://www.spicylogic.com/allenday/blog/2009/02/11/taste-item-item-recommender-example/
        ArrayList<RecommendedEvent> recommendedEvents = new ArrayList<RecommendedEvent>();
        try {
            DataModel model;
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/picalag_pserver");
            model = new MySQLJDBCDataModel(ds, "ratings", "user_id", "event_id", "rating", "");

            ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(model);
            ItemBasedRecommender itemRecommender = new GenericItemBasedRecommender(model, itemSimilarity);

            // filter by date (events similar taking place at the same date)
            EventToEventRecommendationDateEventIDRescorer idRescorer = EventToEventRecommendationDateEventIDRescorer.generateFilter(e, date);

            List<RecommendedItem> recommendations = itemRecommender.mostSimilarItems(Long.parseLong(e.getId().toString()), nb_recs, idRescorer);

            for (RecommendedItem recommendedItem : recommendations) {
                RecommendedEvent re = new RecommendedEvent((int) recommendedItem.getItemID());
                re.dist = itemSimilarity.itemSimilarity(Long.parseLong(e.getId().toString()), recommendedItem.getItemID());
                
                recommendedEvents.add(re);
            }

        } catch (TasteException ex) {
            Logger.getLogger(CollaborativeFiltering.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(CollaborativeFiltering.class.getName()).log(Level.SEVERE, null, ex);
        }

        return recommendedEvents;
    }

    /**
     * Recommends max *nb_recs* events similar to a given event *e* according to users ratings, taking place at the same date
     * @param e
     * @param nb_recs
     * @return a List of recommended events
     */
    public static ArrayList<RecommendedEvent> eventToEventRecommendationsSameDay(Event e, int nb_recs) {
        return eventToEventRecommendations(e, e.get("date").toString(), nb_recs);
    }
}
