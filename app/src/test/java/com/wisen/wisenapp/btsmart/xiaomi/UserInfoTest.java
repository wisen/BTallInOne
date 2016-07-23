package com.wisen.wisenapp.btsmart.xiaomi;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by wisen on 2016-07-23.
 */
public class UserInfoTest {
    private UserInfo userinfo;

    @Before
    public void setUp() throws Exception {
        userinfo = new UserInfo(989358576, 1, 32, 172, 80, "wisen", 1);

    }

    @Test
    public void testGetCRC8() throws Exception {
        byte[] seq = {0x77, 0x69, 0x73, 0x65, 0x6e, 0x5f, 0x77, 0x61};
        //c8:0f:10:00:86:58
        byte a = (byte)((userinfo.getCRC8(seq)^Integer.parseInt("c80f",16)) & 0xff);

        System.out.println(a);
    }
}