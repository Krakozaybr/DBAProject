package com.example.dbaproject.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String format(Date date){
        return new SimpleDateFormat(DATE_TIME_FORMAT).format(date);
    }
}
