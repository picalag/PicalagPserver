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
@Table("events_boolean_features")
@BelongsToParents({
    @BelongsTo(foreignKeyName = "event_id", parent = Event.class),
    @BelongsTo(foreignKeyName = "feature_id", parent = Feature.class),})
public class EventBooleanFeature extends Model {

}
