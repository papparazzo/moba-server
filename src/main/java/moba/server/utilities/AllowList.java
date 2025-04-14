/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2016 Stefan Paproth <pappi-@gmx.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.utilities;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

final public class AllowList {

    private final CircularFifoQueue<InetAddress> allowList;

    public AllowList(int maxEntries, ArrayList<String> allowed)
    throws UnknownHostException {
        allowList = new CircularFifoQueue<>(maxEntries);
        if(allowed == null) {
            return;
        }
        for(String addr: allowed) {
            add(addr);
        }
    }

    public ArrayList<String> getList() {
        ArrayList<String> list = new ArrayList<>(allowList.size());
        for(InetAddress addr: allowList) {
            list.add(addr.getHostAddress());
        }
        return list;
    }

    public synchronized void setList(ArrayList<String> list)
    throws UnknownHostException {
        allowList.clear();
        for(String addr: list) {
            add(addr);
        }
    }

    public synchronized boolean add(String address)
    throws UnknownHostException {
        return add(InetAddress.getByName(address));
    }

    public synchronized boolean add(InetAddress ip) {
        if(allowList.contains(ip)) {
            return false;
        }
        return allowList.add(ip);
    }

    public synchronized boolean isAllowed(InetAddress ip) {
        if (ip.isAnyLocalAddress() || ip.isLoopbackAddress()) {
            return true;
        }
        return allowList.contains(ip);
    }
}
