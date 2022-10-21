package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.DoubleFunction;

import static org.junit.Assert.*;

public class RunAsnbReportTaskletTest {

    private RunAsnbReportTasklet sut;

    @Test
    public void testTopupAmountFormat(){
        sut = new RunAsnbReportTasklet();

        double test1 = 956.00;
        double test2 = 8888.90;
        double test3 = 123456789123.12;

        Assert.assertEquals("00000000000095600", sut.getTopupAmountFormatted().apply(test1));
        Assert.assertEquals("00000000000888890", sut.getTopupAmountFormatted().apply(test2));
        Assert.assertEquals("00012345678912312", sut.getTopupAmountFormatted().apply(test3));

        Assert.assertEquals(17, sut.getTopupAmountFormatted().apply(test1).length());
        Assert.assertEquals(17, sut.getTopupAmountFormatted().apply(test2).length());
        Assert.assertEquals(17, sut.getTopupAmountFormatted().apply(test3).length());
    }
}
