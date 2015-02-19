package com.tokko.cameandwent.cameandwent;

import java.util.Calendar;
public class TimeConverter {

    public static long hourAndMinuteToMillis(long came, String time) {
        String[] split = time.split(":");
        int hour = Integer.valueOf(split[0]);
        int minute = Integer.valueOf(split[1]);
        return hourAndMinuteToMillis(came, hour, minute);
    }

    public static long hourAndMinuteToMillis(int hour, int minute) {
        return hourAndMinuteToMillis(System.currentTimeMillis(), hour, minute);
    }
    public static long hourAndMinuteToMillis(long time, int hour, int minute){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        return c.getTimeInMillis();
    }

    public static int millisToHours(long millis){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public static int millisToMinutes(long millis){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.MINUTE);
    }

    public static long extractDate(long time){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        return c.getTimeInMillis();
    }


    public static long weeksToMillis(int weeks){
        return 7*24*60*60*1000;
    }


    public static long timeIntervalAsLong(String time) {
        String[] split = time.split(":");
        int hour = Integer.valueOf(split[0]);
        int minute = Integer.valueOf(split[1]);
        return hour*60*60*1000 + minute*60*1000;
    }


}
