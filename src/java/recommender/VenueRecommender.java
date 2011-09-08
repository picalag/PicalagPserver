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
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLBooleanPrefJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import representations.recommendation.RecommendedVenue;

/**
 *
 * @author seb
 */
public class VenueRecommender {

    private VenueRecommender() {
    }

    /**
     * Recommends venues to user u based on it's favorite venues (collaborative filtering)
     * Uses an Item-Based CF system as the number of venues is not expected to vary a lot
     * @param u         user who needs recommendations
     * @param nb_recs   maximum number of recommendations
     * @return an ArrayList of recommended venues (that encapsulates the venue recommended and explanations)
     */
    public static ArrayList<RecommendedVenue> recommendVenues(User u, int nb_recs) {
        ArrayList<RecommendedVenue> recommendedVenues = new ArrayList<RecommendedVenue>();

        try {
            DataModel model;
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/picalag_pserver");
            model = new MySQLBooleanPrefJDBCDataModel(ds, "users_favorite_venues", "user_id", "venue_id", "");


            /*Specifies the Similarity algorithm*/
            ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(model);
            //ItemSimilarity itemSimilarity = new TanimotoCoefficientSimilarity(model);

            /*Initalizing the recommender */
            ItemBasedRecommender recommender = new GenericItemBasedRecommender(model, itemSimilarity);

            //calling the recommend method to generate recommendations
            List<RecommendedItem> recommendations = recommender.recommend((Integer) u.getId(), nb_recs);

            for (RecommendedItem r : recommendations) {
                RecommendedVenue rv = new RecommendedVenue((int) r.getItemID());
                List<RecommendedItem> because = recommender.recommendedBecause((Integer) u.getId(), r.getItemID(), 3);
                for(RecommendedItem b : because) {
                    rv.recommendedBecause.add((int) b.getItemID());
                }
                recommendedVenues.add(rv);
            }

        } catch (TasteException ex) {
            Logger.getLogger(VenueRecommender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(VenueRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }

        return recommendedVenues;
    }
}
