package moba.server.utilities;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.net.InetAddress;

public class AllowList {

    private final CircularFifoQueue<InetAddress> allowList;

    public AllowList(int maxEntries)
    throws InterruptedException {
        allowList = new CircularFifoQueue<>(maxEntries);
    }

    public synchronized void clear() {
        allowList.clear();
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
