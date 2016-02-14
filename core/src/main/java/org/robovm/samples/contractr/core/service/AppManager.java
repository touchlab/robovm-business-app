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

    public AppManager(RoboVMContext roboVMContext) {
        MBassador<Object> clientBus = new MBassador<Object>(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default()));

        MBassador<Object> taskBus = new MBassador<Object>(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default()));

        databaseHelper = new DatabaseHelper(roboVMContext, "asdf", clientBus, taskBus);
    }



    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}
