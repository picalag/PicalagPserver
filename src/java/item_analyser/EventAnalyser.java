/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package item_analyser;

import edu.northwestern.at.utils.corpuslinguistics.lemmatizer.DefaultLemmatizer;
import edu.northwestern.at.utils.corpuslinguistics.lemmatizer.Lemmatizer;
import edu.northwestern.at.utils.corpuslinguistics.tokenizer.DefaultWordTokenizer;
import edu.northwestern.at.utils.corpuslinguistics.tokenizer.WordTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import pserver.functions.CosineDistance;
import pserver.functions.StopWordsManager;
import pserver.functions.TfIdfManager;
import representations.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

/**
 * This class provides static methods to analyse events
 * @author seb
 */
public class EventAnalyser {

    private EventAnalyser() {
    }

    /**
     * Adds an event in picalag pserver.
     * This method analyses the event by generating the feature vectors (tf.idf ...)
     * @param id_event      calagator event id
     * @param title         event title
     * @param description   event description
     * @param venue         venue name
     * @param id_venue      calagator venue id
     * @param tags      category if known
     * @param date          date "yyyy-mm-dd hh:mm" or "yyyy-mm-dd" format if not correct format, will be transformed into 0000-01-01 and the event will probably not be recommended
     * @return true if no exception was raised
     */
    // TODO add tags support as boolean features
    public static boolean analyseEvent(
            int id_event,
            String title,
            String description,
            String venue,
            int id_venue,
            String tags,
            String date) {

        // if the event does not already exist
        if (Event.findFirstByCalagId(id_event) == null) {
            try {
                // clean description from usual (i.e. unimportant) words and ponctuation
                StopWordsManager swm = new StopWordsManager();
                // title counts double
                String cleanedEventDescription = swm.cleanText(title)
                        + " " + swm.cleanText(title)
                        + " " + swm.cleanText(description)
                        + " " + swm.cleanText(venue);


                // tokenize and lemmatize the cleaned description
                Lemmatizer l = new DefaultLemmatizer();
                WordTokenizer wt = new DefaultWordTokenizer();

                List<String> tokens = new ArrayList<String>();
                for (String w : wt.extractWords(cleanedEventDescription)) {
                    if (!swm.isStopWord(w)) {
                        tokens.add(l.lemmatize(w));
                    }
                }

                TfIdfManager tfIdfMgr = new TfIdfManager();
                tfIdfMgr.computeFromTokens(tokens);

                date = dateConversion(date);

                Event e = Event.createIt("id_calag", id_event, "date", date, "viewed", 0);
                Venue v = Venue.findOrCreateByCalagId(id_venue);
                v.add(e);

                e.createWeightedVector(tfIdfMgr.getWeightsMap(), tfIdfMgr.getFrequencyMap());
                e.saveIt();

                // try to infere new features for the event, based on existing events
                //enrichEventFeatures(e);

                if ((!tags.isEmpty()) || (tags != null)) {
                    String[] tags_array = tags.split(",");
                    for (String tag : tags_array) {
                        e.addBooleanFeature(tag.trim());
                    }
                }
                e.saveIt();

            } catch (Exception ex) {
                System.err.println(ex);
                return false;
            }
        }
        return true;
    }


    /*
     * This method convert a date of format 2011-04-16 20:00 to 2011-04-16
     * @param date  the date to convert
     * @return      the converted date
     */
    public static String dateConversion(String date) {
        if (date.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
            date = date.substring(0, 10);
        } else if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            // correct format
        } else {
            // the provided string is not a date of correct format...
            date = "0000-01-01";
        }
        return date;
    }

    /**
     * This method tries to infere new features for event *e* from previously
     * added events thanks to a SlopeOne recommender algorithm
     * (collaborative filtering exploiting item-feature weights matrix from DB)
     * @param e the event to enrich
     */
    public static void enrichEventFeatures(Event e) {
        try {
            DataModel model;
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/picalag_pserver");
            model = new MySQLJDBCDataModel(ds, "events_weighted_features", "event_id", "feature_id", "weight", "");


            if (model.getNumItems() != 0 && model.getNumUsers() != 0) {
                CachingRecommender cachingRecommender = new CachingRecommender(new SlopeOneRecommender(model));
                List<RecommendedItem> recommendations = cachingRecommender.recommend(Long.parseLong(e.getId().toString()), 5);

                for (RecommendedItem recommendedItem : recommendations) {
                    Feature f = Feature.findById(recommendedItem.getItemID());
                    // System.out.println(recommendedItem + "\t" + f.get("name"));
                    // enrichment weight
                    double weight = recommendedItem.getValue();
                    // add new value
                    e.addWeightedFeature((String) f.get("name"), weight, 0);
                    System.err.println("suggested feature: " + f.get("name"));
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(EventAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
