/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package representations;

import activejdbc.Model;
import activejdbc.annotations.BelongsTo;
import activejdbc.annotations.BelongsToParents;
import activejdbc.annotations.Table;

/**
 *
 * @author seb
 */
@Table("ratings")
@BelongsToParents({
    @BelongsTo(foreignKeyName = "event_id", parent = Event.class),
    @BelongsTo(foreignKeyName = "user_id", parent = User.class),})
public class Rating extends Model {

    /**
     * Possible ratings
     */
    public static final int DISLIKED = -5;
    public static final int NEUTRAL = 0;
    public static final int VIEWED = 1;
    public static final int ADDED = 3;
    public static final int SHARED = 5;
    public static final int LIKED = 5;

    public void rate(User u, Event e, int rating) {
        this.setRating(rating);
        u.add(this);
        e.add(this);
    }

    /**
     * Creates a rating entry in db
     * @param u         the user that rates the event
     * @param e         the event to rate
     * @param rating    rating to be given, see final static ints
     * @return the rating activeObject
     */
    public static Rating rateIt(User u, Event e, int rating) {
        Rating r = Rating.findFirst("event_id=" + e.getId() + " AND user_id=" + u.getId());
        if (r == null) {
            r = Rating.createIt("rating", rating);
            u.add(r);
            e.add(r);
        } else {
            r.setRating(rating);
        }

        return r;
    }

    public static int getRating(User u, Event e) {
        Rating r = Rating.findFirst("event_id=" + e.getId() + " AND user_id=" + u.getId());
        if (r == null) {
            return Rating.NEUTRAL;
        } else {
            return r.getRating();
        }
    }

    public int getRating() {
        return this.getInteger("rating");
    }

    public void setRating(int r) {
        this.set("rating", r);
        this.saveIt();
    }
}
