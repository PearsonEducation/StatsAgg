package com.pearson.statsagg.globals;

import static com.pearson.statsagg.metric_aggregation.aggregators.StatsdMetricAggregator.STATSD_MATH_CONTEXT;
import static com.pearson.statsagg.metric_aggregation.aggregators.StatsdMetricAggregator.STATSD_ROUNDING_MODE;
import static com.pearson.statsagg.metric_aggregation.aggregators.StatsdMetricAggregator.STATSD_SCALE;
import com.pearson.statsagg.utilities.MathUtilities;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class StatsdNthPercentiles {
    
    private static final Logger logger = LoggerFactory.getLogger(StatsdNthPercentiles.class.getName());

    private final String nthPercentiles_Csv_;
    private final List<String> nthPercentiles_CleanStrings_;
    private final List<String> nthPercentiles_CleanStrings_StatsdFormatted_;
    private final List<BigDecimal> nthPercentiles_Fractional_;
    
    public StatsdNthPercentiles(String nthPercentiles_Csv) {
        this.nthPercentiles_Csv_ = nthPercentiles_Csv;
        this.nthPercentiles_CleanStrings_ = new ArrayList<>();
        this.nthPercentiles_CleanStrings_StatsdFormatted_ = new ArrayList<>();
        this.nthPercentiles_Fractional_ = new ArrayList<>();
        
        String[] nthPercentile_Strings = StringUtils.split(nthPercentiles_Csv, ',');
        
        if (nthPercentile_Strings != null) {
            for (String nthPercentile_String : nthPercentile_Strings) {
                try {
                    BigDecimal statsdNthPercentile_BigDecimal = MathUtilities.smartBigDecimalScaleChange(new BigDecimal(nthPercentile_String.trim()), STATSD_SCALE, STATSD_ROUNDING_MODE);
                    String nthPercentile_CleanString = statsdNthPercentile_BigDecimal.stripTrailingZeros().toPlainString();
                    BigDecimal nthPercentile_Fractional_ = statsdNthPercentile_BigDecimal.divide(new BigDecimal("100"), STATSD_MATH_CONTEXT);

                    nthPercentiles_CleanStrings_.add(nthPercentile_CleanString);
                    nthPercentiles_CleanStrings_StatsdFormatted_.add(nthPercentile_CleanString.replace('.', '_').replace("-", "top"));
                    nthPercentiles_Fractional_.add(nthPercentile_Fractional_);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }       
        }
    }

    public String getNthPercentiles_Csv() {
        return nthPercentiles_Csv_;
    }

    public List<String> getNthPercentiles_CleanStrings() {
        return nthPercentiles_CleanStrings_;
    }
    
    public List<String> getNthPercentiles_CleanStrings_StatsdFormatted() {
        return nthPercentiles_CleanStrings_StatsdFormatted_;
    }
    
    public List<BigDecimal> getNthPercentiles_Fractional() {
        return nthPercentiles_Fractional_;
    }    
    
}
