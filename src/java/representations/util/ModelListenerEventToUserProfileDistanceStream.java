/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package representations.util;

import activejdbc.ModelListener;
import pserver.functions.CosineDistance;
import representations.Event;
import representations.User;

/**
 *
 * @author seb
 */
public class ModelListenerEventToUserProfileDistanceStream implements ModelListener<Event> {

    public User u;
    public HashMapTopNEvents hm;

    /**
     * Constructor
     * @param u     cosine distance to user u's profile will be computed
     * @param hm    result will be stored on hm (see representations.util.HashMapTopNEvent class for conditions)
     */
    public ModelListenerEventToUserProfileDistanceStream(User u, HashMapTopNEvents hm) {
        this.u = u;
        this.hm = hm;
    }

    @Override
    public void onModel(Event e) {
        double sim = CosineDistance.cosineSimilarity(this.u, e);
        //System.err.println(e2.getId() + "\t" + sim);
        hm.put(e, sim);
    }

}
