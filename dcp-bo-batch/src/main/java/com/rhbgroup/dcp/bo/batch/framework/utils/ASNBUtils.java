package com.rhbgroup.dcp.bo.batch.framework.utils;

public final class ASNBUtils {

    private ASNBUtils() {}

    /**
     *  This method use to concatenate field '4 RIMCIMOP-DR-RECIP-REF' and '4 RIMCIMOP-CR-RECIP-REF'
     *  for auto-settlement
     *
     * @param prefix
     * @param fundCode
     * @return
     */
    public static String getDRCRRecipRef(String prefix, String fundCode) {

        String prefixValue = "";
        String fundCodeValue = "";

        if(prefix != null) prefixValue = prefix;
        if(fundCode != null) fundCodeValue = fundCode;

        return prefixValue.concat(fundCodeValue);
    }
}
