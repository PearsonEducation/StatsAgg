package com.pearson.statsagg.utilities.math_utils;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MathUtilities {
    
    private static final Logger logger = LoggerFactory.getLogger(MathUtilities.class.getName());
    
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    
    public static double computeSmallerNumber(double number1, double number2) {
        if (number1 <= number2) {
            return number1;
        }
        else {
            return number2;
        }
    }
    
    public static double computeLargerNumber(double number1, double number2) {
        if (number1 >= number2) {
            return number1;
        }
        else {
            return number2;
        }
    }

    public static double computePercentChange(double oldValue, double newValue) {
        
        if (oldValue == 0) {
            throw new IllegalArgumentException("oldValue cannot be 0");
        }
        
        double percentChange = ((newValue - oldValue) / oldValue) * 100;
        
        return percentChange;
    }
    
    public static boolean areBigDecimalsNumericallyEqual(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        
        boolean isEqual = false;
        
        if ((bigDecimal1 != null) && (bigDecimal2 != null)) {
            isEqual = (bigDecimal1.compareTo(bigDecimal2) == 0);
        }
        else if (bigDecimal1 == null) {
            isEqual = (bigDecimal2 == null);
        }
        
        return isEqual;
    }
    
    public static Long computeMedianOfLongs(List<Long> numbers) {
        
        if ((numbers == null) || (numbers.isEmpty())) {
            return null;
        }
        
        if (numbers.size() == 1) {
            return numbers.get(0);
        }
        
        Long median;
        
        List<Long> localNumbers = new ArrayList(numbers);
        Collections.sort(localNumbers);
        
        boolean isOddSized = (localNumbers.size() % 2) == 1;
        int medianIndex = localNumbers.size() / 2;

        if (isOddSized) {
            median = localNumbers.get(medianIndex);
        }
        else {
            median = (localNumbers.get(medianIndex - 1) + localNumbers.get(medianIndex)) / 2;
        }        
    
        return median;
    }
    
    public static Double computeMedianOfDoubles(List<Double> numbers) {
        
        if ((numbers == null) || (numbers.isEmpty())) {
            return null;
        }
        
        if (numbers.size() == 1) {
            return numbers.get(0);
        }
        
        Double median;
        
        List<Double> localNumbers = new ArrayList(numbers);
        Collections.sort(localNumbers);
        
        boolean isOddSized = (localNumbers.size() % 2) == 1;
        int medianIndex = localNumbers.size() / 2;

        if (isOddSized) {
            median = localNumbers.get(medianIndex);
        }
        else {
            median = (localNumbers.get(medianIndex - 1) + localNumbers.get(medianIndex)) / 2;
        }        
    
        return median;
    }
    
    public static BigDecimal computeMedianOfBigDecimals(List<BigDecimal> numbers, MathContext mathContext, boolean areNumbersAlreadySorted) {
        
        if ((numbers == null) || (numbers.isEmpty())) {
            return null;
        }
        
        if (numbers.size() == 1) {
            return numbers.get(0);
        }
        
        BigDecimal median;
        
        List<BigDecimal> sortedNumbers;
        if (!areNumbersAlreadySorted) {
            sortedNumbers = new ArrayList(numbers);
            Collections.sort(sortedNumbers);
        }
        else {
            sortedNumbers = numbers;
        }
        
        boolean isOddSized = (sortedNumbers.size() % 2) == 1;
        int medianIndex = sortedNumbers.size() / 2;

        if (isOddSized) {
            median = sortedNumbers.get(medianIndex);
        }
        else {
            median = sortedNumbers.get(medianIndex - 1).add(sortedNumbers.get(medianIndex));
            median = median.divide(new BigDecimal(2), mathContext);
        }        
    
        return median;
    }

    public static BigDecimal smartBigDecimalScaleChange(BigDecimal number, int scale, RoundingMode roundingMode) {
        
        if ((number == null) || (roundingMode == null)) {
            return null;
        }
        
        if ((number.scale() >= 0) && (scale >= 0)) {
            if (number.scale() <= scale) {
                return number;
            }
            else {
                return number.setScale(scale, roundingMode);
            }
        }
        else {
            return number.setScale(scale, roundingMode);
        }
    }
    
    public static BigDecimal computePopulationStandardDeviationOfBigDecimals(List<BigDecimal> numbers) {
        
        if ((numbers == null) || numbers.isEmpty()) {
            return null;
        }
        
        try {
            double[] doublesArray = new double[numbers.size()];

            for (int i = 0; i < doublesArray.length; i++) {
                doublesArray[i] = numbers.get(i).doubleValue();
            }

            StandardDeviation standardDeviation = new StandardDeviation();
            standardDeviation.setBiasCorrected(false);
            BigDecimal standardDeviationResult = new BigDecimal(standardDeviation.evaluate(doublesArray));

            return standardDeviationResult;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public static Long getSmallestValue(List<Long> values) {
        
        if ((values == null) || values.isEmpty()) {
            return null;
        }
        
        Long returnValue = null;
        int counter = 0;
        
        for (Long currentValue : values) {
            if (counter == 0) {
                returnValue = currentValue;
            }
            else if ((counter > 0) && (returnValue > currentValue)) {
                returnValue = currentValue;
            }
            
            counter++;        
        }
        
        return returnValue;
    }

    public static BigDecimal correctOutOfRangePercentage(BigDecimal input) {
        
        if (input == null) {
            return null;
        }
        
        if (input.compareTo(ONE_HUNDRED) > 0) {
            return ONE_HUNDRED;
        }
        
        if (input.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        
        return input;
    }
    
    public static BigDecimal safeGetBigDecimal(String numericString) {
        
        if (numericString == null) {
            return null;
        }
        
        BigDecimal bigDecimal;

        try {
            bigDecimal = new BigDecimal(numericString);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            bigDecimal = null;
        }

        return bigDecimal;
    }

    public static String getFastPlainStringWithNoTrailingZeros(BigDecimal bigDecimal) {
        
        if (bigDecimal == null) return null;
        
        String numericString = bigDecimal.toPlainString();
        if ((numericString == null) || (numericString.length() <= 1)) return numericString;
            
        try {
            for (int i=(numericString.length()-1); i >= 0; i--) {
                char currentChar = numericString.charAt(i);
                if (currentChar == '0') continue;
                if (currentChar == '.') return numericString.substring(0, i);
                if (currentChar != '0') {
                    for (int j = 0; j < i; j++) {
                        if (numericString.charAt(j) == '.') {
                            return numericString.substring(0, (i+1));
                        }
                    }
                    return numericString;
                }
            }
            
            return numericString;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return numericString;
        }
        
    }
    
    public static String convertNumericObjectToString(Object numericObject, boolean treatBooleanAsNumeric) {
        
        if (numericObject == null) return null;
        
        String valueString = null;
        
        if (numericObject instanceof Integer) valueString = Integer.toString((Integer) numericObject);
        else if (numericObject instanceof Long) valueString = Long.toString((Long) numericObject);
        else if (numericObject instanceof Short) valueString = Short.toString((Short) numericObject);
        else if (numericObject instanceof Byte) valueString = Byte.toString((Byte) numericObject);
        else if (numericObject instanceof Double) valueString = Double.toString((Double) numericObject);
        else if (numericObject instanceof Float) valueString = Float.toString((Float) numericObject);
        else if (numericObject instanceof BigDecimal) {
            BigDecimal numericObjectBigDecimal = (BigDecimal) numericObject;
            valueString = numericObjectBigDecimal.stripTrailingZeros().toPlainString();
        }
        else if (numericObject instanceof BigInteger) {
            BigInteger numericObjectBigInteger = (BigInteger) numericObject;
            valueString = numericObjectBigInteger.toString();
        }
        else if ((numericObject instanceof Boolean) && treatBooleanAsNumeric) {
            Boolean numberObjectBoolean = (Boolean) numericObject;
            if (numberObjectBoolean) valueString = "1";
            else valueString = "0";
        }
        else if ((numericObject instanceof Boolean) && !treatBooleanAsNumeric) {
            Boolean numberObjectBoolean = (Boolean) numericObject;
            if (numberObjectBoolean) valueString = "true";
            else valueString = "false";
        }
        
        return valueString;
    }
        
    public static BigDecimal convertNumericObjectToBigDecimal(Object numericObject, boolean treatBooleanAsNumeric) {
        
        if (numericObject == null) return null;
        
        BigDecimal valueBigDecimal = null;
        
        if (numericObject instanceof BigDecimal) valueBigDecimal = (BigDecimal) numericObject;
        else if (numericObject instanceof Integer) valueBigDecimal = new BigDecimal(Integer.toString((Integer) numericObject));
        else if (numericObject instanceof Long) valueBigDecimal = new BigDecimal(Long.toString((Long) numericObject));
        else if (numericObject instanceof Short) valueBigDecimal = new BigDecimal(Short.toString((Short) numericObject));
        else if (numericObject instanceof Byte) valueBigDecimal = new BigDecimal(Byte.toString((Byte) numericObject));
        else if (numericObject instanceof Double) valueBigDecimal = new BigDecimal(Double.toString((Double) numericObject));
        else if (numericObject instanceof Float) valueBigDecimal = new BigDecimal(Float.toString((Float) numericObject));
        else if (numericObject instanceof BigInteger) valueBigDecimal = new BigDecimal((BigInteger) numericObject);
        else if ((numericObject instanceof Boolean) && treatBooleanAsNumeric) {
            Boolean numberObjectBoolean = (Boolean) numericObject;
            if (numberObjectBoolean) valueBigDecimal = BigDecimal.ONE;
            else valueBigDecimal = BigDecimal.ZERO;
        }

        return valueBigDecimal;
    }
    
    public static boolean isObjectNumericType(Object object, boolean treatBooleanAsNumeric) {
        
        if (object == null) return false;
                
        if (object instanceof Integer) return true;
        else if (object instanceof Long) return true;
        else if (object instanceof Short) return true;
        else if (object instanceof Byte) return true;
        else if (object instanceof Double) return true;
        else if (object instanceof Float) return true;
        else if (object instanceof BigDecimal) return true;
        else if (object instanceof BigInteger) return true;
        else if ((object instanceof Boolean) && treatBooleanAsNumeric) return true;
        
        return false;
    }
    
    public static boolean isStringAnInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isStringABigDecimal(String input) {
        try {
            BigDecimal bigDecimal = new BigDecimal(input);
            return (bigDecimal != null);
        }
        catch (Exception e) {
            return false;
        }
    }
    
}
