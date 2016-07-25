package com.wisen.wisenapp.btsmart.xiaomi;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by wisen on 2016-07-25.
 */
public class BondedDeviceSaxHandler extends DefaultHandler {

    private ArrayList<BondedDevice> mList;
    private BondedDevice device;
    private String content;

    public BondedDeviceSaxHandler(ArrayList<BondedDevice> mList) {
        this.mList = mList;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        content = new String(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if("device".equals(localName)){
            device = new BondedDevice(); //新建BondedDevice对象
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if("name".equals(localName)){
            device.setName(content);
        }else if("address".equals(localName)){
            device.setAddress(content);
        }else if("device".equals(localName)){
            mList.add(device); //将device对象加入到List中
        }
    }
}
