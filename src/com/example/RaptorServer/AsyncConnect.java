package com.example.RaptorServer;

import android.os.AsyncTask;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AsyncConnect extends AsyncTask<Void, Void, String>  {


    @Override
    protected String doInBackground(Void... voids) {
        try
        {
            Connection conn = new Connection(Globals.hostname);

            conn.connect();

            boolean isAuthenticated = conn.authenticateWithPassword(Globals.username, Globals.password);

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

        return "cool";
    }

    @Override
    protected void onPostExecute(String result) {

    }


}