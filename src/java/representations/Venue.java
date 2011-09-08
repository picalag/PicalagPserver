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
public class Venue extends Model {

    public static Venue findOrCreateByCalagId(int id) {
        Venue v = Venue.findFirst("id_calag = ?", id);
        if (v == null) {
            v = Venue.createIt("id_calag", id);
        }
        return v;
    }

    public static Venue findFirstByCalagId(int id) {
        return Venue.findFirst("id_calag = ?", id);
    }
}
