package com.yahoo.vespa.zookeeper;

import org.apache.zookeeper.server.NIOServerCnxn;
import org.apache.zookeeper.server.NIOServerCnxnFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class is created by zookeeper by reflection, see the ZooKeeperServer constructor.
 * 
 * @author bratseth
 */
@SuppressWarnings("unused")
public class RestrictedServerCnxnFactory extends NIOServerCnxnFactory {

    private static final Logger log = Logger.getLogger(RestrictedServerCnxnFactory.class.getName());
    
    public RestrictedServerCnxnFactory() throws IOException {
        super();
    }
    
    @Override
    protected NIOServerCnxn createConnection(SocketChannel socket, SelectionKey selection) throws IOException {
        String remoteHost = ((InetSocketAddress)socket.getRemoteAddress()).getHostName();

        String zookeeperClients = System.getProperty(ZooKeeperServer.ZOOKEEPER_VESPA_CLIENTS_PROPERTY);
        if (zookeeperClients == null || zookeeperClients.isEmpty()) {
            log.fine("Allowing connection to ZooKeeper from " + remoteHost + ", as " + ZooKeeperServer.ZOOKEEPER_VESPA_CLIENTS_PROPERTY + " is not set");
            return super.createConnection(socket, selection); // client checking is not activated
        }

        Set<String> zooKeeperClients = toHostnameSet(zookeeperClients);
        if ( ! remoteHost.equals("localhost") && ! zooKeeperClients.contains(remoteHost)) {
            String errorMessage = "Rejecting connection to ZooKeeper from " + remoteHost +
                                  ": This cluster only allow connection from hosts in: " + zooKeeperClients;
            if ("true".equals(System.getenv("vespa_zkfacade__restrict"))) {
                log.info(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            else {
                log.fine("Would reject if activated: " + errorMessage);
            }
        }
        log.fine("Allowing connection to ZooKeeper from " + remoteHost + ", as it is in " + zookeeperClients);
        return super.createConnection(socket, selection);
    }

    private Set<String> toHostnameSet(String commaSeparatedString) {
        Set<String> hostnames = new HashSet<>();
        for (String hostname : commaSeparatedString.split(","))
            hostnames.add(hostname.trim());
        return hostnames;
    }

}
