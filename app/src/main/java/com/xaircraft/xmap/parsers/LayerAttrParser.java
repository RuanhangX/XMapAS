package com.xaircraft.xmap.parsers;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import EMap.IO_GisDB.IEGisDB_Base.ATT_FIELD_TYPE;
import android.util.Xml;

import com.xaircraft.xmap.entity.LayerAttrEntity;

public class LayerAttrParser {
	public List<LayerAttrEntity> parse(InputStream is) throws Exception {  
        List<LayerAttrEntity> LayerAttrEntitys = null;  
        LayerAttrEntity layerAttrEntity = null;  
          
//      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  
//      XmlPullParser parser = factory.newPullParser();  
          
        XmlPullParser parser = Xml.newPullParser(); //由android.util.Xml创建一个XmlPullParser实例  
        parser.setInput(is, "UTF-8");               //设置输入流 并指明编码方式  
  
        int eventType = parser.getEventType();  
        while (eventType != XmlPullParser.END_DOCUMENT) {  
            switch (eventType) {  
            case XmlPullParser.START_DOCUMENT:  
                LayerAttrEntitys = new ArrayList<LayerAttrEntity>();  
                break;  
            case XmlPullParser.START_TAG:  
                if (parser.getName().equals("LayerAttrEntity")) {  
                    layerAttrEntity = new LayerAttrEntity();  
                } else if (parser.getName().equals("name")) {  
                    eventType = parser.next();  
                    layerAttrEntity.setName(parser.getText());  
                } else if (parser.getName().equals("type")) {  
                    eventType = parser.next();  
                    layerAttrEntity.setType(ATT_FIELD_TYPE.valueOf(parser.getText()));  
                } else if (parser.getName().equals("defaultValue")) {  
                    eventType = parser.next();
                    layerAttrEntity.setDefaultValue(parser.getText());  
                }  
                break;  
            case XmlPullParser.END_TAG:  
                if (parser.getName().equals("LayerAttrEntity")) {  
                    LayerAttrEntitys.add(layerAttrEntity);  
                    layerAttrEntity = null;      
                }  
                break;  
            }  
            eventType = parser.next();  
        }  
        return LayerAttrEntitys;  
    }  
      
    public String serialize(List<LayerAttrEntity> LayerAttrEntitys) throws Exception {  
//      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  
//      XmlSerializer serializer = factory.newSerializer();  
          
        XmlSerializer serializer = Xml.newSerializer(); //由android.util.Xml创建一个XmlSerializer实例  
        StringWriter writer = new StringWriter();  
        serializer.setOutput(writer);   //设置输出方向为writer  
        serializer.startDocument("UTF-8", true);  
        serializer.startTag("", "LayerAttrEntitys");  
        for (LayerAttrEntity LayerAttrEntity : LayerAttrEntitys) {  
            serializer.startTag("", "LayerAttrEntity"); 
            
            serializer.startTag("", "name");  
            serializer.text(LayerAttrEntity.getName() + "");  
            serializer.endTag("", "name");  
            
            serializer.startTag("", "type");  
            serializer.text(LayerAttrEntity.getType().toString());  
            serializer.endTag("", "type");  
              
            serializer.startTag("", "defaultValue");  
            serializer.text(LayerAttrEntity.getDefaultValue() + "");  
            serializer.endTag("", "defaultValue");  
              
            serializer.endTag("", "LayerAttrEntity");  
        }  
        serializer.endTag("", "LayerAttrEntitys");  
        serializer.endDocument();  
          
        return writer.toString();  
    }  

}
