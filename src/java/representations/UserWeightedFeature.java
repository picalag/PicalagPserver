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
@Table("users_weighted_features")
@BelongsToParents({
    @BelongsTo(foreignKeyName="user_id", parent=User.class),
    @BelongsTo(foreignKeyName="feature_id", parent=Feature.class),
})
public class UserWeightedFeature extends Model {
    
}
