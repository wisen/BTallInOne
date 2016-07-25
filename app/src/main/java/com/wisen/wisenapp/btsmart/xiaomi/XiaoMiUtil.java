package com.wisen.wisenapp.btsmart.xiaomi;

import android.content.res.AssetManager;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static android.system.OsConstants.O_APPEND;

/**
 * Created by wisen on 2016-07-25.
 */
public class XiaoMiUtil {

    private static final String TAG = "XiaoMiUtil";

    public static String BONDED_FLAG = "XIAOMI_BONDED_FLAG";
    public static int XIAOMI_NOT_BONDED = 1;
    public static int XIAOMI_BONDED = 0;

    private static String BondedDeviceFileName = "/sdcard/bonded_devices.xml";

    public static boolean isBondedFileExist(){
        File file = new File(BondedDeviceFileName);
        return file.exists();
    }

    public static void do_parser(ArrayList<BondedDevice> mList){
        try {
            File file = new File(BondedDeviceFileName);
            if(file.exists()){
                FileInputStream fis = new FileInputStream(file);
                InputSource is2 = new InputSource(fis);
                //使用工厂方法初始化SAXParserFactory变量spf
                SAXParserFactory spf = SAXParserFactory.newInstance();
                //通过SAXParserFactory得到SAXParser的实例
                SAXParser sp = spf.newSAXParser();
                //通过SAXParser得到XMLReader的实例
                XMLReader xr = sp.getXMLReader();
                //初始化自定义的类MySaxHandler的变量msh，将beautyList传递给它，以便装载数据
                BondedDeviceSaxHandler msh = new BondedDeviceSaxHandler(mList);
                //将对象msh传递给xr
                xr.setContentHandler(msh);
                //调用xr的parse方法解析输入流
                xr.parse(is2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveinfo(ArrayList<BondedDevice> mList){
        try {
            File file = new File(BondedDeviceFileName);
            if(!file.exists()){
                file.createNewFile();
            }

            Log.d(TAG, "saveinfo");
            FileOutputStream os = new FileOutputStream(file, false);
            //获取XmlSerializer对象
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            org.xmlpull.v1.XmlSerializer xmlSerializer = factory.newSerializer();
            //设置输出流对象
            xmlSerializer.setOutput(os, "utf-8");

            xmlSerializer.startDocument("utf-8", true);
            xmlSerializer.startTag(null, "Bdevices");

            for (BondedDevice device : mList) {
                xmlSerializer.startTag(null, "device");

                xmlSerializer.startTag(null, "name");
                xmlSerializer.text(device.getName());
                xmlSerializer.endTag(null, "name");

                xmlSerializer.startTag(null, "address");
                xmlSerializer.text(device.getAddress());
                xmlSerializer.endTag(null, "address");

                xmlSerializer.endTag(null, "device");
            }

            xmlSerializer.endTag(null, "Bdevices");
            xmlSerializer.endDocument();
            os.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int BytetoInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }

    public static int BytetoInt(byte[] bRefArr, int length) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }
}
