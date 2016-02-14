package org.robovm.samples.contractr.core.service;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.math.BigDecimal;

/**
 * Created by kgalligan on 2/14/16.
 */
@DatabaseTable
public class Client {

    @DatabaseField(id = true)
    public String id;

    @DatabaseField
    public String name;

    @DatabaseField
    public BigDecimal hourlyRate;
}
