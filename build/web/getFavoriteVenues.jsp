<%-- 
    Document   : getFavoriteVenues
    Created on : 17 juil. 2011, 13:25:09
    Author     : seb
--%>
<%@page import="java.util.ArrayList"%>
<%@page import="representations.Venue"%>
<%@page contentType="text/xml" pageEncoding="UTF-8"%>
<picalag>
    <favorite_venues>
        <%
                    ArrayList<Venue> favoriteVenues = (ArrayList<Venue>) request.getAttribute("favoriteVenues");
                    for (Venue v : favoriteVenues) { %>
                        <venue><%=v.get("id_calag")%></venue>
        <%          }
        %>
    </favorite_venues>
</picalag>