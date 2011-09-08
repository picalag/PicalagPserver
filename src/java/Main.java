/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import activejdbc.Base;
import item_analyser.EventAnalyser;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import pserver.functions.CosineDistance;
import representations.*;

/**
 *
 * @author seb
 */
public class Main {

    public static void main(String[] args) {
        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/picalag_pserver", "root", "");

        try {
            InputStream ips = new FileInputStream("/home/seb/Dropbox/Manchester/Project/test20110416.txt");
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String line;
            ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> hm = new HashMap<String, String>();

            while ((line = br.readLine()) != null) {
                if (line.matches("Start = .*")) {
                    line = line.replaceFirst("Start = ", "");
                    hm.put("Start", line);
                } else if (line.matches("Title = .*")) {
                    line = line.replaceFirst("Title = ", "");
                    hm.put("Title", line);
                } else if (line.matches("Venue = .*")) {
                    line = line.replaceFirst("Venue = ", "");
                    hm.put("Venue", line);
                } else if (line.matches("Description = .*")) {
                    line = line.replaceFirst("Description = ", "");
                    hm.put("Description", line);
                } else if (line.matches("Category = .*")) {
                    line = line.replaceFirst("Category = ", "");
                    hm.put("Category", line);
                } else if (line.equals("=====================")) {
                    list.add(hm);
                    hm = new HashMap<String, String>();
                }
            }

//            for (int i = 0; i < list.size(); i++) {
//                EventAnalyser ea = new EventAnalyser();
//                hm = list.get(i);
//                ea.analyseEvent(i + 1, hm.get("Title"), hm.get("Description"), hm.get("Venue"), i + 1, hm.get("Category"), hm.get("Start"));
//                System.out.println(i);
//            }

//            hm = list.get(0);
//            EventAnalyser ea = new EventAnalyser();
//            ea.analyseEvent(1, hm.get("Title"), hm.get("Description"), hm.get("Venue"), 1, hm.get("Category"), hm.get("Start"));

            Event e1 = Event.findById(1);
            Event e2 = Event.findById(72);
            Event e3 = Event.findById(30);
            System.err.println(CosineDistance.cosineSimilarity(e1, e2));
            System.err.println(CosineDistance.cosineSimilarity(e2, e1));
            System.err.println(CosineDistance.cosineSimilarity(e1, e1));
            System.err.println(CosineDistance.cosineSimilarity(e1, e3));
            System.err.println(CosineDistance.cosineSimilarity(e3, e2));


            br.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        Base.close();
    }
}
