/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations;

import activejdbc.Model;

/**
 *
 * @author seb
 */
public class Feature extends Model {

    static public Feature findOrCreateItByName(String name) {
        Feature f = Feature.findFirst("name = '" + name + "'");
        if (f == null) {
            f = Feature.createIt("name",name);
        }
        return f;
    }
}
