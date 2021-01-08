package com.pearson.statsagg.database_objects;

import com.pearson.statsagg.utilities.json_utils.JsonBigDecimal;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseObjectCommon {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseObjectCommon.class.getName());

    public static final int TIME_UNIT_DAYS = 71;
    public static final int TIME_UNIT_HOURS = 72;
    public static final int TIME_UNIT_MINUTES = 73;
    public static final int TIME_UNIT_SECONDS = 74;
    public static final int TIME_UNIT_MILLISECONDS = 75;
    
    public static final BigDecimal MILLISECONDS_PER_SECOND = new BigDecimal(1000);
    public static final BigDecimal MILLISECONDS_PER_MINUTE = new BigDecimal(60000);
    public static final BigDecimal MILLISECONDS_PER_HOUR = new BigDecimal(3600000);
    public static final BigDecimal MILLISECONDS_PER_DAY = new BigDecimal(86400000);

    public static final int TIME_UNIT_SCALE = 7;
    public static final int TIME_UNIT_PRECISION = 31;
    public static final RoundingMode TIME_UNIT_ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final MathContext TIME_UNIT_MATH_CONTEXT = new MathContext(TIME_UNIT_PRECISION, TIME_UNIT_ROUNDING_MODE);
    
    public static String getTimeUnitStringFromCode(Integer timeUnitCode, boolean outputLowercase) {
        
        if ((timeUnitCode == null)) {
            return null;
        }

        if (timeUnitCode == TIME_UNIT_DAYS) {
            if (outputLowercase) return "days";
            return "Days";
        }
        else if (timeUnitCode == TIME_UNIT_HOURS) {
            if (outputLowercase) return "hours";
            return "Hours";
        }
        else if (timeUnitCode == TIME_UNIT_MINUTES) {
            if (outputLowercase) return "minutes";
            return "Minutes";
        }
        else if (timeUnitCode == TIME_UNIT_SECONDS) {
            if (outputLowercase) return "seconds";
            return "Seconds";
        }
        else logger.warn("Unrecognized time unit");
         
        return null;
    }
    
    public static Integer getTimeUnitCodeFromString(String timeUnit) {
        
        if ((timeUnit == null) || timeUnit.isEmpty()) {
            return null;
        }
        
        if (timeUnit.equalsIgnoreCase("Days")) return TIME_UNIT_DAYS;
        else if (timeUnit.equalsIgnoreCase("Hours")) return TIME_UNIT_HOURS;
        else if (timeUnit.equalsIgnoreCase("Minutes")) return TIME_UNIT_MINUTES;
        else if (timeUnit.equalsIgnoreCase("Seconds")) return TIME_UNIT_SECONDS;
        else logger.warn("Unrecognized time unit code");
        
        return null;
    }
    
    public static BigDecimal getMillisecondValueForTime(BigDecimal time, Integer timeUnitCode) {
        
        if ((time == null) || (timeUnitCode == null)) {
            return null;
        }
        
        if (timeUnitCode == TIME_UNIT_SECONDS) return MathUtilities.smartBigDecimalScaleChange(time.multiply(MILLISECONDS_PER_SECOND), TIME_UNIT_SCALE, TIME_UNIT_ROUNDING_MODE);
        else if (timeUnitCode == TIME_UNIT_MINUTES) return MathUtilities.smartBigDecimalScaleChange(time.multiply(MILLISECONDS_PER_MINUTE), TIME_UNIT_SCALE, TIME_UNIT_ROUNDING_MODE);
        else if (timeUnitCode == TIME_UNIT_HOURS) return MathUtilities.smartBigDecimalScaleChange(time.multiply(MILLISECONDS_PER_HOUR), TIME_UNIT_SCALE, TIME_UNIT_ROUNDING_MODE);
        else if (timeUnitCode == TIME_UNIT_DAYS) return MathUtilities.smartBigDecimalScaleChange(time.multiply(MILLISECONDS_PER_DAY), TIME_UNIT_SCALE, TIME_UNIT_ROUNDING_MODE);
        
        return null;
    }
    
    public static BigDecimal getValueForTimeFromMilliseconds(Long timeInMs, Integer timeUnitCode) {
        
        if ((timeInMs == null) || (timeUnitCode == null)) {
            return null;
        }
        
        BigDecimal timeInMs_BigDecimal = new BigDecimal(timeInMs);
        
        if (timeUnitCode == TIME_UNIT_SECONDS) return timeInMs_BigDecimal.divide(MILLISECONDS_PER_SECOND, TIME_UNIT_SCALE, TIME_UNIT_ROUNDING_MODE).stripTrailingZeros();
        else if (timeUnitCode == TIME_UNIT_MINUTES) return timeInMs_BigDecimal.divide(MILLISECONDS_PER_MINUTE, TIME_UNIT_SCALE, TIME_UNIT_ROUNDING_MODE).stripTrailingZeros();
        else if (timeUnitCode == TIME_UNIT_HOURS) return timeInMs_BigDecimal.divide(MILLISECONDS_PER_HOUR, TIME_UNIT_SCALE, TIME_UNIT_ROUNDING_MODE).stripTrailingZeros();
        else if (timeUnitCode == TIME_UNIT_DAYS) return timeInMs_BigDecimal.divide(MILLISECONDS_PER_DAY, TIME_UNIT_SCALE, TIME_UNIT_ROUNDING_MODE).stripTrailingZeros();
        
        return null;
    }
    
    public static JsonObject getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(JsonObject jsonObject, String time_FieldName, String timeUnit_FieldName) {
        
        if (jsonObject == null) {
            return null;
        }
        
        try {
            JsonElement time_jsonElement = jsonObject.get(time_FieldName);
            JsonElement timeUnit_JsonElement = jsonObject.get(timeUnit_FieldName);

            if ((time_jsonElement != null) && (timeUnit_JsonElement != null)) {
                long currentField_JsonElement_Long = time_jsonElement.getAsLong();
                int timeUnit_Int = timeUnit_JsonElement.getAsInt();
                BigDecimal time_BigDecimal = DatabaseObjectCommon.getValueForTimeFromMilliseconds(currentField_JsonElement_Long, timeUnit_Int);
                
                jsonObject.remove(time_FieldName);
                JsonBigDecimal time_JsonBigDecimal = new JsonBigDecimal(time_BigDecimal);
                jsonObject.addProperty(time_FieldName, time_JsonBigDecimal);
                
                jsonObject.remove(timeUnit_FieldName);
                jsonObject.addProperty(timeUnit_FieldName, DatabaseObjectCommon.getTimeUnitStringFromCode(timeUnit_Int, false));
            }
            else if (time_jsonElement != null) {
                jsonObject.remove(time_FieldName);
            }
            else if (timeUnit_JsonElement != null) {
                jsonObject.remove(timeUnit_FieldName);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return jsonObject;
    }
    
    public static String trimNewLineDelimitedString(String newLineDelimitedString) {
        
        if ((newLineDelimitedString == null) || newLineDelimitedString.isEmpty()) {
            return newLineDelimitedString;
        }
        
        StringBuilder stringBuilder = new StringBuilder();

        List<String> strings = StringUtilities.getListOfStringsFromDelimitedString(newLineDelimitedString, '\n');
        
        if ((strings != null) && !strings.isEmpty()) {
            for (String string : strings) {
                String trimmedString = string.trim();
                if (!trimmedString.isEmpty()) stringBuilder.append(trimmedString).append("\n");
            }
        }
        
        return stringBuilder.toString().trim();
    }
    
}
