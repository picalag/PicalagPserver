/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations.util;

import activejdbc.ModelListener;
import pserver.functions.CosineDistance;
import representations.Event;

/**
 * This class calcs the distance between an event *e* and event fetched from the database
 * in a streaming maneer
 * @author seb
 */
public class ModelListenerEventDistanceStream implements ModelListener<Event> {

    public Event e;
    public HashMapTopNEvents hm;

    /**
     * Constructor
     * @param e     cosine distance to e will be computed
     * @param hm    result will be stored on hm (see representations.util.HashMapTopNEvent class for conditions)
     */
    public ModelListenerEventDistanceStream(Event e, HashMapTopNEvents hm) {
        this.e = e;
        this.hm = hm;
    }

    @Override
    public void onModel(Event e2) {
        
        double sim = CosineDistance.cosineSimilarity(this.e, e2);
        //System.err.println(e2.getId() + "\t" + sim);
        hm.put(e2, sim);
    }
}
