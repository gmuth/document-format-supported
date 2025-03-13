package de.gmuth.ipp.client;

/**
 * Copyright (c) 2025-2025 Gerhard Muth
 */

import de.gmuth.log.Logging;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

import static java.time.Instant.now;

class PrinterDiscovery implements ServiceListener {

    interface Listener {
        void onPrinterDiscovered(String printerUri);
    }

    static Logger logger = Logging.getLogger(PrinterDiscovery.class);

    public static void discoverPrinters(Listener aListener) throws InterruptedException, IOException {
        PrinterDiscovery serviceListener = new PrinterDiscovery(aListener);
        JmDNS jmDNS = JmDNS.create();
        jmDNS.addServiceListener("_ipp._tcp.local.", serviceListener);
        serviceListener.waitUntilIdleFor(Duration.ofSeconds(3));
        jmDNS.close();
    }

    private void waitUntilIdleFor(Duration duration) throws InterruptedException {
        while (Duration.between(lastServiceEvent, now()).get(ChronoUnit.SECONDS) < duration.getSeconds()) {
            Thread.sleep(100);
        }
    }

    private final Listener listener;

    private PrinterDiscovery(Listener aListener) {
        listener = aListener;
    }

    private Instant lastServiceEvent = now();

    @Override
    public void serviceAdded(ServiceEvent serviceEvent) {
        lastServiceEvent = now();
    }

    @Override
    public void serviceRemoved(ServiceEvent serviceEvent) {
        lastServiceEvent = now();
    }

    @Override
    public void serviceResolved(ServiceEvent serviceEvent) {
        lastServiceEvent = now();
        ServiceInfo serviceInfo = serviceEvent.getInfo();
        logger.info("Discovered printer: " + serviceInfo.getName());
        String printerUri = String.format("ipp://%s:%d/%s",
                serviceInfo.getServer(),
                serviceInfo.getPort(),
                serviceInfo.getPropertyString("rp")
        );
        Logging.flush();
        listener.onPrinterDiscovered(printerUri);
    }

}