package com.example.RaptorServer;

import android.app.Activity;
import android.os.Bundle;
import java.io.File;
import java.util.regex.Pattern;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.LocalFile;


public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    // Set these variables for your testing environment:


    private String host = "hostaddress";  // Remote SFTP hostname
    private String user = "username";      // Remote system login name
        private String password = "password";    // Remote system password
    private String remoteDir = "/../var/www/";
    // Look for a file path like "smoke20070128_wkt.txt"
    private String filePatternString = ".*/*.png";
    // Local directory to receive file
    private String localDir = "/sdcard/test";


    private File localDirFile;
    private Pattern filePattern;
    private FileSystemManager fsManager = null;
    private FileSystemOptions opts = null;
    private FileObject sftpFile;

    private FileObject src = null; // used for cleanup in release()


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MyActivity app = new MyActivity();

        app.initialize();

        app.process();

        app.release();

    }


    /**
     * Creates the download directory localDir if it
     * does not exist and makes a connection to the remote SFTP server.
     *
     */
    public void initialize() {
        if (localDirFile == null) {
            localDirFile = new File(localDir);
        }
        if (!this.localDirFile.exists()) {
            localDirFile.mkdirs();
        }

        try {
            this.fsManager = VFS.getManager();
        } catch (FileSystemException ex) {
            throw new RuntimeException("failed to get fsManager from VFS", ex);
        }

        UserAuthenticator auth = new StaticUserAuthenticator(null, this.user, this.password);//UserAuthenticator(null, this.user, this.password);
        this.opts = new FileSystemOptions();
        try {
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
        } catch (FileSystemException ex) {
            throw new RuntimeException("setUserAuthenticator failed", ex);
        }

        this.filePattern = Pattern.compile(filePatternString);
    } // initialize()


    /**
     * Retrieves files that match the specified FileSpec from the SFTP server
     * and stores them in the local directory.
     */
    public void process() {

        String startPath = "sftp://" + this.host + this.remoteDir;
        FileObject[] children;

        // Set starting path on remote SFTP server.
        try {
            this.sftpFile = this.fsManager.resolveFile(startPath, opts);

            //System.out.println("SFTP connection successfully established to " + startPath);
        } catch (FileSystemException ex) {
            throw new RuntimeException("SFTP error parsing path " +
                    this.remoteDir,
                    ex);
        }


        // Get a directory listing
        try {
            children = this.sftpFile.getChildren();
        } catch (FileSystemException ex) {
            throw new RuntimeException("Error collecting directory listing of " +
                    startPath, ex);
        }

        search:
        for (FileObject f : children) {
            try {
                String relativePath =
                        File.separatorChar + f.getName().getBaseName();

                if (f.getType() == FileType.FILE) {
                    System.out.println("Examining remote file " + f.getName());

                    if (!this.filePattern.matcher(f.getName().getPath()).matches()) {
                        System.out.println("  Filename does not match, skipping file ." +
                                relativePath);
                        continue search;
                    }

                    String localUrl = "file://" + this.localDir + relativePath;
                    String standardPath = this.localDir + relativePath;
                    System.out.println("  Standard local path is " + standardPath);
                    LocalFile localFile =
                            (LocalFile) this.fsManager.resolveFile(localUrl);
                    System.out.println("    Resolved local file name: " +
                            localFile.getName());

                    if (!localFile.getParent().exists()) {
                        localFile.getParent().createFolder();
                    }

                    System.out.println("  ### Retrieving file ###");
                    localFile.copyFrom(f,
                            new AllFileSelector());
                } else {
                    System.out.println("Ignoring non-file " + f.getName());
                }
            } catch (FileSystemException ex) {
                throw new RuntimeException("Error getting file type for " +
                        f.getName(), ex);
            }
        } // for (FileObject f : children)

        // Set src for cleanup in release()
        src = children[0];
    } // process(Object obj)


    /**
     * Release system resources, close connection to the filesystem.
     */
    public void release() {
        FileSystem fs = null;

        fs = this.src.getFileSystem(); // This works even if the src is closed.
        this.fsManager.closeFileSystem(fs);
    } // release()
}
