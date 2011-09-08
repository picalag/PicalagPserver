<%-- 
    Document   : getRecommendations
    Created on : 18 juil. 2011, 14:45:20
    Author     : seb
--%>
<%@page import="representations.Venue"%>
<%@page import="representations.recommendation.RecommendedVenue"%>
<%@page import="representations.recommendation.RecommendedEvent"%>
<%@page import="representations.Event"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/xml" pageEncoding="UTF-8"%>
<picalag>
    <recommendations>
        <%
        String recType = (String) request.getAttribute("recType");
        
        if (recType.equals("CB_Event") || recType.equals("CB_User") || (recType.equals("CF_Event"))) {
            ArrayList<RecommendedEvent> recommendations = (ArrayList<RecommendedEvent>) request.getAttribute("recommendations");
            for (RecommendedEvent re : recommendations) {
                Event e = re.getEvent();
                if (e != null) {
                    %>
                    <event>
                        <id><%=e.get("id_calag")%></id>
                        <distance><%=re.dist%></distance>
                    </event>
                    <%
                }
            }
        }
        else if (recType.equals("CF_User")) {
            ArrayList<RecommendedEvent> recommendations = (ArrayList<RecommendedEvent>) request.getAttribute("recommendations");
            for (RecommendedEvent re : recommendations) {
                Event e = re.getEvent();
                if (e != null) {
                    %>
                    <event>
                        <id><%=e.get("id_calag")%></id>
                        <%
                        for(int rating : re.recommendedBecause) {
                        %>
                            <neighbour_rating><%=rating%></neighbour_rating>
                        <%
                        }
                        %>
                    </event>
                    <%
                }
            }
        }
        else if (recType.equals("Venues")) {
            ArrayList<RecommendedVenue> recommendations = (ArrayList<RecommendedVenue>) request.getAttribute("recommendations");
            for (RecommendedVenue rv : recommendations) {
                Venue v = rv.getVenue();
                if (v != null) {
                    %>
                    <venue>
                        <id><%=v.get("id_calag")%></id>
                        <%
                        for(int id_venue : rv.recommendedBecause) {
                        %>
                            <similar><%=id_venue%></similar>
                        <%
                        }
                        %>
                    </venue>
                    <%
                }
            }
        }
        else if (recType.equals("Pop_Event") || recType.equals("Random_Event")) {
            ArrayList<RecommendedEvent> recommendations = (ArrayList<RecommendedEvent>) request.getAttribute("recommendations");
            for (RecommendedEvent re : recommendations) {
                Event e = re.getEvent();
                if (e != null) {
                    %>
                    <event>
                        <id><%=e.get("id_calag")%></id>
                     <% if (recType.equals("Pop_Event")) {  %>
                            <grade><%=re.rating%></grade>
                     <% } %>
                    </event>
                    <%
                }
            }
        }
        else if (recType.equals("Pop_Venue") || recType.equals("Random_Venue")) {
            ArrayList<RecommendedVenue> recommendations = (ArrayList<RecommendedVenue>) request.getAttribute("recommendations");
            for (RecommendedVenue rv : recommendations) {
                Venue v = rv.getVenue();
                if (v != null) {
                    %>
                    <venue>
                        <id><%=v.get("id_calag")%></id>
                     <% if (recType.equals("Pop_Venue")) {  %>
                            <fans><%=rv.nb_fans%></fans>
                     <% } %>
                    </venue>
                    <%
                }
            }
        }
        %>
    </recommendations>
</picalag>