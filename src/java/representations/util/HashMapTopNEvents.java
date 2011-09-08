/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import representations.Event;

/**
 * This class implements Map<Event, Double>
 * it is design to allow to keep only the top-N (distance based) events and discriminate
 * relevant candidates thanks to threshold
 * @author seb
 */
public class HashMapTopNEvents implements Map<Event, Double> {

    public int n;
    public double threshold;
    public HashMap<Event, Double> hm;

    /**
     * Constructor
     * @param nmax          number of entries max in the hashmap
     * @param threshold     threshold for adding new event
     */
    public HashMapTopNEvents(int nmax, double threshold) {
        this.n = nmax;
        this.threshold = threshold;
        hm = new HashMap<Event, Double>();
    }

    @Override
    public int size() {
        return hm.size();
    }

    @Override
    public boolean isEmpty() {
        return hm.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return hm.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return hm.containsValue(value);
    }

    @Override
    public Double get(Object key) {
        return hm.get(key);
    }

    /**
     * The put function is different from a normal HashMap as it has extra-condition
     * to check before the Event is added in the HashMap hm. However, the params and the return value are similar.
     * @param key
     * @param value
     * @return
     */
    @Override
    public Double put(Event key, Double value) {
        Double d = null;
        if (value > threshold) {
            if (hm.size() < n) {
                // still space available
                d = hm.put(key, value);
            } else {
                // if the hashmap is full and the similarity we want to add is
                // better than the worst in the current list, drop the worst and add this one
                Event e_min = getEventMinSim();
                if (hm.get(e_min) < value) {
                    hm.remove(e_min);
                    d = hm.put(key, value);
                }
            }
        }
        return d;
    }

    /**
     * @return the event with the smallest double associated
     */
    public Event getEventMinSim() {
        Event e_min = null;
        double min = Double.MAX_VALUE;
        for (Event e : hm.keySet()) {
            if (hm.get(e) < min) {
                e_min = e;
                min = hm.get(e);
            }
        }
        return e_min;
    }

    @Override
    public Double remove(Object key) {
        return hm.remove(key);
    }

    @Override
    public void putAll(Map m) {
        hm.putAll(m);
    }

    @Override
    public void clear() {
        hm.clear();
    }

    @Override
    public Set<Event> keySet() {
        return hm.keySet();
    }

    @Override
    public Collection values() {
        return hm.values();
    }

    @Override
    public Set entrySet() {
        return hm.entrySet();
    }

    public HashMap<Event, Double> getHashMap() {
        return hm;
    }
}
