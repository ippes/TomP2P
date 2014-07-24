/*
 * Copyright 2011 Thomas Bocek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.tomp2p.connection;

import java.util.Random;

/**
 * Store port information. 
 * 
 * @author Thomas Bocek
 */
public class Ports {
   
    // The number of maximum ports, 2^16.
    public static final int MAX_PORT = 65535;
    //IANA recommends to use ports higher than 49152.
    public static final int MIN_DYN_PORT = 49152;
    // The default port of TomP2P.
    public static final int DEFAULT_PORT = 7700;
    // IANA recommends to use ports higher than 49152
    private static final int RANGE = MAX_PORT - MIN_DYN_PORT;
    private static final Random RND = new Random();

    // provide this information if you know your mapping beforehand, i.e. manual
    // port-forwarding
    private final int tcpPort;
    private final int udpPort;
    private final boolean randomPorts;

    /**
     * Creates random ports for TCP and UDP. The random ports start from port 49152
     */
    public Ports() {
        this(-1, -1);
    }

    /**
     * Creates a Binding class that binds to a specified protocol and provides
     * information about manual port forwarding.
     * 
     * @param protocol
     *            The protocol to bind to
     * @param externalAddress
     *            The external address, how other peers will see us. Use null if
     *            you don't want to use external address
     * @param externalTCPPort
     *            The external port, how other peers will see us, if 0 is
     *            provided, a random port will be used
     * @param externalUDPPort
     *            The external port, how other peers will see us, if 0 is
     *            provided, a random port will be used
     */
    public Ports(final int tcpPort, final int udpPort) {
    	this.randomPorts = tcpPort < 0 && udpPort < 0;
        this.tcpPort = tcpPort < 0 ? (RND.nextInt(RANGE) + MIN_DYN_PORT) : tcpPort;
        this.udpPort = udpPort < 0 ? (RND.nextInt(RANGE) + MIN_DYN_PORT) : udpPort;
    }

    /**
     * @return Returns the external port, how other peers see us
     */
    public int tcpPort() {
        return tcpPort;
    }

    /**
     * @return Returns the external port, how other peers see us
     */
    public int udpPort() {
        return udpPort;
    }

    /**
     * @return True if the user specified both ports in advance. This tells us
     *         that the user knows about the ports and did a manual
     *         port-forwarding.
     */
    public boolean isManualPort() {
        // set setExternalPortsManually to true if the user specified both ports
        // in advance. This tells us that the user knows about the ports and did
        // a manual port-forwarding.
        return !randomPorts;
    }
}
