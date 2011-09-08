/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations;

import activejdbc.Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author seb
 */
public class Event extends Model {

    /**
     * This method adds the weighted features entries in the database for this event
     * @param weightsMap    maps the features (string) with their weights
     * @param frequencyMap  maps the features (string) with their frequencies
     */
    public void createWeightedVector(
            HashMap<String, Double> weightsMap,
            HashMap<String, Integer> frequencyMap) {

        for (String term : weightsMap.keySet()) {
            if (weightsMap.get(term) > 0) {
                this.addWeightedFeature(term, weightsMap.get(term), frequencyMap.get(term));
            }
        }
    }

    /**
     * This method adds the boolean features entries in the database for this event
     * @param boolFeatureList   list of boolean features (string) to add
     */
    public void createBooleanVector(ArrayList<String> boolFeatureList) {
        for (String term : boolFeatureList) {
            this.addBooleanFeature(term);
        }
    }

    /**
     * Adds a weighted feature entry in the database for this event
     * @param term      the feature name
     * @param weight    corresponding weight
     * @param freq      corresponding frequency
     */
    public void addWeightedFeature(String term, double weight, int freq) {
        Feature feature = Feature.findOrCreateItByName(term);
        EventWeightedFeature ewf = EventWeightedFeature.createIt();
        ewf.set("frequency", freq);
        ewf.set("weight", weight);
        ewf.saveIt();
        this.add(ewf);
        feature.add(ewf);
        ewf.saveIt();
    }

    /**
     * Adds a weighted feature entry in the database for this event
     * @param feature_id    the feature id
     * @param weight        corresponding weight
     * @param freq          corresponding frequency
     */
    public void addExistingWeightedFeature(int feature_id, double weight, int freq) {
        Feature feature = Feature.findById(feature_id);
        EventWeightedFeature ewf = EventWeightedFeature.createIt();
        ewf.set("frequency", freq);
        ewf.set("weight", weight);
        ewf.saveIt();
        this.add(ewf);
        feature.add(ewf);
        ewf.saveIt();
    }

    /**
     * Adds a boolean feature entry in the database for this event
     * @param term      the feature name
     */
    public void addBooleanFeature(String term) {
        Feature feature = Feature.findOrCreateItByName(term);
        EventBooleanFeature ebf = EventBooleanFeature.findFirst("event_id = " + this.getId() + " AND feature_id = " + feature.getId());
        if (ebf == null) {
            ebf = EventBooleanFeature.createIt();
            this.add(ebf);
            feature.add(ebf);
            ebf.saveIt();
        }
    }
    // TODO create addTag and delTag methods to deal with tags as boolean features

    /**
     * Adds a new weighted feature entry if the feature does not already exist as a weighted feature
     * or updates weight and frequency if it does
     * @param term      the feature name
     * @param weight    corresponding weight
     * @param freq      corresponding frequency
     */
    public void updateOrCreateWeightedFeature(String term, double weight, int freq) {
        Feature feature = Feature.findOrCreateItByName(term);
        EventWeightedFeature ewf = EventWeightedFeature.findFirst("event_id = " + this.getId() + " AND feature_id = " + feature.getId());
        if (ewf == null) {
            addWeightedFeature(term, weight, freq);
        } else {
            ewf.set("frequency", freq);
            ewf.set("weight", weight);
            ewf.saveIt();
        }
    }

    /**
     * Adds a new weighted feature entry if the feature does not already exist as a weighted feature
     * or updates weight and frequency if it does
     * @param feature_id    the feature id
     * @param weight        corresponding weight
     * @param freq          corresponding frequency
     */
    public void updateOrAddExistingWeightedFeature(int feature_id, double weight, int freq) {
        EventWeightedFeature ewf = EventWeightedFeature.findFirst("event_id = " + this.getId() + " AND feature_id = " + feature_id);
        if (ewf == null) {
            this.addExistingWeightedFeature(feature_id, weight, freq);
        } else {
            if (freq != 0) {
                ewf.set("frequency", freq);
            }
            ewf.set("weight", weight);
            ewf.saveIt();
        }
    }

    public List<EventWeightedFeature> getWeightedFeaturesList() {
        List<EventWeightedFeature> ewf_list = this.getAll(EventWeightedFeature.class);
        return ewf_list;
    }

    public HashMap<Integer, Double> getWeightedFeaturesHashMap() {
        HashMap<Integer, Double> hm = new HashMap<Integer, Double>();

        List<EventWeightedFeature> ewf_list = this.getAll(EventWeightedFeature.class);
        for (EventWeightedFeature wf : ewf_list) {
            hm.put((Integer) wf.get("feature_id"), wf.getDouble("weight"));
        }
        return hm;
    }

    public List<EventBooleanFeature> getBooleanFeaturesList() {
        List<EventBooleanFeature> ebf_list = this.getAll(EventBooleanFeature.class);
        return ebf_list;
    }

    public HashSet<Integer> getBooleanFeaturesHashSet() {
        HashSet<Integer> hs = new HashSet<Integer>();

        List<EventBooleanFeature> ebf_list = this.getAll(EventBooleanFeature.class);
        for (EventBooleanFeature bf : ebf_list) {
            hs.add((Integer) bf.get("feature_id"));
        }
        return hs;
    }

    /**
     * Add a view to the current event
     */
    public int view() {
        int view = this.getInteger("viewed");
        this.set("viewed", view + 1);
        this.saveIt();
        return view + 1;
    }

    /**
     * Returns an event according to its id in calagator
     * @param id    id in calag
     * @return      the corresponding event, or null if it does not exist
     */
    public static Event findFirstByCalagId(int id) {
        return Event.findFirst("id_calag = ?", id);
    }
}
