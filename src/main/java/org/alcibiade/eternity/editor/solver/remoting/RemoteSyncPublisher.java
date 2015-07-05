/* This file is part of Eternity II Editor.
 *
 * Eternity II Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Eternity II Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Eternity II Editor.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Eternity II Editor project is hosted on SourceForge:
 * http://sourceforge.net/projects/eternityii/
 * and maintained by Yannick Kirschhoffer <alcibiade@alcibiade.org>
 */
package org.alcibiade.eternity.editor.solver.remoting;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import org.alcibiade.eternity.editor.model.GridModel;
import org.alcibiade.eternity.editor.solver.ClusterListener;
import org.alcibiade.eternity.editor.solver.ClusterManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * This is a watchdog process that will send status updates to a remote server.
 *
 * @author Yannick Kirschhoffer
 */
public class RemoteSyncPublisher extends Thread implements ClusterListener {

    public static final String HTTP_PROTOCOL = "http";
    private static final long STATUS_UPDATE_INTERVAL = 3600 * 1000;
    private ClusterManager clusterManager;
    private URL remoteServerUrl;
    private boolean newRecordAvailable;

    public RemoteSyncPublisher(ClusterManager clusterManager, String remoteServer) {
        this.clusterManager = clusterManager;
        try {
            this.remoteServerUrl = new URL(remoteServer);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Malformed URL " + remoteServer, ex);
        }

        if (!remoteServerUrl.getProtocol().equals(HTTP_PROTOCOL)) {
            throw new IllegalArgumentException("Unknown protocol " + remoteServerUrl.getProtocol());
        }

        this.setName("Remote synchronization publisher");
        this.newRecordAvailable = false;

        clusterManager.logMessage("Results will be published to %s", remoteServerUrl);

        clusterManager.addClusterListener(this);
    }

    @Override
    public void run() {
        try {
            long lastStatusUpdate = System.currentTimeMillis();

            do {
                long now = System.currentTimeMillis();

                if (newRecordAvailable || (now - lastStatusUpdate) > STATUS_UPDATE_INTERVAL) {
                    lastStatusUpdate = System.currentTimeMillis();
                    newRecordAvailable = false;
                    tryToNotifyServer();
                }

                if (!isInterrupted()) {
                    sleep(10 * 1000);
                }
            } while (!isInterrupted());
        } catch (InterruptedException e) {
            // Do nothing.
        }
    }

    @Override
    public void bestSolutionUpdated(int bestScore) {
        this.newRecordAvailable = true;
    }

    private void tryToNotifyServer() {
        try {
            clusterManager.logMessage("Sending current solution to %s",
                    remoteServerUrl.toExternalForm());

            GridModel bestSolution = clusterManager.getBestSolution();

            NameValuePair[] data = {
                new NameValuePair("hostname", InetAddress.getLocalHost().getHostName()),
                new NameValuePair("score", Integer.toString(bestSolution.countPairs())),
                new NameValuePair("grid", bestSolution.toQuadString())
            };

            PostMethod post = new PostMethod(remoteServerUrl.toExternalForm());
            post.setRequestBody(data);


            HttpClient client = new HttpClient();
            int status = client.executeMethod(post);

            post.getResponseBodyAsString();

            if (status == HttpStatus.SC_OK) {
                clusterManager.logMessage("Http transfer successful");
            } else {
                clusterManager.logMessage("Http transfer failed with status " + status);
            }
        } catch (IOException ex) {
            clusterManager.logMessage(ex.getLocalizedMessage());
        }
    }
}
