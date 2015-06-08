package com.pearson.statsagg.globals;

import com.google.common.collect.ImmutableList;
import com.pearson.statsagg.utilities.JsonUtils;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.boon.Boon;
import org.boon.core.value.LazyValueMap;
import org.boon.core.value.ValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class StatsdHistogramConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(StatsdHistogramConfiguration.class.getName());
    
    private final String metric_; 
    private final ImmutableList<BigDecimal> bins_BigDecimal_;
    private final ImmutableList<String> bins_String_;
    private final ImmutableList<String> bins_GraphiteFriendlyString_;
    private final boolean isInfDetected_;
    
    public StatsdHistogramConfiguration(String metric, ImmutableList<BigDecimal> bins_BigDecimal, 
            ImmutableList<String> bins_String, ImmutableList<String> bins_GraphiteFriendlyString, boolean isInfDetected) {
        this.metric_ = metric;
        this.bins_BigDecimal_ = bins_BigDecimal;
        this.bins_String_ = bins_String;
        this.bins_GraphiteFriendlyString_ = bins_GraphiteFriendlyString;
        this.isInfDetected_ = isInfDetected;
    }
    
    /*
    Returns a list of StatsD histogram configurations. The order of the histogram configurations is preserved, but the bins are manipulated for correctness.
    Bins values are sorted numerically, remove bins that are <= 0, de-duplicated, and only allow one string -- "inf" (which must be the last bin value).
    */
    public static List<StatsdHistogramConfiguration> getStatsdHistogramConfigurations(String unparsedStatsdHistogramConfigurations) {
        
        if ((unparsedStatsdHistogramConfigurations == null) || unparsedStatsdHistogramConfigurations.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<StatsdHistogramConfiguration> statsdHistogramConfigurations = new ArrayList<>();
        
        try {
            Object parsedJsonObject = (Object) Boon.fromJson(unparsedStatsdHistogramConfigurations);
            ValueList parsedJsonValueList = null;
            if ((parsedJsonObject != null) && (parsedJsonObject instanceof ValueList)) parsedJsonValueList = (ValueList) Boon.fromJson(unparsedStatsdHistogramConfigurations);
            
            if (parsedJsonValueList == null) return new ArrayList<>();

            for (Object statsdHistogramConfigurationObject : parsedJsonValueList) {
                try {
                    LazyValueMap statsdHistogramConfiguration = (LazyValueMap) statsdHistogramConfigurationObject;

                    String metric = (String) statsdHistogramConfiguration.get("metric");
                    if (metric != null) metric = metric.trim();

                    List<String> binValues_String = new ArrayList<>();
                    List<String> binValues_GraphiteFriendlyString = new ArrayList<>();
                    Set<BigDecimal> binValues_BigDecimal = new TreeSet<>();
                    boolean isInfDetected = false;

                    if (statsdHistogramConfiguration.get("bins") instanceof ValueList) {
                        ValueList binObjects = (ValueList) statsdHistogramConfiguration.get("bins");

                        for (Object binObject : binObjects) {
                            Object binsObject = (Object) binObject;
                            String binValue = JsonUtils.convertNumericObjectToString(binsObject, true);

                            if (binValue != null) {
                                BigDecimal binValue_BigDecimal = new BigDecimal(binValue.trim()).stripTrailingZeros();
                                if (binValue_BigDecimal.compareTo(BigDecimal.ZERO) == 1) binValues_BigDecimal.add(binValue_BigDecimal);
                            }

                            if ((binsObject != null) && (binValue == null) && (binsObject instanceof String)) {
                                binValue = (String) binsObject;
                                if (binValue.trim().equalsIgnoreCase("inf")) isInfDetected = true;
                            }
                        }
                    }

                    for (BigDecimal binValue : binValues_BigDecimal) {
                        String binValue_String = binValue.stripTrailingZeros().toPlainString();
                        binValues_String.add(binValue_String);
                        binValues_GraphiteFriendlyString.add(binValue_String.replace('.', '_'));
                    }

                    if (isInfDetected) {
                        binValues_String.add("inf");
                        binValues_GraphiteFriendlyString.add("inf");
                    }

                    if (metric != null) {
                        ImmutableList<BigDecimal> bigValues_BigDecimal_Immutable = ImmutableList.copyOf(binValues_BigDecimal);
                        ImmutableList<String> bigValues_String_Immutable = ImmutableList.copyOf(binValues_String);
                        ImmutableList<String> bigValues_GraphiteFriendlyString_Immutable = ImmutableList.copyOf(binValues_GraphiteFriendlyString);
                        StatsdHistogramConfiguration statsdHistogram = new StatsdHistogramConfiguration(metric, bigValues_BigDecimal_Immutable, 
                                bigValues_String_Immutable, bigValues_GraphiteFriendlyString_Immutable, isInfDetected);
                        statsdHistogramConfigurations.add(statsdHistogram);
                    }
                }
                catch (Exception e) {
                    logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return statsdHistogramConfigurations;
    }

    @Override
    public String toString() {
        StringBuilder histogramConfiguration = new StringBuilder();
        
        histogramConfiguration.append("{").append("metric:'").append(metric_).append("'").append(",bins:[");
        
        if (bins_String_ != null) {
            for (int i = 0; i < bins_String_.size(); i++) {
                if (bins_String_.get(i).equals("inf")) histogramConfiguration.append("'").append(bins_String_.get(i)).append("'");
                else histogramConfiguration.append(bins_String_.get(i));
                    
                if (((i + 1) != bins_String_.size())) histogramConfiguration.append(",");
            }
        }
        
        histogramConfiguration.append("]}");
        
        return histogramConfiguration.toString();
    }
    
    public String getMetric() {
        return metric_;
    }

    public ImmutableList<BigDecimal> getBins_BigDecimal() {
        return bins_BigDecimal_;
    }
    
    public ImmutableList<String> getBins_String() {
        return bins_String_;
    }

    public ImmutableList<String> getBins_GraphiteFriendlyString() {
        return bins_GraphiteFriendlyString_;
    }
    
    public boolean isInfDetected() {
        return isInfDetected_;
    }

}
