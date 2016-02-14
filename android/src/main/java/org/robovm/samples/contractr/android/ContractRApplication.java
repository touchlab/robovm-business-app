package org.robovm.samples.contractr.android;

import android.app.Application;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import org.robovm.samples.contractr.core.common.RoboVMContext;
import org.robovm.samples.contractr.core.service.AppManager;
import roboguice.RoboGuice;

public class ContractRApplication extends Application {

    private AppManager appManager;
    private RoboVMContext roboVMContext;

    @Override
    public void onCreate() {

        try {
            Class.forName("org.sqldroid.SQLDroidDriver");
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }

        roboVMContext = new RoboVMContext(true, getFilesDir());
        appManager = new AppManager(roboVMContext);

        RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(this)).with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(RoboVMContext.class).toInstance(roboVMContext);
                        bind(AppManager.class).toInstance(appManager);
                    }
                }));

        super.onCreate();
    }
}
