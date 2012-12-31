package com.example.RaptorServer;

import android.app.Activity;
import android.os.Bundle;

import java.io.*;

import ch.ethz.ssh2.*;

public class MyActivity extends Activity {




    private String remoteDir = "/../var/www/";
    // Look for a file path like "smoke20070128_wkt.txt"
    private String filePatternString = ".*/*.png";
    // Local directory to receive file
    private String localDir = "/sdcard/test";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MyActivity app = new MyActivity();

        try
        {
            Connection conn = new Connection(Globals_default.hostname);

            conn.connect();

            boolean isAuthenticated = conn.authenticateWithPassword(Globals_default.username, Globals_default.password);

            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");

            Session sess = conn.openSession();

            sess.execCommand("ls");

            System.out.println("Here is some information about the remote host:");

            /*
                * This basic example does not handle stderr, which is sometimes dangerous
                * (please read the FAQ).
                */

            InputStream stdout = new StreamGobbler(sess.getStdout());

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

            while (true)
            {
                String line = br.readLine();
                if (line == null)
                    break;
                System.out.println(line);
            }

            /* Show exit status, if available (otherwise "null") */

            System.out.println("ExitCode: " + sess.getExitStatus());

            /* Close this session */

            sess.close();

            /* Close the connection */

            conn.close();


        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
            System.exit(2);
        }


    }

}
