package com.nesscomputing.amqp;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;

import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;

/**
 * Starts and stops a local QPid for testing.
 */
public class QPidProvider implements AmqpProvider
{
    private Broker b;
    private int port = 0;

    @Override
    public void startup() throws Exception
    {
        final URL configUrl = Resources.getResource(QPidProvider.class, "/qpid/config.xml");
        final File configFile = new File(configUrl.toURI());
        BrokerOptions options = new BrokerOptions();
        options.setConfigFile(configFile.getAbsolutePath());
        final URL log4jUrl = Resources.getResource(QPidProvider.class, "/log4j.xml");
        final File log4jFile = new File(log4jUrl.toURI());
        options.setLogConfigFile(log4jFile.getAbsolutePath());

        port = findUnusedPort();
        options.addPort(port);

        System.setProperty("QPID_HOME", configFile.getParentFile().getAbsolutePath());

        b = new Broker();
        b.startup(options);
    }

    @Override
    public String getUri()
    {
        return String.format("amqp://localhost:%d", port);
    }

    @Override
    public void shutdown()
    {
        b.shutdown();
    }

    private static final int findUnusedPort()
    {
        int port;

        ServerSocket socket = null;
        try {
            socket = new ServerSocket();
            socket.bind(new InetSocketAddress(0));
            port = socket.getLocalPort();
        }
        catch (IOException ioe) {
            throw Throwables.propagate(ioe);
        }
        finally {
            try {
                socket.close();
            } catch (IOException ioe) {
                // GNDN
            }
        }

        return port;
    }
}
