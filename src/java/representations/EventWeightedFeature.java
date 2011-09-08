/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package representations;

import activejdbc.Model;
import activejdbc.annotations.*;

/**
 *
 * @author seb
 */
@Table("events_weighted_features")
@BelongsToParents({
    @BelongsTo(foreignKeyName="event_id", parent=Event.class),
    @BelongsTo(foreignKeyName="feature_id", parent=Feature.class),
})
public class EventWeightedFeature extends Model {
    // public Feature feature=null;
}
