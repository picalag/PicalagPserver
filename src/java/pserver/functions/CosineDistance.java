/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import representations.Event;
import representations.User;
import representations.UserFavoriteVenue;

/**
 *
 * @author seb
 */
public class CosineDistance {

    public CosineDistance() {
    }

    /**
     * This method computes the cosine similarity between 2 vectors A and B
     *
     * @param a     first vector, A
     * @param b     second vector, B
     * @return      sim(A,B) = (A.B)/(||A||.||B||)
     */
    public static double sim(ArrayList<Double> a, ArrayList<Double> b) {
        double sim = 0;

        if (a.size() == b.size()) {
            for (int i = 0; i < a.size(); i++) {
                sim += a.get(i) * b.get(i);
            }
            sim /= (computeNorm(a) * computeNorm(b));
        }

        return sim;
    }

    /**
     * Returns the euclidian norm of vectore A
     * @param a     vector
     * @return      sqrt(A.A)
     */
    public static double computeNorm(ArrayList<Double> a) {
        double norm = 0;

        for (double ai : a) {
            norm += ai * ai;
        }
        norm = Math.sqrt(norm);

        return norm;
    }

    /**
     * Returns the cosine similarity value between two events
     * @param e1    first event
     * @param e2    second event
     * @return      cosine similarity (see pserver.functions.CosineDistance
     */
    public static double cosineSimilarity(Event e1, Event e2) {
        double sim = 0;
        // events vectors creation

        // create empty vectors
        ArrayList<Double> e1Vect = new ArrayList<Double>();
        ArrayList<Double> e2Vect = new ArrayList<Double>();
        // get boolean features
        HashSet<Integer> e1BooleanFeaturesHashSet = e1.getBooleanFeaturesHashSet();
        HashSet<Integer> e2BooleanFeaturesHashSet = e2.getBooleanFeaturesHashSet();
        // get weighted features
//        HashMap<Integer, Double> e1WeightedFeaturesHashMap = e1.getWeightedFeaturesHashMap();
//        HashMap<Integer, Double> e2WeightedFeaturesHashMap = e2.getWeightedFeaturesHashMap();
        
        HashMap<Integer, Double> e1WeightedFeaturesHashMap = TfIdfManager.computeTfIdfWeights(e1);
        HashMap<Integer, Double> e2WeightedFeaturesHashMap = TfIdfManager.computeTfIdfWeights(e2);
        

        // compare feature sets

        // compare boolean features
        for (int f : e1BooleanFeaturesHashSet) {
            if (e2BooleanFeaturesHashSet.contains(f)) {
                e1Vect.add(1.0);
                e2Vect.add(1.0);
            } else {
                e1Vect.add(1.0);
                e2Vect.add(0.0);
            }
        }
        for (int f : e2BooleanFeaturesHashSet) {
            if (e1BooleanFeaturesHashSet.contains(f)) {
                // already done, do nothing
            } else {
                e1Vect.add(0.0);
                e2Vect.add(1.0);
            }
        }
        // compare weighted features
        for (int f : e1WeightedFeaturesHashMap.keySet()) {
            if (e2WeightedFeaturesHashMap.containsKey(f)) {
                e1Vect.add(e1WeightedFeaturesHashMap.get(f));
                e2Vect.add(e2WeightedFeaturesHashMap.get(f));
            } else {
                e1Vect.add(e1WeightedFeaturesHashMap.get(f));
                e2Vect.add(0.0);
            }
        }
        for (int f : e2WeightedFeaturesHashMap.keySet()) {
            if (e1WeightedFeaturesHashMap.containsKey(f)) {
                // already done, do nothing
            } else {
                e1Vect.add(0.0);
                e2Vect.add(e2WeightedFeaturesHashMap.get(f));
            }
        }

        // compare other parameters
        // venue ID
        if (e1.getInteger("venue_id") == e2.getInteger("venue_id")) {
            e1Vect.add(1.0);
            e2Vect.add(1.0);
        } else {
            e1Vect.add(1.0);
            e2Vect.add(0.0);
            e1Vect.add(0.0);
            e2Vect.add(1.0);
        }

        sim = CosineDistance.sim(e1Vect, e2Vect);

        return sim;
    }

    /**
     * Returns the cosine similarity value between a user's profile and an event
     * @param u     user
     * @param e     event
     * @return cosine similarity (see pserver.functions.CosineDistance)
     */
    public static double cosineSimilarity(User u, Event e) {
        double sim = 0;
        // events vectors creation

        // create empty vectors
        ArrayList<Double> uVect = new ArrayList<Double>();
        ArrayList<Double> eVect = new ArrayList<Double>();
        // get weighted features
        HashMap<Integer, Double> uWeightedFeaturesHashMap = u.getWeightedFeaturesHashMap();
        HashMap<Integer, Double> eWeightedFeaturesHashMap = e.getWeightedFeaturesHashMap();
        double mean_weight = 0;
        // compare feature sets

        // compare weighted features
        for (int f : uWeightedFeaturesHashMap.keySet()) {
            if (eWeightedFeaturesHashMap.containsKey(f)) {
                uVect.add(uWeightedFeaturesHashMap.get(f));
                eVect.add(eWeightedFeaturesHashMap.get(f));
            } else {
                uVect.add(uWeightedFeaturesHashMap.get(f));
                eVect.add(0.0);
            }
            mean_weight+=uWeightedFeaturesHashMap.get(f);
        }
        mean_weight/=(double)uWeightedFeaturesHashMap.size();
        
        for (int f : eWeightedFeaturesHashMap.keySet()) {
            if (uWeightedFeaturesHashMap.containsKey(f)) {
                // already done, do nothing
            } else {
                uVect.add(0.0);
                eVect.add(eWeightedFeaturesHashMap.get(f));
            }
        }

        // compare other parameters
        // venue ID
        UserFavoriteVenue ufv = UserFavoriteVenue.findFirst("venue_id=?", e.getInteger("venue_id"));
        if (ufv != null) {
            // venue is user's favorite
            uVect.add(mean_weight);
            eVect.add(mean_weight);
        } else {
            // venue is not user's favorite
            uVect.add(mean_weight);
            eVect.add(0.0);
            uVect.add(0.0);
            eVect.add(mean_weight);
        }

        sim = CosineDistance.sim(uVect, eVect);

        return sim;
    }
}
