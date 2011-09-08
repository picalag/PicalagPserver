/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pserver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author seb
 */
public class PicalagLog {

    private PicalagLog() {

    }

    static public void log(String msg) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("/home/seb/logs/picalag_log.txt", true));
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            out.write(dateFormat.format(date) + "|||" + msg + "\n");
            out.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
