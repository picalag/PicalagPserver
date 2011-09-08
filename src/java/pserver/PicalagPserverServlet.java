package pserver;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import item_analyser.EventAnalyser;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import recommender.CollaborativeFiltering;
import recommender.ContentBased;
import recommender.NonPersonalizedRS;
import recommender.VenueRecommender;
import representations.*;
import representations.recommendation.RecommendedEvent;
import representations.recommendation.RecommendedVenue;
import representations.util.ComparatorDistRecommendedEvents;
import representations.util.ComparatorNbFansRecommendedVenues;
import representations.util.ComparatorRatingRecommendedEvents;

/**
 *
 * @author seb
 */
public class PicalagPserverServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Servlet response will be XML if no error
        response.setContentType("text/xml");

        System.out.println("Received request:");
        System.out.println(request.getPathInfo());
        Map<String, String[]> parameterMap = request.getParameterMap();
        System.out.println("Parameters: " + parameterMap.size());
        for (String key : parameterMap.keySet()) {
            String values = "";
            for (String val : parameterMap.get(key)) {
                values += val + "; ";
            }
            System.out.println(key + " = " + values);
        }

        try {
            // NB:  at this point, DB Connection is already opened by a Filter
            //      so we don't have to care about open or close it...

            // Servlet Requests handling => RESTful style API

            if ((request.getPathInfo().equals("/")) || (request.getPathInfo().matches(".*\\.jsp"))) {
                System.out.println("Not a method, exclude");
            } // end exclude
            /**
             * @method /post_event
             * @description adds a new event to PServer DB
             * @param   id_event    (int)       event ID in calagator
             * @param   title       (string)    event title in calagator
             * @param   description (string)    event description in calagator
             * @param   venue       (string)    venue name in calagator
             * @param   id_venue    (int)       venue ID in calagator
             * @param   tags        (string)    event tags
             * @param   date        (string)    event start date: format "yyyy-mm-dd" or "yyyy-mm-dd hh:mm"
             */
            else if (request.getPathInfo().equals("/post_event")) {
                post_event(request, response);
            } // end post_event
            /**
             * @method /post_venue
             * @description adds a new venue to PServer DB
             * @param   id_venue    (int)       venue ID in calagator
             */
            else if (request.getPathInfo().equals("/post_venue")) {
                post_venue(request, response);
            } // end post_venue
            /**
             * @method /add_user
             * @description creates a new user or updates an existing user in PServer DB
             * @param   id_user     (int)       user ID in calagator
             * @param   male        (boolean)   "true" if male, "false" if female
             * @param   dob         (string)    date of birth: format "yyyy-mm-dd" or "yyyy-mm-dd hh:mm"
             */
            else if (request.getPathInfo().equals("/add_user")) {
                add_user(request, response);
            } // end add_user
            /**
             * @method /rate_event
             * @description called when a user rates an event
             * @param id_user   (int)   user ID in calagator
             * @param id_event  (int)   event ID in calagator
             * @param rating    (mixed) rating value
             *                    |   (int)    an explicit rating
             *                    |or (string) based on an implicit rating event ("disliked"|"neutral"|"viewed"|"added"|"liked"|"shared")
             */
            else if (request.getPathInfo().equals("/rate_event")) {
                rate_event(request, response);
            } // end rate_event
            /**
             * @method /view_event
             * @description a user views an event: both event feature vector and user profile should be updated
             * @param id_user   (int)   user ID in calagator
             * @param id_event  (int)   event ID in calagator
             */
            else if (request.getPathInfo().equals("/view_event")) {
                view_event(request, response);
            } // end view_event
            /**
             * @method /never_again
             * @description removes the event features from user's profile and rate it as disliked event
             * @param id_user   (int)   user ID in calagator
             * @param id_event  (int)   event ID in calagator
             */
            else if (request.getPathInfo().equals("/never_again")) {
                never_again(request, response);
            } // end never_again
            /**
             * @method /add_venue_to_favorite
             * @description adds a venue to user's favorite venues list
             * @param id_user   (int)   user ID in calagator
             * @param id_venue  (int)   venue ID in calagator
             */
            else if (request.getPathInfo().equals("/add_venue_to_favorite")) {
                add_venue_to_favorite(request, response);
            } // end add_venue_to_favorite
            /**
             * @method /del_venue_from_favorite
             * @description removes a venue from user's favorite venues list
             * @param id_user   (int)   user ID in calagator
             * @param id_venue  (int)   venue ID in calagator
             */
            else if (request.getPathInfo().equals("/del_venue_from_favorite")) {
                del_venue_from_favorite(request, response);
            } // end del_venue_from_favorite
            /**
             * @method /get_favorite_venues
             * @description returns an XML with user's favorite venues list of calagator IDs
             * @param id_user   (int)   user ID in calagator
             */
            else if (request.getPathInfo().equals("/get_favorite_venues")) {
                get_favorite_venues(request, response);
            } // end get_favorite_venues
            /**
             * @method /get_recommendations_CB_event
             * @description returns an XML with recommended events by a content-based recommender system. Recommendations are events similar to *id_event* event
             * @param id_event  (int)       event ID in calagator
             * @param nb_recs   (int)       *optional* maximum number of recommendations (default = 5)
             * @param date      (string)    *optional* date of recommended events: format "yyyy-mm-dd" or "yyyy-mm-dd hh:mm" (default = same date as the *id_event* event)
             */
            else if (request.getPathInfo().equals("/get_recommendations_CB_event")) {
                get_recommendations_CB_event(request, response);
            } // end get_recommendations_CB_event
            /**
             * @method /get_recommendations_CB_user
             * @description returns an XML with recommended events by a content-based recommender system. Recommendations are events similar to *id_user* user's profile
             * @param id_user   (int)       user ID in calagator
             * @param date      (string)    date of recommended events: format "yyyy-mm-dd" or "yyyy-mm-dd hh:mm"
             * @param nb_recs   (int)       *optional* maximum number of recommendations (default = 5)
             */
            else if (request.getPathInfo().equals("/get_recommendations_CB_user")) {
                get_recommendations_CB_user(request, response);
            } // end get_recommendations_CB_user
            /**
             * @method /get_recommendations_CF_event
             * @description returns an XML with recommended events by a collaborative filtering recommender system (ItemBased). Recommendations are events similar to *id_event* event
             * @param id_event  (int)       event ID in calagator
             * @param nb_recs   (int)       *optional* maximum number of recommendations (default = 5)
             * @param date      (string)    *optional* date of recommended events: format "yyyy-mm-dd" or "yyyy-mm-dd hh:mm" (default = same date as the *id_event* event)
             */
            else if (request.getPathInfo().equals("/get_recommendations_CF_event")) {
                get_recommendations_CF_event(request, response);
            } // end get_recommendations_CF_event
            /**
             * @method /get_recommendations_CF_user
             * @description returns an XML with recommended events by a collaborative filtering recommender system (UserBased). Recommendations are events similar to *id_user* user's neighbourhood interest
             * @param id_user   (int)       user ID in calagator
             * @param date      (string)    date of recommended events: format "yyyy-mm-dd" or "yyyy-mm-dd hh:mm"
             * @param nb_recs   (int)       *optional* maximum number of recommendations (default = 5)
             */
            else if (request.getPathInfo().equals("/get_recommendations_CF_user")) {
                get_recommendations_CF_user(request, response);
            } // end get_recommendations_CB_user
            /**
             * @method /get_recommendations_venues
             * @description returns an XML with recommended venues by a collaborative filtering recommender system (SlopeOne). Recommendations are venues similar to *id_user* user's favorite venues
             * @param id_user   (int)       user ID in calagator
             * @param nb_recs   (int)       *optional* maximum number of recommendations (default = 5)
             */
            else if (request.getPathInfo().equals("/get_recommendations_venues")) {
                get_recommendations_venues(request, response);
            } // end get_recommendations_venues
            /**
             * @method /get_recommendations_most_popular_events
             * @description returns an XML with the most popular events (according to the sum of ratings)
             * @param date      (string)    date of recommended events: format "yyyy-mm-dd" or "yyyy-mm-dd hh:mm"
             * @param nb_recs   (int)       *optional* maximum number of recommendations (default = 5)
             * @param id_user   (int)       *optional* user ID in calagator, if given, events known by this user won't be recommended
             */
            else if (request.getPathInfo().equals("/get_recommendations_most_popular_events")) {
                get_recommendations_most_popular_events(request, response);
            } // end get_recommendations_most_popular_events
            /**
             * @method /get_recommendations_most_popular_venues
             * @description returns an XML with the most popular venues (according to the number of user having them on their favorite list)
             * @param nb_recs   (int)       *optional* maximum number of recommendations (default = 5)
             * @param id_user   (int)       *optional* user ID in calagator, if given, venues already in this user's favorite list won't be recommended
             */
            else if (request.getPathInfo().equals("/get_recommendations_most_popular_venues")) {
                get_recommendations_most_popular_venues(request, response);
            } // end get_recommendations_most_popular_venues
            /**
             * @method /get_recommendations_random_events
             * @description returns an XML with random events (according to the sum of ratings)
             * @param date      (string)    date of recommended events: format "yyyy-mm-dd" or "yyyy-mm-dd hh:mm"
             * @param nb_recs   (int)       *optional* maximum number of recommendations (default = 5)
             * @param id_user   (int)       *optional* user ID in calagator, if given, events known by this user won't be recommended
             */
            else if (request.getPathInfo().equals("/get_recommendations_random_events")) {
                get_recommendations_random_events(request, response);
            } // end get_recommendations_random_events
            /**
             * @method /get_recommendations_random_venues
             * @description returns an XML with random venues (according to the number of user having them on their favorite list)
             * @param nb_recs   (int)       *optional* maximum number of recommendations (default = 5)
             * @param id_user   (int)       *optional* user ID in calagator, if given, venues already in this user's favorite list won't be recommended
             */
            else if (request.getPathInfo().equals("/get_recommendations_random_venues")) {
                get_recommendations_random_venues(request, response);
            } // end get_recommendations_random_venues
            /**
             * @method /is_favorite_venue
             * @description returns "true" if the venue *id_venue* is one of *id_user*'s favorite
             * @param id_user   (int)       user ID in calagator
             * @param id_venue  (int)       venue ID in calagator
             */
            else if (request.getPathInfo().equals("/is_favorite_venue")) {
                is_favorite_venue(request, response);
            } // end is_favorite_venue
            /**
             * @method /get_rating
             * @description returns user's rating for an event
             * @param id_user   (int)   user ID in calagator
             * @param id_event  (int)   event ID in calagator
             */
            else if (request.getPathInfo().equals("/get_rating")) {
                get_rating(request, response);
            } // end get_rating
            /**
             * @method /ping
             * @description sends OK status if server is running
             */
            else if (request.getPathInfo().equals("/ping")) {
                response.sendError(response.SC_OK, "PONG!");
            } // end ping
            else {
                // Method not found, return error 404 "NOT FOUND"
                response.sendError(response.SC_NOT_FOUND, "Method not found");
            }

        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    // API functions
    protected void post_event(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Trying to post a new event

        // Get parameters
        String s_id_event = request.getParameter("id_event");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String venue = request.getParameter("venue");
        String s_id_venue = request.getParameter("id_venue");
        String tags = request.getParameter("tags");
        String date = request.getParameter("date");
        // TODO tags ?

        Integer id_event = null;
        Integer id_venue = null;

        try {
            id_event = Integer.parseInt(s_id_event);
            id_venue = Integer.parseInt(s_id_venue);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_event or id_venue:\n" + nan);
        }


        if ((id_event == null)
                || (title == null)
                || (description == null)
                || (venue == null)
                || (id_venue == null)
                || (tags == null)
                || (date == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // correct call to post_event
            boolean done = EventAnalyser.analyseEvent(id_event, title, description, venue, id_venue, tags, date);
            if (done) {
                response.sendError(response.SC_OK, "Successfully added event");
            } else {
                response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Oops, looks like something went wrong while adding the event");
            }
        }

    }

    protected void post_venue(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Trying to post a new venue

        // Get parameters

        String s_id_venue = request.getParameter("id_venue");

        Integer id_venue = null;

        try {
            id_venue = Integer.parseInt(s_id_venue);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_venue:\n" + nan);
        }

        if (id_venue == null) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            Venue.findOrCreateByCalagId(id_venue);
            response.sendError(response.SC_OK, "Successfully added event");
        }
    }

    protected void add_user(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");
        String s_male = request.getParameter("male");
        String dob = request.getParameter("dob");

        Integer id_user = null;

        try {
            id_user = Integer.parseInt(s_id_user);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_user:\n" + nan);
        }


        if ((id_user == null)
                || (dob == null)
                || (!s_male.equals("true") && !s_male.equals("false"))) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, add user
            boolean male = (s_male.equals("true") ? true : false);

            boolean done = User.addOrUpdateUser(id_user, male, dob);
            if (done) {
                response.sendError(response.SC_OK, "Successfully added or updated user");
            } else {
                response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Oops, looks like something went wrong while adding or updating user");
            }
        }

    }

    protected void rate_event(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");
        String s_id_event = request.getParameter("id_event");
        String s_rating = request.getParameter("rating");

        Integer id_user = null;
        Integer id_event = null;
        Integer rating = null;

        try {
            id_event = Integer.parseInt(s_id_event);
            id_user = Integer.parseInt(s_id_user);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_event or id_user:\n" + nan);
        }
        try {
            rating = Integer.parseInt(s_rating);
        } catch (NumberFormatException nan) {
            // not an explicit grade, try typical values
            if (s_rating.equals("disliked")) {
                rating = Rating.DISLIKED;
            } else if (s_rating.equals("neutral")) {
                rating = Rating.NEUTRAL;
            } else if (s_rating.equals("added")) {
                rating = Rating.ADDED;
            } else if (s_rating.equals("liked")) {
                rating = Rating.LIKED;
            } else if (s_rating.equals("shared")) {
                rating = Rating.SHARED;
            } else if (s_rating.equals("viewed")) {
                rating = Rating.VIEWED;
            } else {
                System.err.println("Not a grade exception:\n" + nan);
                rating = Rating.NEUTRAL;
            }
        }

        if ((id_user == null)
                || (id_event == null)
                || (rating == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, rate event

            // try to fetch event and user from db with their calagator id
            Event e = Event.findFirstByCalagId(id_event);
            User u = User.findFirstByCalagId(id_user);
            if (e == null) {
                response.sendError(response.SC_NOT_FOUND, "Event not in DB");
            } else if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else {
                // everything fine, proceed !
                boolean done = u.rateEvent(e, rating);
                if (done) {
                    response.sendError(response.SC_OK, "Successfully rated event");
                } else {
                    response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Oops, looks like something went wrong while rating the event");
                }
            }
        }
    }

    protected void view_event(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");
        String s_id_event = request.getParameter("id_event");

        Integer id_user = null;
        Integer id_event = null;

        try {
            id_event = Integer.parseInt(s_id_event);
            id_user = Integer.parseInt(s_id_user);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_event or id_user:\n" + nan);
        }

        if ((id_user == null)
                || (id_event == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, view event

            // try to fetch event and user from db with their calagator id
            Event e = Event.findFirstByCalagId(id_event);
            User u = User.findFirstByCalagId(id_user);
            if (e == null) {
                response.sendError(response.SC_NOT_FOUND, "Event not in DB");
            } else if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else {
                // everything fine, proceed !
                boolean done = u.viewEventUpdateProfileAndEventVector(e);
//                if (Rating.findFirst("user_id=" + u.getId() + " AND event_id=" + e.getId()) == null) {
//                    u.rateEvent(e, Rating.VIEWED);
//                }
                if (done) {
                    response.sendError(response.SC_OK, "Successfully viewed event: event features vector and user's profile updated");
                } else {
                    response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Oops, looks like something went wrong while viewind and updating event and user's profile");
                }
            }
        }
    }

    protected void never_again(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");
        String s_id_event = request.getParameter("id_event");

        Integer id_user = null;
        Integer id_event = null;

        try {
            id_event = Integer.parseInt(s_id_event);
            id_user = Integer.parseInt(s_id_user);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_event or id_user:\n" + nan);
        }

        if ((id_user == null)
                || (id_event == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, never again

            // try to fetch event and user from db with their calagator id
            Event e = Event.findFirstByCalagId(id_event);
            User u = User.findFirstByCalagId(id_user);
            if (e == null) {
                response.sendError(response.SC_NOT_FOUND, "Event not in DB");
            } else if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else {
                // everything fine, proceed !
                boolean done1 = u.neverAgain(e);
                boolean done2 = u.rateEvent(e, Rating.DISLIKED);
                boolean done = done1 && done2;
                if (done) {
                    response.sendError(response.SC_OK, "Successfully updated profile with the disliked event");
                } else {
                    response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Oops, looks like something went wrong while disliking the event");
                }
            }
        }
    }

    protected void add_venue_to_favorite(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");
        String s_id_venue = request.getParameter("id_venue");

        Integer id_user = null;
        Integer id_venue = null;

        try {
            id_venue = Integer.parseInt(s_id_venue);
            id_user = Integer.parseInt(s_id_user);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_venue or id_user:\n" + nan);
        }

        if ((id_user == null)
                || (id_venue == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, add to favorite venues list

            // try to fetch venue and user from db with their calagator id
            Venue v = Venue.findFirstByCalagId(id_venue);
            User u = User.findFirstByCalagId(id_user);
            if (v == null) {
                response.sendError(response.SC_NOT_FOUND, "Venue not in DB");
            } else if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else {
                // everything fine, proceed !
                boolean done = u.addVenueToFavorite(v);
                if (done) {
                    response.sendError(response.SC_OK, "Successfully updated favorite venues list");
                } else {
                    response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Oops, looks like something went wrong while updating favorite venues list");
                }
            }
        }
    }

    protected void del_venue_from_favorite(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");
        String s_id_venue = request.getParameter("id_venue");

        Integer id_user = null;
        Integer id_venue = null;

        try {
            id_venue = Integer.parseInt(s_id_venue);
            id_user = Integer.parseInt(s_id_user);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_venue or id_user:\n" + nan);
        }

        if ((id_user == null)
                || (id_venue == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, del from favorite venues list

            // try to fetch venue and user from db with their calagator id
            Venue v = Venue.findFirstByCalagId(id_venue);
            User u = User.findFirstByCalagId(id_user);
            if (v == null) {
                response.sendError(response.SC_NOT_FOUND, "Venue not in DB");
            } else if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else {
                // everything fine, proceed !
                boolean done = u.removeVenueFromFavorite(v);
                if (done) {
                    response.sendError(response.SC_OK, "Successfully updated favorite venues list");
                } else {
                    response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Oops, looks like something went wrong while updating favorite venues list");
                }
            }
        }
    }

    protected void get_favorite_venues(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");

        Integer id_user = null;

        try {
            id_user = Integer.parseInt(s_id_user);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_user:\n" + nan);
        }

        if (id_user == null) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get user's favorite venues list

            // try to fetch user from db with his calagator id
            User u = User.findFirstByCalagId(id_user);
            if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else {
                // everything fine, proceed !

                ArrayList<Venue> favoriteVenues = u.getFavoriteVenuesList();
                request.setAttribute("favoriteVenues", favoriteVenues);

                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/getFavoriteVenues.jsp");
                dispatcher.forward(request, response);
            }
        }
    }

    protected void get_recommendations_CB_event(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String s_id_event = request.getParameter("id_event");
        String s_nb_recs = request.getParameter("nb_recs");
        String date = request.getParameter("date");

        boolean sameDay = false;
        if ((date == null) || (date.isEmpty())) {
            sameDay = true;
        } else {
            // date normalisation
            date = EventAnalyser.dateConversion(date);
        }

        Integer id_event = null;
        Integer nb_recs = null;

        if ((s_nb_recs == null) || (s_nb_recs.isEmpty())) {
            // default nb recs = 5
            nb_recs = 5;
        }

        try {
            id_event = Integer.parseInt(s_id_event);
            if (nb_recs == null) {
                nb_recs = Integer.parseInt(s_nb_recs);
            }
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_event or nb_recs:\n" + nan);
        }

        if ((id_event == null)
                || (nb_recs == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get CB recommendations similar to event

            // try to fetch event from db with its calagator id
            Event e = Event.findFirstByCalagId(id_event);
            if (e == null) {
                response.sendError(response.SC_NOT_FOUND, "Event not in DB");
            } else {
                // everything fine, proceed !
                ArrayList<RecommendedEvent> recommendations = new ArrayList<RecommendedEvent>();

                if (sameDay) {
                    recommendations = ContentBased.recommendationEventsSameDay(e, nb_recs);
                } else {
                    recommendations = ContentBased.recommendationEvents(e, date, nb_recs);
                }

                Collections.sort(recommendations, Collections.reverseOrder(new ComparatorDistRecommendedEvents()));

//                for (RecommendedEvent re : recommendations) {
//                    int id_rec = re.getEvent().getInteger("id_calag");
//                    PicalagLog.log("recommended_CB_event|||" + id_event + "|||" + id_rec);
//                }

                request.setAttribute("recommendations", recommendations);
                request.setAttribute("recType", "CB_Event");

                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/getRecommendations.jsp");
                dispatcher.forward(request, response);

            }
        }

    }

    protected void get_recommendations_CB_user(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String s_id_user = request.getParameter("id_user");
        String s_nb_recs = request.getParameter("nb_recs");
        String date = request.getParameter("date");

        Integer id_user = null;
        Integer nb_recs = null;

        if ((s_nb_recs == null) || (s_nb_recs.isEmpty())) {
            // default nb recs = 5
            nb_recs = 5;
        }

        try {
            id_user = Integer.parseInt(s_id_user);
            if (nb_recs == null) {
                nb_recs = Integer.parseInt(s_nb_recs);
            }
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_event or nb_recs:\n" + nan);
        }

        if ((id_user == null)
                || (nb_recs == null)
                || (date == null)
                || (date.isEmpty())) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get CB recommendations similar to user

            // date normalisation
            date = EventAnalyser.dateConversion(date);

            // try to fetch user from db with its calagator id
            User u = User.findFirstByCalagId(id_user);
            if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else {
                // everything fine, proceed !
                ArrayList<RecommendedEvent> recommendations = new ArrayList<RecommendedEvent>();
                recommendations = ContentBased.recommendationEventsToUserProfile(u, date, nb_recs);

                Collections.sort(recommendations, Collections.reverseOrder(new ComparatorDistRecommendedEvents()));

//                for (RecommendedEvent re : recommendations) {
//                    int id_rec = re.getEvent().getInteger("id_calag");
//                    PicalagLog.log("recommended_CB_user|||" + id_user + "|||" + id_rec);
//                }

                request.setAttribute("recommendations", recommendations);
                request.setAttribute("recType", "CB_User");

                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/getRecommendations.jsp");
                dispatcher.forward(request, response);

            }
        }

    }

    protected void get_recommendations_CF_event(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String s_id_event = request.getParameter("id_event");
        String s_nb_recs = request.getParameter("nb_recs");
        String date = request.getParameter("date");

        boolean sameDay = false;
        if ((date == null) || (date.isEmpty())) {
            sameDay = true;
        } else {
            // date normalisation
            date = EventAnalyser.dateConversion(date);
        }

        Integer id_event = null;
        Integer nb_recs = null;

        if ((s_nb_recs == null) || (s_nb_recs.isEmpty())) {
            // default nb recs = 5
            nb_recs = 5;
        }

        try {
            id_event = Integer.parseInt(s_id_event);
            if (nb_recs == null) {
                nb_recs = Integer.parseInt(s_nb_recs);
            }
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_event or nb_recs:\n" + nan);
        }

        if ((id_event == null)
                || (nb_recs == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get CF recommendations similar to event

            // try to fetch event from db with its calagator id
            Event e = Event.findFirstByCalagId(id_event);
            if (e == null) {
                response.sendError(response.SC_NOT_FOUND, "Event not in DB");
            } else {
                // everything fine, proceed !
                ArrayList<RecommendedEvent> recommendations = new ArrayList<RecommendedEvent>();

                if (sameDay) {
                    recommendations = CollaborativeFiltering.eventToEventRecommendationsSameDay(e, nb_recs);
                } else {
                    recommendations = CollaborativeFiltering.eventToEventRecommendations(e, date, nb_recs);
                }

                Collections.sort(recommendations, Collections.reverseOrder(new ComparatorDistRecommendedEvents()));

//                for (RecommendedEvent re : recommendations) {
//                    int id_rec = re.getEvent().getInteger("id_calag");
//                    PicalagLog.log("recommended_CF_event|||" + id_event + "|||" + id_rec);
//                }

                request.setAttribute("recommendations", recommendations);
                request.setAttribute("recType", "CF_Event");

                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/getRecommendations.jsp");
                dispatcher.forward(request, response);

            }
        }


    }

    protected void get_recommendations_CF_user(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String s_id_user = request.getParameter("id_user");
        String s_nb_recs = request.getParameter("nb_recs");
        String date = request.getParameter("date");

        Integer id_user = null;
        Integer nb_recs = null;

        if ((s_nb_recs == null) || (s_nb_recs.isEmpty())) {
            // default nb recs = 5
            nb_recs = 5;
        }

        try {
            id_user = Integer.parseInt(s_id_user);
            if (nb_recs == null) {
                nb_recs = Integer.parseInt(s_nb_recs);
            }
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_event or nb_recs:\n" + nan);
        }

        if ((id_user == null)
                || (nb_recs == null)
                || (date == null)
                || (date.isEmpty())) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get CF recommendations similar to user

            // date normalisation
            date = EventAnalyser.dateConversion(date);

            // try to fetch user from db with its calagator id
            User u = User.findFirstByCalagId(id_user);
            if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else {
                // everything fine, proceed !
                ArrayList<RecommendedEvent> recommendations = new ArrayList<RecommendedEvent>();

                recommendations = CollaborativeFiltering.userBasedRecommendations(u, date, nb_recs);

                Collections.sort(recommendations, Collections.reverseOrder(new ComparatorRatingRecommendedEvents()));

//                for (RecommendedEvent re : recommendations) {
//                    int id_rec = re.getEvent().getInteger("id_calag");
//                    PicalagLog.log("recommended_CF_user|||" + id_user + "|||" + id_rec);
//                }

                request.setAttribute("recommendations", recommendations);
                request.setAttribute("recType", "CF_User");

                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/getRecommendations.jsp");
                dispatcher.forward(request, response);

            }
        }

    }

    protected void get_recommendations_venues(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");
        String s_nb_recs = request.getParameter("nb_recs");

        Integer nb_recs = null;

        if ((s_nb_recs == null) || (s_nb_recs.isEmpty())) {
            // default nb recs = 5
            nb_recs = 5;
        }

        Integer id_user = null;

        try {
            id_user = Integer.parseInt(s_id_user);
            if (nb_recs == null) {
                nb_recs = Integer.parseInt(s_nb_recs);
            }
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_user or nb_recs:\n" + nan);
        }

        if ((id_user == null)
                || (nb_recs == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get venues recommendations

            // try to fetch user from db with his calagator id
            User u = User.findFirstByCalagId(id_user);
            if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else {
                // everything fine, proceed !

                ArrayList<RecommendedVenue> recommendations = new ArrayList<RecommendedVenue>();

                recommendations = VenueRecommender.recommendVenues(u, nb_recs);

//                for (RecommendedVenue rv : recommendations) {
//                    int id_rec = rv.getVenue().getInteger("id_calag");
//                    PicalagLog.log("recommended_Venue|||" + id_user + "|||" + id_rec);
//                }

                request.setAttribute("recommendations", recommendations);
                request.setAttribute("recType", "Venues");

                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/getRecommendations.jsp");
                dispatcher.forward(request, response);
            }
        }
    }

    protected void get_recommendations_most_popular_events(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String s_id_user = request.getParameter("id_user");
        String s_nb_recs = request.getParameter("nb_recs");
        String date = request.getParameter("date");

        Integer id_user = null;
        Integer nb_recs = null;

        if ((s_nb_recs == null) || (s_nb_recs.isEmpty())) {
            // default nb recs = 5
            nb_recs = 5;
        }

        try {
            if ((s_id_user != null) && (!s_id_user.isEmpty())) {
                id_user = Integer.parseInt(s_id_user);
            }
            if (nb_recs == null) {
                nb_recs = Integer.parseInt(s_nb_recs);
            }
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_user or nb_recs:\n" + nan);
        }

        if ((nb_recs == null)
                || (date == null)
                || (date.isEmpty())) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get popular events

            // date normalisation
            date = EventAnalyser.dateConversion(date);

            // try to fetch user from db with its calagator id
            User u = null;
            if (id_user != null) {
                u = User.findFirstByCalagId(id_user);
            } else {
                id_user = 0;
            }

            // everything fine, proceed !
            ArrayList<RecommendedEvent> recommendations = new ArrayList<RecommendedEvent>();


            if (u == null) {
                recommendations = NonPersonalizedRS.getMostPopularEvents(date, nb_recs);
            } else {
                recommendations = NonPersonalizedRS.getMostPopularEvents(date, nb_recs, u);
            }

            Collections.sort(recommendations, Collections.reverseOrder(new ComparatorRatingRecommendedEvents()));

//            for (RecommendedEvent re : recommendations) {
//                int id_rec = re.getEvent().getInteger("id_calag");
//                PicalagLog.log("recommended_pop_event|||" + id_user + "|||" + id_rec);
//            }

            request.setAttribute("recommendations", recommendations);
            request.setAttribute("recType", "Pop_Event");

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/getRecommendations.jsp");
            dispatcher.forward(request, response);
        }
    }

    protected void get_recommendations_most_popular_venues(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String s_id_user = request.getParameter("id_user");
        String s_nb_recs = request.getParameter("nb_recs");

        Integer id_user = null;
        Integer nb_recs = null;

        if ((s_nb_recs == null) || (s_nb_recs.isEmpty())) {
            // default nb recs = 5
            nb_recs = 5;
        }

        try {
            if ((s_id_user != null) && (!s_id_user.isEmpty())) {
                id_user = Integer.parseInt(s_id_user);
            }
            if (nb_recs == null) {
                nb_recs = Integer.parseInt(s_nb_recs);
            }
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_user or nb_recs:\n" + nan);
        }

        if (nb_recs == null) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get popular venues

            // try to fetch user from db with its calagator id
            User u = null;
            if (id_user != null) {
                u = User.findFirstByCalagId(id_user);
            } else {
                id_user = 0;
            }

            // everything fine, proceed !
            ArrayList<RecommendedVenue> recommendations = new ArrayList<RecommendedVenue>();


            if (u == null) {
                recommendations = NonPersonalizedRS.getMostPopularVenues(nb_recs);
            } else {
                recommendations = NonPersonalizedRS.getMostPopularVenues(nb_recs, u);
            }

            Collections.sort(recommendations, Collections.reverseOrder(new ComparatorNbFansRecommendedVenues()));

//            for (RecommendedVenue rv : recommendations) {
//                int id_rec = rv.getVenue().getInteger("id_calag");
//                PicalagLog.log("recommended_pop_venue|||" + id_user + "|||" + id_rec);
//            }

            request.setAttribute("recommendations", recommendations);
            request.setAttribute("recType", "Pop_Venue");

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/getRecommendations.jsp");
            dispatcher.forward(request, response);
        }

    }

    protected void get_recommendations_random_events(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");
        String s_nb_recs = request.getParameter("nb_recs");
        String date = request.getParameter("date");

        Integer id_user = null;
        Integer nb_recs = null;

        if ((s_nb_recs == null) || (s_nb_recs.isEmpty())) {
            // default nb recs = 5
            nb_recs = 5;
        }

        try {
            if ((s_id_user != null) && (!s_id_user.isEmpty())) {
                id_user = Integer.parseInt(s_id_user);
            }
            if (nb_recs == null) {
                nb_recs = Integer.parseInt(s_nb_recs);
            }
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_user or nb_recs:\n" + nan);
        }

        if ((nb_recs == null)
                || (date == null)
                || (date.isEmpty())) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get random events

            // date normalisation
            date = EventAnalyser.dateConversion(date);

            // try to fetch user from db with its calagator id
            User u = null;
            if (id_user != null) {
                u = User.findFirstByCalagId(id_user);
            } else {
                id_user = 0;
            }

            // everything fine, proceed !
            ArrayList<RecommendedEvent> recommendations = new ArrayList<RecommendedEvent>();


            if (u == null) {
                recommendations = NonPersonalizedRS.getRandomEvents(date, nb_recs);
            } else {
                recommendations = NonPersonalizedRS.getRandomEvents(date, nb_recs, u);
            }

//            for (RecommendedEvent re : recommendations) {
//                int id_rec = re.getEvent().getInteger("id_calag");
//                PicalagLog.log("recommended_rand_event|||" + id_user + "|||" + id_rec);
//            }

            request.setAttribute("recommendations", recommendations);
            request.setAttribute("recType", "Random_Event");

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/getRecommendations.jsp");
            dispatcher.forward(request, response);
        }
    }

    protected void get_recommendations_random_venues(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String s_id_user = request.getParameter("id_user");
        String s_nb_recs = request.getParameter("nb_recs");

        Integer id_user = null;
        Integer nb_recs = null;

        if ((s_nb_recs == null) || (s_nb_recs.isEmpty())) {
            // default nb recs = 5
            nb_recs = 5;
        }

        try {
            if ((s_id_user != null) && (!s_id_user.isEmpty())) {
                id_user = Integer.parseInt(s_id_user);
            }
            if (nb_recs == null) {
                nb_recs = Integer.parseInt(s_nb_recs);
            }
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_user or nb_recs:\n" + nan);
        }

        if (nb_recs == null) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get random venues

            // try to fetch user from db with its calagator id
            User u = null;
            if (id_user != null) {
                u = User.findFirstByCalagId(id_user);
            } else {
                id_user = 0;
            }

            // everything fine, proceed !
            ArrayList<RecommendedVenue> recommendations = new ArrayList<RecommendedVenue>();


            if (u == null) {
                recommendations = NonPersonalizedRS.getRandomVenues(nb_recs);
            } else {
                recommendations = NonPersonalizedRS.getMostPopularVenues(nb_recs, u);
            }

//            for (RecommendedVenue rv : recommendations) {
//                int id_rec = rv.getVenue().getInteger("id_calag");
//                PicalagLog.log("recommended_rand_venue|||" + id_user + "|||" + id_rec);
//            }

            request.setAttribute("recommendations", recommendations);
            request.setAttribute("recType", "Random_Venue");

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/getRecommendations.jsp");
            dispatcher.forward(request, response);
        }

    }

    protected void is_favorite_venue(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");
        String s_id_venue = request.getParameter("id_venue");

        Integer id_user = null;
        Integer id_venue = null;

        try {
            id_user = Integer.parseInt(s_id_user);
            id_venue = Integer.parseInt(s_id_venue);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_user or id_venue:\n" + nan);
        }

        if ((id_user == null) || (id_venue == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK

            // try to fetch user from db with his calagator id
            User u = User.findFirstByCalagId(id_user);
            Venue v = Venue.findFirstByCalagId(id_venue);
            if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else if (v == null) {
                response.sendError(response.SC_NOT_FOUND, "Venue not in DB");
            } else {
                // everything fine, proceed !
                response.setContentType("text");
                PrintWriter out = response.getWriter();

                UserFavoriteVenue ufv = UserFavoriteVenue.findFirst("venue_id=" + v.getId() + " AND user_id=" + u.getId());
                if (ufv != null) {
                    out.print("true");
                } else {
                    out.print("false");
                }
                out.flush();
                out.close();
            }
        }
    }

    protected void get_rating(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String s_id_user = request.getParameter("id_user");
        String s_id_event = request.getParameter("id_event");

        Integer id_user = null;
        Integer id_event = null;

        try {
            id_event = Integer.parseInt(s_id_event);
            id_user = Integer.parseInt(s_id_user);
        } catch (NumberFormatException nan) {
            System.err.println("Not a number exception for id_event or id_user:\n" + nan);
        }

        if ((id_user == null)
                || (id_event == null)) {
            // missing arguments, return error 400 "Bad syntax in the request"
            response.sendError(response.SC_BAD_REQUEST, "Missing arguments");
        } else {
            // params OK, get rating

            // try to fetch event and user from db with their calagator id
            Event e = Event.findFirstByCalagId(id_event);
            User u = User.findFirstByCalagId(id_user);
            if (e == null) {
                response.sendError(response.SC_NOT_FOUND, "Event not in DB");
            } else if (u == null) {
                response.sendError(response.SC_NOT_FOUND, "User not in DB");
            } else {
                // everything fine, proceed !
                response.setContentType("text");
                PrintWriter out = response.getWriter();

                int r = Rating.getRating(u, e);
                out.print(r);

                out.flush();
                out.close();
            }
        }
    }
}
