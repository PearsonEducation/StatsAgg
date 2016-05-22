package com.pearson.statsagg.webui.api;

import java.math.BigDecimal;

/**
 * @author Jeffrey Schmidt
 */
public class JsonBigDecimal extends BigDecimal {

    public JsonBigDecimal(String val) {
        super(val);
    }

    public JsonBigDecimal(BigDecimal bigDecimal) {
        super(bigDecimal.toPlainString());
    }

    @Override
    public String toString() {
        return this.stripTrailingZeros().toPlainString();
    }
    
}