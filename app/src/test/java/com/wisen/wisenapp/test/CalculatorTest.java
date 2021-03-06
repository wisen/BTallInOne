package com.wisen.wisenapp.test;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by wisen on 2016-07-23.
 */
public class CalculatorTest {

    private Calculator mCalculator;

    @Before
    public void setUp() throws Exception {
        mCalculator = new Calculator();
    }

    @Test
    public void testSum() throws Exception {
        //expected: 6, sum of 1 and 5
        //assertEquals(6d, mCalculator.sum(1d, 5d), 0);
        byte a = (byte)0x12;
        byte b = (byte)0x34;
        byte c = (byte)0xf8;
        byte d = (byte)0x3a;

        byte[] bRefArr = {0x12, 0x34};
        int iOutcome = 0;
        byte bLoop;
        System.out.println(bRefArr.length);

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
            System.out.println("i:"+iOutcome);
        }


    }

    @Test
    public void testSubstract() throws Exception {
        assertEquals(1d, mCalculator.substract(5d, 4d), 0);
    }

    @Test
    public void testDivide() throws Exception {
        assertEquals(4d, mCalculator.divide(20d, 5d), 0);
    }

    @Test
    public void testMultiply() throws Exception {
        assertEquals(10d, mCalculator.multiply(2d, 5d), 0);
    }
}