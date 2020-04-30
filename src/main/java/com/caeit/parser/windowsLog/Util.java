package com.caeit.parser.windowsLog;

import net.sf.json.JSONObject;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * by hhj @2018-10-31
 * utils for this parser
 */
public class Util {

    protected static String xml2JSONTop(File file) {
        JSONObject obj = new JSONObject();
        try {
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build(file);
            Element root = doc.getRootElement();
            obj.put(root.getName(), iterateElement(root));
            String s = obj.toString();
            return s;
        } catch (Exception e) {
            return "";
        }
    }

    protected static String xml2JSONTopByStr(String xmlStr) {
        JSONObject obj = new JSONObject();
        try {
            SAXBuilder sb = new SAXBuilder();
            InputStream is = new ByteArrayInputStream(xmlStr.getBytes());
            Document doc = sb.build(is);
            Element root = doc.getRootElement();
            obj.put(root.getName(), iterateElement(root));
            return obj.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static Map  iterateElement(Element element) {
        List jiedian = element.getChildren();
        Element et;
        Map map = new LinkedHashMap();
        List list;
        //增加了对节点的处理
        List<Attribute> attributes=element.getAttributes();
        for(int i=0;i<attributes.size();i++){
            map.put(attributes.get(i).getName(), attributes.get(i).getValue());
        }
        //遍历所有子节点
        for (int i = 0; i < jiedian.size(); i++) {
            list = new LinkedList();
            et = (Element) jiedian.get(i);
            //若  子节点内文本为空
            if (et.getTextTrim().equals("")) {
                if (et.getChildren().size() == 0)
                    //若  没有子节点则继续
                    continue;
                //若有  子节点  且  当前map有 本节点名称
                if (map.containsKey(et.getName())) {
                    //list.add(map.get(et.getName()));
                    //list = (List) map.get(et.getName());
                    if (map.get(et.getName()) instanceof LinkedList){
                        List taskList = (LinkedList)map.get(et.getName());
                        for(int tag=0;tag<taskList.size();tag++){
                            list.add(taskList.get(tag));
                        }
                    }else if(map.get(et.getName()) instanceof LinkedHashMap){
                        LinkedHashMap linkedHashMap = (LinkedHashMap) map.get(et.getName());
                        list.add(linkedHashMap);
                    }
                }
                list.add(iterateElement(et));
                if(list.size()>1){
                    map.put(et.getName(), list);
                }else{
                    map.put(et.getName(), list.get(0));
                }

            } else { //子节点内 文本不为空
                List<Attribute> temp_att=et.getAttributes();
                //若  子节点内有属性
                if(temp_att.size()!=0){
                    Map childMap=new HashMap();
                    for(int j=0;j<temp_att.size();j++){
                        childMap.put(temp_att.get(j).getName(), temp_att.get(j).getValue());
                    }
                    childMap.put("content",et.getTextTrim());
                    map.put(et.getName(), childMap);
                }else{
                    if (map.containsKey(et.getName())) {

                        try{
                            list = (List) map.get(et.getName());
                        }catch(Exception e){
                            list.add(map.get(et.getName()));
                        }
                    }
                    list.add(et.getTextTrim());
                    if(list.size()>1){
                        map.put(et.getName(), list);
                    }else{
                        map.put(et.getName(), list.get(0));
                    }
                }


            }
        }
        return map;
    }

    /**
     * 格式化JSON字符串
     * @param jsonStr
     * @return
     */
    protected static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr))
            return "";
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '"':
                    if (last != '\\'){
                        isInQuotationMarks = !isInQuotationMarks;
                    }
                    sb.append(current);
                    break;
                case '{':
                case '[':
                    sb.append(current);
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent++;
                        addIndentBlank(sb, indent);
                    }
                    break;
                case '}':
                case ']':
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent--;
                        addIndentBlank(sb, indent);
                    }
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\' && !isInQuotationMarks) {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }
        return sb.toString();
    }

    /**
     * json缩进
     * @param sb
     * @param indent
     */
    protected static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }

    /**
     * get current time
     * @return
     */
    protected static String getCurrentTime(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date())+" : ";
    }
}
