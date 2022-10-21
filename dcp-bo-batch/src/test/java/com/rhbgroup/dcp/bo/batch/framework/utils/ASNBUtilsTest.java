package com.rhbgroup.dcp.bo.batch.framework.utils;

import org.junit.Assert;
import org.junit.Test;

public class ASNBUtilsTest {

    @Test
    public void getDRCRRecipRefwithDIBGoodScenario(){
        String expectedResult = "DIBASB";
        Assert.assertEquals(expectedResult, ASNBUtils.getDRCRRecipRef("DIB", "ASB"));
    }

    @Test
    public void getDRCRRecipRefwithDIBPrefixNull(){
        String expectedResult = "ASB";
        Assert.assertEquals(expectedResult, ASNBUtils.getDRCRRecipRef(null, "ASB"));
    }

    @Test
    public void getDRCRRecipRefwithDIBFundCodeNull(){
        String expectedResult = "DIB";
        Assert.assertEquals(expectedResult, ASNBUtils.getDRCRRecipRef("DIB", null));
    }
}
