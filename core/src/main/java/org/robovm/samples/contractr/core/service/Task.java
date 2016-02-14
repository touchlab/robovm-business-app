package org.robovm.samples.contractr.core.service;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kgalligan on 2/14/16.
 */
@DatabaseTable
public class Task {

    @DatabaseField(id = true)
    public String id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    public Client client;

    @DatabaseField
    public String title;

    @DatabaseField
    public String notes;

    @DatabaseField
    public boolean finished;

    @DatabaseField(dataType = DataType.DATE_LONG)
    public Date workStartTime;

    @DatabaseField
    public int secondsWorked;

    public int getSecondsElapsed() {
        if (workStartTime == null) {
            return secondsWorked;
        }
        long elapsed = System.currentTimeMillis() - workStartTime.getTime();
        return (int) (elapsed / 1000) + secondsWorked;
    }

    public String getTimeElapsed() {
        int seconds = getSecondsElapsed();
        int minutes = seconds / 60;
        int hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    public String getAmountEarned(Locale locale) {
        BigDecimal amount = client.hourlyRate.multiply(BigDecimal.valueOf(getSecondsElapsed() / 3600.0));
        return NumberFormat.getCurrencyInstance(locale).format(amount);
    }
}
