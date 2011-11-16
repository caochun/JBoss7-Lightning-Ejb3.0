/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.as.remoting;

import java.net.InetSocketAddress;

import org.jboss.as.network.NetworkInterfaceBinding;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.remoting3.Endpoint;
import org.jboss.remoting3.security.ServerAuthenticationProvider;
import org.jboss.remoting3.spi.NetworkServerProvider;
import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.channels.AcceptingChannel;
import org.xnio.channels.ConnectedStreamChannel;

/**
 * Contains the remoting strem server
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractStreamServerService implements Service<AcceptingChannel<? extends ConnectedStreamChannel>>{

    private final Logger log = Logger.getLogger("org.jboss.as.remoting");

    @SuppressWarnings("rawtypes")
    //private final InjectedValue<ChannelListener> connectorValue = new InjectedValue<ChannelListener>();
    private final InjectedValue<ServerAuthenticationProvider> authenticationProviderValue = new InjectedValue<ServerAuthenticationProvider>();
    private final InjectedValue<OptionMap> optionMapInjectedValue = new InjectedValue<OptionMap>();
    private final InjectedValue<Endpoint> endpointValue = new InjectedValue<Endpoint>();

    private final int port;

    private volatile AcceptingChannel<? extends ConnectedStreamChannel> streamServer;

    AbstractStreamServerService(final int port) {
        this.port = port;
    }

    @Override
    public AcceptingChannel<? extends ConnectedStreamChannel> getValue() throws IllegalStateException, IllegalArgumentException {
        return streamServer;
    }

//    @SuppressWarnings("rawtypes")
//    public InjectedValue<ChannelListener> getConnectorInjector() {
//        return connectorValue;
//    }

    public InjectedValue<ServerAuthenticationProvider> getAuthenticationProviderInjector() {
        return authenticationProviderValue;
    }

    public InjectedValue<OptionMap> getOptionMapInjectedValue() {
        return optionMapInjectedValue;
    }

    public InjectedValue<Endpoint> getEndpointInjector() {
        return endpointValue;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        try {
            NetworkServerProvider networkServerProvider = endpointValue.getValue().getConnectionProviderInterface("remote", NetworkServerProvider.class);
            ServerAuthenticationProvider sap = authenticationProviderValue.getValue();
            OptionMap options = optionMapInjectedValue.getValue();
            streamServer = networkServerProvider.createServer(getSocketAddress(), options, sap);
            log.infof("Listening on %s", getSocketAddress());

        } catch (Exception e) {
            // AutoGenerated
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        IoUtils.safeClose(streamServer);
    }

    abstract NetworkInterfaceBinding getNetworkInterfaceBinding();

    InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(getNetworkInterfaceBinding().getAddress(), port);
    }

}