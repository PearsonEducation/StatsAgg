package com.pearson.statsagg.utilities.time_utils;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DateAndTime {

    private static final Logger logger = LoggerFactory.getLogger(DateAndTime.class.getName());
    
    public static final int COMPARE_ERROR_CODE = -44444444;

    public static String getFormattedCurrentDateAndTime(String simpleDateFormat) {
        
        if (simpleDateFormat == null) {
            return null;
        }
        
        try {
            DateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
            Date currentSystemDateAndTime = new Date();

            return dateFormat.format(currentSystemDateAndTime);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public static String getFormattedDateAndTime(Date dateAndTime, String simpleDateFormat) {
        
        if ((dateAndTime == null) || (simpleDateFormat == null)) {
            return null;
        }
        
        try {
            DateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
            return dateFormat.format(dateAndTime);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public static Date convertUnixEpochTimeToDate(String unixTime) {
        
        if (unixTime == null) {
            return null;
        }
        
        try {
            Date date = new Date(Long.parseLong(unixTime) * 1000);

            return date;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public static Calendar convertUnixEpochTimeToCalendar(String unixTime) {
        
        if (unixTime == null) {
            return null;
        }
        
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(unixTime) * 1000);

            return calendar;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public static Timestamp convertUnixEpochTimeToTimestamp(String unixTime) {
        
        if (unixTime == null) {
            return null;
        }
        
        try {
            Timestamp timestamp = new Timestamp(Long.parseLong(unixTime) * 1000);

            return timestamp;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public static boolean isValidFormattedDate(String simpleDateFormat, String testDateAndTime) {
        
        if ((simpleDateFormat == null) || (testDateAndTime == null)) {
            return false;
        }
        
        try {
            DateFormat dateValidator = new SimpleDateFormat(simpleDateFormat);
            dateValidator.parse(testDateAndTime);

            return true;
        }
        catch (ParseException e) {
            logger.trace("The entered date, \"" + testDateAndTime + "\", is not properly formatted");
            return false;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }        
    }

    public static boolean isTodayInDaysOfWeekAbbreviation(String daysOfWeekAbbreviation) {
        
        if (daysOfWeekAbbreviation == null) {
            return false;
        }
        
        try {
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("EEEE");
            String day = dateFormat.format(date);

            if (day.equals("Monday") && daysOfWeekAbbreviation.contains("M")) {
                return true;
            }
            if (day.equals("Tuesday") && daysOfWeekAbbreviation.contains("T")) {
                return true;
            }
            if (day.equals("Wednesday") && daysOfWeekAbbreviation.contains("W")) {
                return true;
            }
            if (day.equals("Thursday") && daysOfWeekAbbreviation.contains("R")) {
                return true;
            }
            if (day.equals("Friday") && daysOfWeekAbbreviation.contains("F")) {
                return true;
            }
            if (day.equals("Saturday") && daysOfWeekAbbreviation.contains("A")) {
                return true;
            }
            if (day.equals("Sunday") && daysOfWeekAbbreviation.contains("U")) {
                return true;
            }

            return false;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
    }
    
    public static void setTimeOfDayEqualToAnotherTimeOfDay(Calendar dateAndTime, Calendar desiredTime) {
        
        if ((dateAndTime == null) || (desiredTime == null) || (dateAndTime == desiredTime)) {
            return;
        }

        setTimeOfDay(dateAndTime, 
                desiredTime.get(Calendar.HOUR_OF_DAY), 
                desiredTime.get(Calendar.MINUTE),
                desiredTime.get(Calendar.SECOND), 
                desiredTime.get(Calendar.MILLISECOND));
    }
    
    public static void setTimeOfDay(Calendar dateAndTime, int hourOfDay, int minute, int second, int millisecond) { 
        
        if (dateAndTime == null) {
            return;
        }
        
        dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateAndTime.set(Calendar.MINUTE, minute);
        dateAndTime.set(Calendar.SECOND, second);
        dateAndTime.set(Calendar.MILLISECOND, millisecond);  
    }
    
    public static void setTimeOfDay(Calendar dateAndTime, int secondsIntoDay) {
        
        if (dateAndTime == null) {
            return;
        }
        
        dateAndTime.set(Calendar.HOUR_OF_DAY, secondsIntoDay / 3600);
        dateAndTime.set(Calendar.MINUTE, (secondsIntoDay % 3600) / 60);
        dateAndTime.set(Calendar.SECOND, (secondsIntoDay % 3600) % 60);
        dateAndTime.set(Calendar.MILLISECOND, 0);
    }
    
    public static int getSecondsIntoDay(Calendar dateAndTime) {
        
        if (dateAndTime == null) {
            return -1;
        }
        
        return (dateAndTime.get(Calendar.HOUR_OF_DAY) * 3600) + (dateAndTime.get(Calendar.MINUTE) * 60) + dateAndTime.get(Calendar.SECOND);
    }
    
    public static Calendar getCalendarFromDateAndSecondsIntoDay(Calendar date, int secondsIntoDay) {
        
        if ((date == null) || (secondsIntoDay < 0)) {
            return null;
        }
            
        Calendar calendar = getCalendarWithSameDateAtStartofDay(date);
        calendar.add(Calendar.SECOND, secondsIntoDay);

        return calendar;
    }
    
    public static Timestamp getTimestampFromDateAndSecondsIntoDay(Calendar date, int secondsIntoDay) {
        
        if ((date == null) || (secondsIntoDay < 0)) {
            return null;
        }
            
        Calendar calendar = getCalendarWithSameDateAtStartofDay(date);
        calendar.add(Calendar.SECOND, secondsIntoDay);
        
        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
                
        return timestamp;
    }
    
    public static boolean isSameDate(Calendar dateAndTime1, Calendar dateAndTime2) {
        
        if ((dateAndTime1 == null) || (dateAndTime2 == null)) {
            return false;
        }
        
        if ((dateAndTime1.get(Calendar.DATE) == dateAndTime2.get(Calendar.DATE)) && 
                (dateAndTime1.get(Calendar.MONTH) == dateAndTime2.get(Calendar.MONTH)) && 
                (dateAndTime1.get(Calendar.YEAR) == dateAndTime2.get(Calendar.YEAR))) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public static boolean isSameDate(Date dateAndTime1, Date dateAndTime2) {
        
        if ((dateAndTime1 == null) || (dateAndTime2 == null)) {
            return false;
        }

        if ((dateAndTime1.getDate() == dateAndTime2.getDate()) && 
                (dateAndTime1.getMonth() == dateAndTime2.getMonth()) && 
                (dateAndTime1.getYear() == dateAndTime2.getYear())) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public static boolean isSameDate(Timestamp dateAndTime1, Timestamp dateAndTime2) {
        
        if ((dateAndTime1 == null) || (dateAndTime2 == null)) {
            return false;
        }

        if ((dateAndTime1.getDate() == dateAndTime2.getDate()) && 
                (dateAndTime1.getMonth() == dateAndTime2.getMonth()) && 
                (dateAndTime1.getYear() == dateAndTime2.getYear())) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public static boolean isSameDate(Timestamp dateAndTime1, Calendar dateAndTime2) {
        
        if ((dateAndTime1 == null) || (dateAndTime2 == null)) {
            return false;
        }

        Calendar dateAndTimeCalendar1 = Calendar.getInstance();
        dateAndTimeCalendar1.setTimeInMillis(dateAndTime1.getTime());
        boolean isSameDate = DateAndTime.isSameDate(dateAndTime2, dateAndTimeCalendar1);  
        
        return isSameDate;
    }
    
    public static Calendar getCalendarWithSameDateAtDifferentTime(Calendar date, int hourOfDay, int minute, int second, int millisecond) { 
        
        if (date == null) {
            return null;
        }
        
        Calendar newCalendarSameDateDifferentTime = (Calendar) date.clone();
        newCalendarSameDateDifferentTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        newCalendarSameDateDifferentTime.set(Calendar.MINUTE, minute);
        newCalendarSameDateDifferentTime.set(Calendar.SECOND, second);
        newCalendarSameDateDifferentTime.set(Calendar.MILLISECOND, millisecond);  
        
        return newCalendarSameDateDifferentTime;
    }
    
    public static Calendar getCalendarWithSameDateAtDifferentTime(Date date, int hourOfDay, int minute, int second, int millisecond) { 
        
        if (date == null) {
            return null;
        }
        
        Calendar newCalendarSameDateDifferentTime = Calendar.getInstance();
        newCalendarSameDateDifferentTime.setTimeInMillis(date.getTime());
        newCalendarSameDateDifferentTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        newCalendarSameDateDifferentTime.set(Calendar.MINUTE, minute);
        newCalendarSameDateDifferentTime.set(Calendar.SECOND, second);
        newCalendarSameDateDifferentTime.set(Calendar.MILLISECOND, millisecond);  
        
        return newCalendarSameDateDifferentTime;
    }
    
    public static Calendar getCalendarWithSameDateAtDifferentTime(long date_UnixTimeMilliseconds, int hourOfDay, int minute, int second, int millisecond) { 
        
        if (date_UnixTimeMilliseconds < 0) {
            return null;
        }
        
        Calendar newCalendarSameDateDifferentTime = Calendar.getInstance();
        newCalendarSameDateDifferentTime.setTimeInMillis(date_UnixTimeMilliseconds);
        newCalendarSameDateDifferentTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        newCalendarSameDateDifferentTime.set(Calendar.MINUTE, minute);
        newCalendarSameDateDifferentTime.set(Calendar.SECOND, second);
        newCalendarSameDateDifferentTime.set(Calendar.MILLISECOND, millisecond);  
        
        return newCalendarSameDateDifferentTime;
    }
    
    public static Calendar getCalendarWithSameDateAtStartofDay(Calendar dateAndTime) {
        
        if (dateAndTime == null) {
            return null;
        }
        
        return getCalendarWithSameDateAtDifferentTime(dateAndTime, 0, 0, 0, 0);
    }
    
    public static Calendar getCalendarWithSameDateAtEndofDay(Calendar dateAndTime) {
        
        if (dateAndTime == null) {
            return null;
        }
        
        return getCalendarWithSameDateAtDifferentTime(dateAndTime, 23, 59, 59, 999);
    }
    
    public static Timestamp getTimestampWithSameDateAtStartOfDay(Calendar dateAndTime) {
        
        if (dateAndTime == null) {
            return null;
        }
        
        Calendar calendar = getCalendarWithSameDateAtDifferentTime(dateAndTime, 0, 0, 0, 0);
        
        if (calendar == null) {
            return null;
        }
        
        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        return timestamp;
    }
    
    public static Timestamp getTimestampWithSameDateAtEndOfDay(Calendar dateAndTime) {
        
        if (dateAndTime == null) {
            return null;
        }
        
        Calendar calendar = getCalendarWithSameDateAtDifferentTime(dateAndTime, 23, 59, 59, 999);
        
        if (calendar == null) {
            return null;
        }
        
        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        return timestamp;
    }
    
    public static Timestamp getTimestampWithSameDateAtStartOfDay(Timestamp dateAndTime) {
        
        if (dateAndTime == null) {
            return null;
        }
        
        Calendar dateAndTimeCalendar = Calendar.getInstance();
        dateAndTimeCalendar.setTimeInMillis(dateAndTime.getTime());
        
        Calendar calendar = getCalendarWithSameDateAtDifferentTime(dateAndTimeCalendar, 0, 0, 0, 0);
        
        if (calendar == null) {
            return null;
        }
        
        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        return timestamp;
    }
    
    public static Timestamp getTimestampWithSameDateAtEndOfDay(Timestamp dateAndTime) {
        
        if (dateAndTime == null) {
            return null;
        }
        
        Calendar dateAndTimeCalendar = Calendar.getInstance();
        dateAndTimeCalendar.setTimeInMillis(dateAndTime.getTime());
        
        Calendar calendar = getCalendarWithSameDateAtDifferentTime(dateAndTimeCalendar, 23, 59, 59, 999);
        
        if (calendar == null) {
            return null;
        }
        
        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        return timestamp;
    }

    public static Calendar getCalendarWithTodaysDateAtSpecifiedTime(String specifiedTime, String simpleDateFormat) {
        
        if ((specifiedTime == null) || specifiedTime.isEmpty() || (simpleDateFormat == null)) {
            return null;
        }
        
        try {
            Calendar specifiedTimeCalendar = getCalendarFromFormattedString(specifiedTime, simpleDateFormat);

            Calendar currentDateAndTime = Calendar.getInstance();
            Calendar todayAtSpecifiedTime = getCalendarWithSameDateAtDifferentTime(currentDateAndTime, 
                    specifiedTimeCalendar.get(Calendar.HOUR_OF_DAY), 
                    specifiedTimeCalendar.get(Calendar.MINUTE), 
                    specifiedTimeCalendar.get(Calendar.SECOND), 
                    specifiedTimeCalendar.get(Calendar.MILLISECOND));

            return todayAtSpecifiedTime;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public static Timestamp addOrSubtractTime(Timestamp dateAndTime, int calendarField, int changeValue) {
        
        if (dateAndTime == null) {
            return null;
        }
        
        Calendar dateAndTimeCalendar = Calendar.getInstance();
        dateAndTimeCalendar.setTimeInMillis(dateAndTime.getTime());
        dateAndTimeCalendar.add(calendarField, changeValue);

        Timestamp timestamp = new Timestamp(dateAndTimeCalendar.getTimeInMillis());
        return timestamp;
    }
    
    public static String getFormattedStringFromDate(Calendar dateAndTime, String simpleDateFormat) {
        
        if ((dateAndTime == null) || (simpleDateFormat == null)) {
            return null;
        }
        
        try {
            DateFormat formatter = new SimpleDateFormat(simpleDateFormat);
            String formattedCalendarString = formatter.format(dateAndTime.getTime());

            return formattedCalendarString;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public static Calendar getCalendarFromFormattedDateString(String date, String simpleDateFormat) {
        
        if ((date == null) || (simpleDateFormat == null)) {
            return null;
        }
        
        try {
            Calendar dateCalendar;

            if (date.equalsIgnoreCase("now")) {
                dateCalendar = Calendar.getInstance();
                dateCalendar = getCalendarWithSameDateAtStartofDay(dateCalendar);
            }
            else if (date.equalsIgnoreCase("today")) {
                dateCalendar = Calendar.getInstance();
                dateCalendar = getCalendarWithSameDateAtStartofDay(dateCalendar);
            }
            else if (date.equalsIgnoreCase("yesterday")) {
                dateCalendar = Calendar.getInstance();
                dateCalendar.add(Calendar.DATE, -1);
                dateCalendar = getCalendarWithSameDateAtStartofDay(dateCalendar);
            }
            else {
                dateCalendar = getCalendarFromFormattedString(date, simpleDateFormat);
            }

            dateCalendar = getCalendarWithSameDateAtStartofDay(dateCalendar);

            return dateCalendar;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public static Calendar getCalendarFromFormattedString(String dateAndTime, String simpleDateFormat) {
        
        if ((dateAndTime == null) || (simpleDateFormat == null)) {
            return null;
        }
        
        try {
            DateFormat formatter = new SimpleDateFormat(simpleDateFormat);
            Date dateDateAndTime = formatter.parse(dateAndTime);

            Calendar calendarDateAndTime = Calendar.getInstance();
            calendarDateAndTime.setTimeInMillis(dateDateAndTime.getTime());
            
            return calendarDateAndTime;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    
    public static Date getDateFromFormattedString(String dateAndTime, String simpleDateFormat) {
        
        if ((dateAndTime == null) || (simpleDateFormat == null)) {
            return null;
        }
        
        try {
            DateFormat formatter = new SimpleDateFormat(simpleDateFormat);
            Date dateDateAndTime = formatter.parse(dateAndTime);
            
            return dateDateAndTime;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public static Timestamp convertToTimestamp(Calendar dateAndTime) {
        
        if (dateAndTime == null) {
            return null;
        }
        
        Timestamp dateAndTimeTimestamp = new Timestamp(dateAndTime.getTimeInMillis());
        
        return dateAndTimeTimestamp;
    }
    
    public static Calendar convertTimestampToCalendar(Timestamp dateAndTime) {
        
        if (dateAndTime == null) {
            return null;
        }
        
        Calendar dateAndTimeCalendar = Calendar.getInstance();
        dateAndTimeCalendar.setTimeInMillis(dateAndTime.getTime());
        
        return dateAndTimeCalendar;
    }
    
    public static boolean isBeforeDate(String dateAndTime1, String dateAndTime2, String simpleDateFormat) {
        
        if ((dateAndTime1 == null) || (dateAndTime2 == null) || (simpleDateFormat == null)) {
            return false;
        }
        
        boolean isValidFormattedDate1 = isValidFormattedDate(simpleDateFormat, dateAndTime1);
        boolean isValidFormattedDate2 = isValidFormattedDate(simpleDateFormat, dateAndTime2);
        if (!isValidFormattedDate1 || !isValidFormattedDate2) {
            return false;
        }
        
        Date dateDateAndTime1 = getDateFromFormattedString(dateAndTime1, simpleDateFormat);
        Date dateDateAndTime2 = getDateFromFormattedString(dateAndTime2, simpleDateFormat);
        
        return dateDateAndTime1.before(dateDateAndTime2);
    }
    
    public static boolean isAfterDate(String dateAndTime1, String dateAndTime2, String simpleDateFormat) {
        
        if ((dateAndTime1 == null) || (dateAndTime2 == null) || (simpleDateFormat == null)) {
            return false;
        }
        
        boolean isValidFormattedDate1 = isValidFormattedDate(simpleDateFormat, dateAndTime1);
        boolean isValidFormattedDate2 = isValidFormattedDate(simpleDateFormat, dateAndTime2);
        if (!isValidFormattedDate1 || !isValidFormattedDate2) {
            return false;
        }
        
        Date dateDateAndTime1 = getDateFromFormattedString(dateAndTime1, simpleDateFormat);
        Date dateDateAndTime2 = getDateFromFormattedString(dateAndTime2, simpleDateFormat);
        
        return dateDateAndTime1.after(dateDateAndTime2);
    }
    
    public static boolean isEqualDate(String dateAndTime1, String dateAndTime2, String simpleDateFormat) {
        
        if ((dateAndTime1 == null) || (dateAndTime2 == null) || (simpleDateFormat == null)) {
            return false;
        }
        
        boolean isValidFormattedDate1 = isValidFormattedDate(simpleDateFormat, dateAndTime1);
        boolean isValidFormattedDate2 = isValidFormattedDate(simpleDateFormat, dateAndTime2);
        if (!isValidFormattedDate1 || !isValidFormattedDate2) {
            return false;
        }
        
        Date dateDateAndTime1 = getDateFromFormattedString(dateAndTime1, simpleDateFormat);
        Date dateDateAndTime2 = getDateFromFormattedString(dateAndTime2, simpleDateFormat);
        
        return dateDateAndTime1.equals(dateDateAndTime2);
    }
    
    /*
     * required format- HH:mm 
     * returns 1 if (the specified time > the current
     * time) returns -1 if (the specified time < the current time) returns 0 if
     * (the specified time = the current time) returns the value of
     * COMPARE_ERROR_CODE if there is an error
     */
    public static int Compare_CurrentTimeVsSpecifiedTime(String hoursAndMinutes) {
        
        if (hoursAndMinutes == null) {
            return COMPARE_ERROR_CODE;
        }
        
        try {
            Calendar todayAtSpecifiedTime = getCalendarWithTodaysDateAtSpecifiedTime(hoursAndMinutes, "HH:mm");

            Calendar currentSystemDateAndTime = Calendar.getInstance();
            int comparisonResult = currentSystemDateAndTime.compareTo(todayAtSpecifiedTime);

            return comparisonResult;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return COMPARE_ERROR_CODE;
        }
    }
}
