/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender;

import activejdbc.Base;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import representations.recommendation.RecommendedEvent;
import java.sql.*;
import representations.User;
import representations.recommendation.RecommendedVenue;

/**
 *
 * @author seb
 */
public class NonPersonalizedRS {

    private NonPersonalizedRS() {
    }

    /**
     * Returns a list of most popular events taking place at the given date
     * @param date      event date
     * @param nb_recs   maximum number of recommended events
     * @return list of recommendations
     */
    public static ArrayList<RecommendedEvent> getMostPopularEvents(String date, int nb_recs) {
        ArrayList<RecommendedEvent> recommendations = new ArrayList<RecommendedEvent>();

        try {
            Connection co = Base.connection();
            Statement st = co.createStatement();
            String sql = "SELECT "
                    + "events.id, SUM(ratings.rating) as grade "
                    + "FROM ratings, events "
                    + "WHERE "
                    + "ratings.event_id = events.id "
                    + "AND events.date = '" + date + "' "
                    + "GROUP BY events.id ORDER BY grade DESC LIMIT 0 , " + nb_recs;

            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int event_id = rs.getInt("id");
                int grade = rs.getInt("grade");
                if (grade > 0) {
                    RecommendedEvent re = new RecommendedEvent(event_id);
                    re.rating = grade;
                    recommendations.add(re);
                }
            }

            rs.close();
            st.close();

        } catch (SQLException ex) {
            System.err.println(ex);
        }

        return recommendations;
    }

    /**
     * Returns a list of most popular events taking place at the given date, unknown from user *u*
     * @param date      event date
     * @param nb_recs   maximum number of recommended events
     * @param u         a user
     * @return list of recommendations
     */
    public static ArrayList<RecommendedEvent> getMostPopularEvents(String date, int nb_recs, User u) {
        ArrayList<RecommendedEvent> recommendations = new ArrayList<RecommendedEvent>();

        try {
            Connection co = Base.connection();
            Statement st = co.createStatement();
            String sql = "SELECT "
                    + "events.id, SUM(ratings.rating) as grade "
                    + "FROM ratings, events "
                    + "WHERE "
                    + "ratings.event_id = events.id "
                    + "AND events.date = '" + date + "' "
                    + "AND events.id NOT IN (SELECT event_id FROM ratings WHERE user_id = " + (Integer) u.getId() + ") "
                    + "GROUP BY events.id ORDER BY grade DESC LIMIT 0 , " + nb_recs;
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int event_id = rs.getInt("id");
                int grade = rs.getInt("grade");
                if (grade > 0) {
                    RecommendedEvent re = new RecommendedEvent(event_id);
                    re.rating = grade;
                    recommendations.add(re);
                }
            }

            rs.close();
            st.close();

        } catch (SQLException ex) {
            System.err.println(ex);
        }

        return recommendations;
    }

    /**
     * Returns a list of most popular venues
     * @param nb_recs maximum number of recommended venues
     * @return list of recommendations
     */
    public static ArrayList<RecommendedVenue> getMostPopularVenues(int nb_recs) {
        ArrayList<RecommendedVenue> recommendations = new ArrayList<RecommendedVenue>();

        try {
            Connection co = Base.connection();
            Statement st = co.createStatement();
            String sql = "SELECT venue_id, COUNT(*) as nb_fans "
                    + "FROM users_favorite_venues "
                    + "GROUP BY venue_id "
                    + "ORDER BY nb_fans DESC LIMIT 0 , " + nb_recs;
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int venue_id = rs.getInt("venue_id");
                int nb_fans = rs.getInt("nb_fans");
                RecommendedVenue rv = new RecommendedVenue(venue_id);
                rv.nb_fans = nb_fans;
                recommendations.add(rv);
            }

            rs.close();
            st.close();

        } catch (SQLException ex) {
            System.err.println(ex);
        }

        return recommendations;
    }

    /**
     * Returns a list of most popular venues not already in user *u*'s favorite list
     * @param nb_recs   maximum number of recommended venues
     * @param u         a user
     * @return list of recommendations
     */
    public static ArrayList<RecommendedVenue> getMostPopularVenues(int nb_recs, User u) {
        ArrayList<RecommendedVenue> recommendations = new ArrayList<RecommendedVenue>();

        try {
            Connection co = Base.connection();
            Statement st = co.createStatement();
            String sql = "SELECT venue_id, COUNT(*) as nb_fans "
                    + "FROM users_favorite_venues "
                    + "WHERE "
                    + "venue_id NOT IN (SELECT venue_id FROM users_favorite_venues WHERE user_id = " + (Integer) u.getId() + ") "
                    + "GROUP BY venue_id "
                    + "ORDER BY nb_fans DESC LIMIT 0 , " + nb_recs;
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int venue_id = rs.getInt("venue_id");
                int nb_fans = rs.getInt("nb_fans");
                RecommendedVenue rv = new RecommendedVenue(venue_id);
                rv.nb_fans = nb_fans;
                recommendations.add(rv);
            }

            rs.close();
            st.close();

        } catch (SQLException ex) {
            System.err.println(ex);
        }

        return recommendations;
    }

    /**
     * Returns a list of random events taking place at the given date
     * @param date      event date
     * @param nb_recs   maximum number of recommended events
     * @return list of recommendations
     */
    public static ArrayList<RecommendedEvent> getRandomEvents(String date, int nb_recs) {
        ArrayList<RecommendedEvent> recommendations = new ArrayList<RecommendedEvent>();

        try {
            Connection co = Base.connection();
            Statement st = co.createStatement();
            String sql = "SELECT events.id "
                    + "FROM events "
                    + "WHERE "
                    + "events.date = '" + date + "' "
                    + "ORDER BY RAND() LIMIT " + nb_recs;
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int event_id = rs.getInt("id");
                RecommendedEvent re = new RecommendedEvent(event_id);
                recommendations.add(re);
            }

            rs.close();
            st.close();

        } catch (SQLException ex) {
            System.err.println(ex);
        }

        return recommendations;
    }

    /**
     * Returns a list of random events taking place at the given date, unknown from user *u*
     * @param date      event date
     * @param nb_recs   maximum number of recommended events
     * @param u         a user
     * @return list of recommendations
     */
    public static ArrayList<RecommendedEvent> getRandomEvents(String date, int nb_recs, User u) {
        ArrayList<RecommendedEvent> recommendations = new ArrayList<RecommendedEvent>();

        try {
            Connection co = Base.connection();
            Statement st = co.createStatement();
            String sql = "SELECT events.id "
                    + "FROM events "
                    + "WHERE "
                    + "events.date = '" + date + "' "
                    + "and id NOT IN (SELECT event_id FROM ratings WHERE user_id = " + (Integer) u.getId() + ") "
                    + "ORDER BY RAND() LIMIT " + nb_recs;
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int event_id = rs.getInt("id");
                RecommendedEvent re = new RecommendedEvent(event_id);
                recommendations.add(re);
            }

            rs.close();
            st.close();

        } catch (SQLException ex) {
            System.err.println(ex);
        }

        return recommendations;
    }

    /**
     * Returns a list of random venues
     * @param nb_recs maximum number of recommended venues
     * @return list of recommendations
     */
    public static ArrayList<RecommendedVenue> getRandomVenues(int nb_recs) {
        ArrayList<RecommendedVenue> recommendations = new ArrayList<RecommendedVenue>();

        try {
            Connection co = Base.connection();
            Statement st = co.createStatement();
            ResultSet rs = st.executeQuery("SELECT id FROM venues ORDER BY RAND() LIMIT " + nb_recs);

            while (rs.next()) {
                int venue_id = rs.getInt("id");
                RecommendedVenue rv = new RecommendedVenue(venue_id);
                recommendations.add(rv);
            }

            rs.close();
            st.close();

        } catch (SQLException ex) {
            System.err.println(ex);
        }

        return recommendations;
    }

    /**
     * Returns a list of random venues not already in user u's favorite list
     * @param nb_recs   maximum number of recommended venues
     * @param u         a user
     * @return list of recommendations
     */
    public static ArrayList<RecommendedVenue> getRandomVenues(int nb_recs, User u) {
        ArrayList<RecommendedVenue> recommendations = new ArrayList<RecommendedVenue>();

        try {
            Connection co = Base.connection();
            Statement st = co.createStatement();
            String sql = "SELECT id "
                    + "FROM venues "
                    + "WHERE "
                    + "id NOT IN (SELECT venue_id FROM users_favorite_venues WHERE user_id = " + (Integer) u.getId() + ") "
                    + "ORDER BY RAND() LIMIT " + nb_recs;
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int venue_id = rs.getInt("id");
                RecommendedVenue rv = new RecommendedVenue(venue_id);
                recommendations.add(rv);
            }

            rs.close();
            st.close();

        } catch (SQLException ex) {
            System.err.println(ex);
        }

        return recommendations;
    }
}
