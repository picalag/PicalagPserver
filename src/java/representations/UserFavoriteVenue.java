/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations;

import activejdbc.Model;
import activejdbc.annotations.BelongsTo;
import activejdbc.annotations.BelongsToParents;
import activejdbc.annotations.Table;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author seb
 */
@Table("users_favorite_venues")
@BelongsToParents({
    @BelongsTo(foreignKeyName = "venue_id", parent = Venue.class),
    @BelongsTo(foreignKeyName = "user_id", parent = User.class),})
public class UserFavoriteVenue extends Model {

    /**
     * Adds a venue as user's favorite
     * @param u user
     * @param v venue
     * @return UserFavoriteVenue activeObject
     */
    public static UserFavoriteVenue addToFavorite(User u, Venue v) {
        UserFavoriteVenue ufv = UserFavoriteVenue.createIt();
        u.add(ufv);
        v.add(ufv);

        return ufv;
    }

    /**
     * Removes venue v from user u's favorite list
     * @param u user
     * @param v venue
     */
    public static void removeFromFavorite(User u, Venue v) {
        UserFavoriteVenue ufv = UserFavoriteVenue.findFirst("user_id=" + u.getId() + " AND venue_id=" + v.getId());
        if (ufv != null) {
            ufv.delete();
        }
    }

    /**
     * Toggles favorite status for a venue v in user u's favorite venues list
     * if v was not favorite: add it in the list
     * if v was favorite: removes it from the list
     * @param u user
     * @param v venue
     */
    public static void toggleFavorite(User u, Venue v) {
        UserFavoriteVenue ufv = UserFavoriteVenue.findFirst("user_id=" + u.getId() + " AND venue_id=" + v.getId());
        if (ufv != null) {
            ufv.delete();
        } else {
            addToFavorite(u, v);
        }
    }

 
    /**
     * get the ArrayList of user *u*'s favorite venues
     * @param u user
     * @return an ArrayList of user *u*'s favorite venues
     */
    public static ArrayList<Venue> getUserFavoriteVenuesList(User u) {
        ArrayList<Venue> fvList = new ArrayList<Venue>();

        List<UserFavoriteVenue> ufvList = u.getAll(UserFavoriteVenue.class);
        for (UserFavoriteVenue ufv : ufvList) {
            fvList.add(ufv.parent(Venue.class));
        }
        return fvList;
    }
}
