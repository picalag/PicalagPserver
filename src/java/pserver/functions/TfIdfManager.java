/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.functions;

import activejdbc.Base;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import representations.EventWeightedFeature;
import representations.Feature;
import representations.Event;

/**
 *
 * @author seb
 */
public class TfIdfManager {

    // frequencyMap maps each word with it's frequency
    public HashMap<String, Integer[]> frequencyMap;
    public HashMap<String, Double> tfidfMap;
    public HashMap<String, Double> weightsMap;

    public TfIdfManager() {
        frequencyMap = new HashMap<String, Integer[]>();
        tfidfMap = new HashMap<String, Double>();
        weightsMap = new HashMap<String, Double>();
    }

    public void countFrequency(List<String> tokens) {
        for (String w : tokens) {
            if (frequencyMap.containsKey(w)) {
                Integer[] intArr = {(frequencyMap.get(w))[0] + 1, (frequencyMap.get(w))[1]};
                frequencyMap.put(w, intArr);
            } else {
                Integer[] intArr = {1, getNumberOfEventsContainingTerm(w)};
                frequencyMap.put(w, intArr);
            }
        }
    }

    public int getMaxFreq() {
        int max = 0;
        for (String w : frequencyMap.keySet()) {
            int f = frequencyMap.get(w)[0];
            if (f > max) {
                max = f;
            }
        }
        return max;
    }

    public static int getNumberOfEventsContainingTerm(String term) {
        int nb = 0;
        try {
            Connection dbConnection = Base.connection();
            Statement statement = dbConnection.createStatement();
            Feature f = Feature.findFirst("name = ?", term);
            if (f != null) {
                // the feature exists
                ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM events_weighted_features WHERE feature_id = " + f.getId() + " AND frequency > 0");
                rs.next();
                nb = rs.getInt(1);
                rs.close();
            } else {
                nb = 0;
            }
            statement.close();
        } catch (SQLException ex) {
            System.err.println(ex);
        } finally {
            return nb;
        }
    }

    public static int getTotalNumberOfEvents() {
        int nb = 0;
        try {
            Connection dbConnection = Base.connection();
            Statement statement = dbConnection.createStatement();

            // the feature exists
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM events");
            rs.next();
            nb = rs.getInt(1);

            rs.close();
            statement.close();
        } catch (SQLException ex) {
            System.err.println(ex);
        } finally {
            return nb;
        }
    }

    public void computeTfIdf() {
        int maxfreq = getMaxFreq();
        int totalNbDoc = getTotalNumberOfEvents();

        if (totalNbDoc < 2) {
            // to avoid the log to be null or NaN
            totalNbDoc = 2;
        }

        for (String term : frequencyMap.keySet()) {
            int freq = frequencyMap.get(term)[0];
            int nbDoc = frequencyMap.get(term)[1];

            double tf = (double) freq / (double) maxfreq;
            double idf = Math.log((double) totalNbDoc / (double) (1 + nbDoc));
            tfidfMap.put(term, tf * idf);
        }
    }

    public void computeWeigths() {
        double norm = 0;
        for (String term : tfidfMap.keySet()) {
            norm += tfidfMap.get(term) * tfidfMap.get(term);
        }
        norm = Math.sqrt(norm);
        for (String term : tfidfMap.keySet()) {
            double w = tfidfMap.get(term) / norm;
            weightsMap.put(term, w);
        }
    }

    /**
     * Computes TF*IDF weights for an event's features
     * @param e event
     * @return HashMap<feature_id, tfIdfWeight>
     */
    public static HashMap<Integer, Double> computeTfIdfWeights(Event e) {
        HashMap<Integer, Double> weightsMap = new HashMap<Integer, Double>();

        try {
            Connection dbConnection = Base.connection();
            Statement statement = dbConnection.createStatement();

            // the feature exists
            ResultSet rs = statement.executeQuery("SELECT weight FROM events_weighted_features WHERE event_id=" + e.getId() + " AND frequency!=0 ORDER BY weight DESC LIMIT 1");

            int maxfreq;

            if (!rs.next()) {
                maxfreq = 1;
            } else {
                maxfreq = rs.getInt(1);
            }

            rs.close();
            statement.close();

            int totalNbDoc = getTotalNumberOfEvents();

            if (totalNbDoc < 2) {
                // to avoid the log to be null or NaN
                totalNbDoc = 2;
            }

            List<EventWeightedFeature> ewf_list = EventWeightedFeature.find("event_id=" + e.getId() + "AND frequency!=0");

            HashMap<String, Double> tfidfMap = new HashMap<String, Double>();
            HashMap<String, Integer> termIdMap = new HashMap<String, Integer>();

            for (EventWeightedFeature ewf : ewf_list) {
                int feature_id = ewf.getInteger("feature_id");
                String term = Feature.findById(feature_id).getString("name");
                termIdMap.put(term, feature_id);
                int freq = ewf.getInteger("frequency");
                int nbDoc = getNumberOfEventsContainingTerm(term);
                double tf = (double) freq / (double) maxfreq;
                double idf = Math.log((double) totalNbDoc / (double) (1 + nbDoc));

                tfidfMap.put(term, tf * idf);
            }

            double norm = 0;
            for (String term : tfidfMap.keySet()) {
                norm += tfidfMap.get(term) * tfidfMap.get(term);
            }
            norm = Math.sqrt(norm);
            for (String term : tfidfMap.keySet()) {
                double w = tfidfMap.get(term) / norm;
                weightsMap.put((Integer) termIdMap.get(term), w);
            }
        } catch (SQLException ex) {
            System.err.println(ex);
        } finally {
            return weightsMap;
        }
    }

    public void computeFromTokens(List<String> tokens) {
        countFrequency(tokens);
        computeTfIdf();
        computeWeigths();
    }

    public HashMap<String, Double> getWeightsMap() {
        return weightsMap;
    }

    public HashMap<String, Integer> getFrequencyMap() {
        HashMap<String, Integer> fm = new HashMap<String, Integer>();
        for (String term : frequencyMap.keySet()) {
            fm.put(term, frequencyMap.get(term)[0]);
        }
        return fm;
    }
}
