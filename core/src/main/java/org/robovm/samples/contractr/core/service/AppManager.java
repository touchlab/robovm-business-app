package org.robovm.samples.contractr.core.service;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import org.robovm.samples.contractr.core.common.RoboVMContext;

/**
 * Created by kgalligan on 2/13/16.
 */
public class AppManager {
    final DatabaseHelper databaseHelper;
    private final MBassador<Object> clientBus;
    private final MBassador<Object> taskBus;

    public AppManager(RoboVMContext roboVMContext) {
        this.clientBus = new MBassador<Object>(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default()));

        this.taskBus = new MBassador<Object>(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default()));

        databaseHelper = new DatabaseHelper(roboVMContext, "asdf", clientBus, taskBus);
    }



    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}
