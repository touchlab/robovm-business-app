package org.robovm.samples.contractr.core.service;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by kgalligan on 2/14/16.
 */
@DatabaseTable
public class WorkUnit {
    @DatabaseField(id = true)
    public String id;

    @DatabaseField(foreign = true)
    public Task task;

    @DatabaseField(dataType = DataType.DATE_LONG)
    public Date startTime;

    @DatabaseField(dataType = DataType.DATE_LONG)
    public Date endTime;
}
