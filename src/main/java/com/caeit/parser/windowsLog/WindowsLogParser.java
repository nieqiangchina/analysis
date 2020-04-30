package com.caeit.parser.windowsLog;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.nifi.processors.evtx.parser.BinaryReader;

import java.io.*;
import java.util.*;

public class WindowsLogParser {

    private static String JSON_TOP_LEVEL = "";
    private static String JSON_BOTTOM_LEVEL = "";

    public static String getParmsInfo() {
        String parmsInfo = "[{\"File\":\"待解析文件\"}]";
        return parmsInfo;
    }

    public Map<String,String> parseWindowsLog(File file){
        Map<String,String> map = new LinkedHashMap<>();
        if(validateFileType(file)){
            map.put("jsonTopLevel",JSON_TOP_LEVEL);
            map.put("jsonBottomLevel",JSON_BOTTOM_LEVEL);
            map.put("validateFileType","true");
            JSON_TOP_LEVEL = "";
            JSON_BOTTOM_LEVEL = "";
        }else{
            map.put("validateFileType","false");
        }
        return map;
    }

    /**
     * parse a csv-typ
     * e windows log file
     * @param file
     * @return
     * @throws IOException
     */
    private String parseCsv(File file)throws IOException{
        JSONArray array = new JSONArray();
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        BufferedReader br= new BufferedReader(new InputStreamReader(in,this.getCharset(file)));
        CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
        CSVReader cr = new CSVReaderBuilder(br)
                .withCSVParser(parser)
                .build();
        //存储键名
        LinkedHashMap<Integer,String> keyMap = new LinkedHashMap<>();
        List<String[]> list = cr.readAll();
        String []headers = list.get(0);
        for(int i = 0;i<headers.length;i++){
            String k = headers[i].replace("\"\"", "");
            if(i==0){
                //处理日志文件首位的特殊字符
                char [] charArray = k.toCharArray();
                //汉字字符的unicode十进制区间[19968,40869]
                if(charArray[0]<19968||charArray[0]>40869) k = k.substring(1);
            }
            //存储键名
            keyMap.put(i,k);
        }
        for (int row = 1;row < list.size(); row++) {
            //组合为JSON格式
            int Length=list.get(row).length;
            JSONObject obj = new JSONObject();
            if(Length > 0){
                for(int k=0;k<Length;k++){
                    String val = list.get(row)[k];
                    if(null == keyMap.get(k)){
                        obj.put("事件内容",val);
                    }else{
                        obj.put(keyMap.get(k),val);
                    }
                }
            }
            if(obj.size() > 0){
                array.add(obj);
            }
        }

        StringBuffer s = new StringBuffer("{\"csv\":"+array.toString()+"}");
        return s.toString();
    }

    /**
     * parse a txt-type windows log file
     * @param file
     * @return
     * @throws IOException
     */
    private String parseTxt(File file)throws IOException{
        JSONArray array = new JSONArray();
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        BufferedReader br= new BufferedReader(new InputStreamReader(in,this.getCharset(file)));
        CSVParser parser = new CSVParserBuilder().withSeparator('\t').build();
        CSVReader cr = new CSVReaderBuilder(br)
                .withCSVParser(parser)
                .build();
        //存储键名
        LinkedHashMap<Integer,String> keyMap = new LinkedHashMap<>();
        List<String[]> list = cr.readAll();
        String []headers = list.get(0);
        for(int i = 0;i<headers.length;i++){
            String k = headers[i].replace("\"\"", "");
            if(i==0){
                //处理日志文件首位的特殊字符
                char [] charArray = k.toCharArray();
                //汉字字符的unicode十进制区间[19968,40869]
                if(charArray[0]<19968||charArray[0]>40869) k = k.substring(1);
            }
            //存储键名
            keyMap.put(i,k);
        }
        for (int row = 1;row < list.size(); row++) {
            //组合为JSON格式
            int Length=list.get(row).length;
            JSONObject obj = new JSONObject();
            if(Length > 0){
                for(int k=0;k<Length;k++){
                    String val = list.get(row)[k];
                    if(null == keyMap.get(k)){
                        obj.put("事件内容",val);
                    }else{
                        obj.put(keyMap.get(k),val);
                    }
                }
            }
            if(obj.size() > 0){
                array.add(obj);
            }
        }

        StringBuffer s = new StringBuffer("{\"txt\":"+array.toString()+"}");
        return s.toString();
    }

    /**
     * parse an xml-type windows log file in top format
     * @param f
     * @return
     * @throws IOException
     */
    protected String parseXmlTop(File f) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(f));
        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(in,this.getCharset(f)));
        String data;
        StringBuffer dataStr = new StringBuffer();
        while((data = bufferedReader.readLine())!=null){
            data = data.trim();
            //read the first line of the file
            if(data.toLowerCase().startsWith("<?")&&data.toLowerCase().endsWith("?>")){
                //首行格式须为：<?xml version="1.0" encoding="UTF8"?>
                dataStr.append("<?xml version=\"1.0\" encoding=\"UTF8\"?>");
                continue;
            }else if("".equals(data)){
                continue;
            }else{
                dataStr.append(data);
            }
        }
        String s = Util.xml2JSONTopByStr(dataStr.toString());
        if("".equals(s)){
            //若字符串为空，代表源文件解析有误
            return "";
        }
        JSONObject jsonObject = JSONObject.fromObject(s);
        Iterator iterator = jsonObject.keys();
        String rootKey = "";
        String var1 = "";
        while (iterator.hasNext()){
            rootKey = (String) iterator.next();
            var1 = jsonObject.getString(rootKey);
        }

        //获取第一层JSON
        jsonObject = JSONObject.fromObject(var1);
        iterator = jsonObject.keys();
        while (iterator.hasNext()){
            String key = iterator.next()+"";
            rootKey += " " + key;
            var1 = jsonObject.getString(key);
        }

        return "{\""+rootKey+"\":"+var1+"}";
    }


    /**
     * parse an evtx-type windows log file in top format
     * @param f
     * @return
     * @time 2019/4/2 nieqiang
     * @throws IOException
     */
    protected String parseEvtxTop(File f) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(f));
        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(in,this.getCharset(f)));
        String data;
        StringBuffer dataStr = new StringBuffer();
        while((data = bufferedReader.readLine())!=null){
            data = data.trim();
            //read the first line of the file
            if(data.toLowerCase().startsWith("<?")&&data.toLowerCase().endsWith("?>")){
                //首行格式须为：<?xml version="1.0" encoding="UTF8"?>
                dataStr.append("<?xml version=\"1.0\" encoding=\"UTF8\"?>");
                continue;
            }else if("".equals(data)){
                continue;
            }else{
                dataStr.append(data);
            }
        }
        String s = Util.xml2JSONTopByStr(dataStr.toString());
        if("".equals(s)){
            //若字符串为空，代表源文件解析有误
            return "";
        }
        JSONObject jsonObject = JSONObject.fromObject(s);
        Iterator iterator = jsonObject.keys();
        String rootKey = "";
        String var1 = "";
        while (iterator.hasNext()){
            rootKey = (String) iterator.next();
            var1 = jsonObject.getString(rootKey);
        }

        //获取第一层JSON
        jsonObject = JSONObject.fromObject(var1);
        iterator = jsonObject.keys();
        while (iterator.hasNext()){
            String key = iterator.next()+"";
            rootKey += " " + key;
            var1 = jsonObject.getString(key);
        }

        return "{\""+rootKey+"\":"+var1+"}";
    }


    /**
     * parse an evtx-type windows log file in bottom format
     * @param f
     * @return
     * @time 2019/4/2 nieqiang
     * @throws IOException
     */
    protected String parseEvtxBottom(File f) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(f));
        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(in,this.getCharset(f)));
        String data;
        StringBuffer dataStr = new StringBuffer();
        while((data = bufferedReader.readLine())!=null){
            data = data.trim();
            //read the first line of the file
            if(data.toLowerCase().startsWith("<?")&&data.toLowerCase().endsWith("?>")){
                //首行格式须为：<?xml version="1.0" encoding="UTF8"?>
                dataStr.append("<?xml version=\"1.0\" encoding=\"UTF8\"?>");
                continue;
            }else if("".equals(data)){
                continue;
            }else{
                dataStr.append(data);
            }
        }
        String s = Util.xml2JSONTopByStr(dataStr.toString());
        if("".equals(s)){
            //empty string means an error may occur
            return "";
        }
        JSONObject jsonObject = JSONObject.fromObject(s);
        Iterator iterator = jsonObject.keys();
        String rootKey = "";
        String var1 = "";
        while (iterator.hasNext()){
            rootKey = (String) iterator.next();
            var1 = jsonObject.getString(rootKey);
        }

        //获取第一层JSON
        jsonObject = JSONObject.fromObject(var1);
        iterator = jsonObject.keys();
        while (iterator.hasNext()){
            String key = iterator.next()+"";
            rootKey += " " + key;
            var1 = jsonObject.getString(key);
        }

        JSONArray jsonArray = JSONArray.fromObject(var1);
        String res = toJsonBottom(jsonArray.toString());

        return "{\""+rootKey+"\":"+res+"}";
    }

    /**
     * parse an xml-type windows log file in bottom format
     * @param f
     * @return
     * @throws IOException
     */
    protected String parseXmlBottom(File f) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(f));
        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(in,this.getCharset(f)));
        String data;
        StringBuffer dataStr = new StringBuffer();
        while((data = bufferedReader.readLine())!=null){
            data = data.trim();
            //read the first line of the file
            if(data.toLowerCase().startsWith("<?")&&data.toLowerCase().endsWith("?>")){
                //首行格式须为：<?xml version="1.0" encoding="UTF8"?>
                dataStr.append("<?xml version=\"1.0\" encoding=\"UTF8\"?>");
                continue;
            }else if("".equals(data)){
                continue;
            }else{
                dataStr.append(data);
            }
        }
        String s = Util.xml2JSONTopByStr(dataStr.toString());
        if("".equals(s)){
            //empty string means an error may occur
            return "";
        }
        JSONObject jsonObject = JSONObject.fromObject(s);
        Iterator iterator = jsonObject.keys();
        String rootKey = "";
        String var1 = "";
        while (iterator.hasNext()){
            rootKey = (String) iterator.next();
            var1 = jsonObject.getString(rootKey);
        }

        //获取第一层JSON
        jsonObject = JSONObject.fromObject(var1);
        iterator = jsonObject.keys();
        while (iterator.hasNext()){
            String key = iterator.next()+"";
            rootKey += " " + key;
            var1 = jsonObject.getString(key);
        }

        JSONArray jsonArray = JSONArray.fromObject(var1);
        String res = toJsonBottom(jsonArray.toString());

        return "{\""+rootKey+"\":"+res+"}";
    }

    /**
     * split the method into 2 parts to simplify the code
     * @param resultString
     * @return
     */
    private String toJsonBottom(String resultString){

        JSONArray jsonArray = JSONArray.fromObject(resultString);
        JSONArray resArray = new JSONArray();
        for(int i = 0;i<jsonArray.size();i++){
            JSONObject jsonObject = JSONObject.fromObject(jsonArray.get(i));
            JSONObject json = generateBottomJson(jsonObject);
            resArray.add(json);
        }
        return resArray.toString();
    }

    /**
     * output a JSONObject in bottom format
     * @param jsonObject
     * @return
     */
    private JSONObject generateBottomJson(JSONObject jsonObject){
        Iterator iterator = jsonObject.keys();
        JSONObject res = new JSONObject();
        while (iterator.hasNext()){
            String key = (String) iterator.next();
            String value = jsonObject.getString(key);
            if(value.startsWith("{")){
                JSONObject jo1 = JSONObject.fromObject(value);
                Iterator it1 = jo1.keys();
                while(it1.hasNext()){
                    String k1 = it1.next()+"";
                    String v1 = jo1.getString(k1);
                    if(v1.startsWith("{")){
                        JSONObject jo2 = JSONObject.fromObject(v1);
                        Iterator it2 = jo2.keys();
                        while (it2.hasNext()){
                            String k2 = it2.next()+"";
                            String v2 = jo2.getString(k2);
                            if(v2.startsWith("[")&&v2.endsWith("]")){
                                String v = v2.replace("[\"","");
                                v = v.replace("\"]","");
                                v = v.replace("\",\"",";");
                                res.put(key+" "+k1+" "+k2,v);
                            }else{
                                res.put(key+" "+k1+" "+k2,v2);
                            }
                        }
                    }else if(v1.startsWith("[")&&v1.endsWith("]")){
                        //handle array
                        String v = v1.replace("[\"","");
                        v = v.replace("\"]","");
                        v = v.replace("\",\"",";");
                        res.put(key +" "+k1,v);
                    }else{
                        res.put(key +" "+k1,v1);
                    }
                }
            }else{
                res.put(key,value);
            }
        }
        return res;
    }

    /**
     * check the validation of the file
     * @param file
     * @return
     */
    private Map<String,String> parse4Validation(File file){
        Map<String,String> map = new LinkedHashMap<>();
        map.put("jsonTopLevel","");
        map.put("jsonBottomLevel","");
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String data = "";
            //取第一行非空数据
            while((data = bufferedReader.readLine())!=null){
                data = data.trim();
                //read the first line of the file
                if("".equals(data)){
                    continue;
                }else{
                    break;
                }
            }
            //判断文件类型
            if(data.contains("xml")){
                //XML文件
                String top = this.parseXmlTop(file);
                String bottom = this.parseXmlBottom(file);
                map.put("jsonTopLevel",top);
                map.put("jsonBottomLevel",bottom);
            }else if(data.contains("\t")&&!data.contains(",")){
                //TXT以制表符"\t"分隔
                String json = parseTxt(file);
                map.put("jsonTopLevel",json);
                map.put("jsonBottomLevel",json);
            }else if(data.contains(",")){
                //CSV以制表符","分隔
                String json = parseCsv(file);
                map.put("jsonTopLevel",json);
                map.put("jsonBottomLevel",json);
            }else{
                //EVTX文件头中包含ElfFile这个字段
                byte [] bytes;
                FileInputStream fs = new FileInputStream(file);
                bytes = new byte[fs.available()];
                fs.read(bytes);
                int len = bytes.length;
                //get the binary header of the file
                   BinaryReader binaryReader = new BinaryReader(bytes);
                String s = binaryReader.readString(len);
                if(s.toLowerCase().equals("elffile")){
                    try {
                        //实例化Scanner类
                        Scanner sc = new Scanner(new File(file.getPath()));
                        while (sc.hasNextLine()) {
                            System.out.println(sc.nextLine());
                        }
                    } catch (FileNotFoundException e) {
                    }
                    //EVTX文件
                    String top = this.parseEvtxTop(file);
                    String bottom = this.parseEvtxBottom(file);
                    map.put("jsonTopLevel",top);
                    map.put("jsonBottomLevel",bottom);
                    return map;
                }else{
                    return map;
                }
            }
        }catch (Exception e){
            return map;
        }
        return map;
    }


    /**
     * validate the file type
     * @param file
     * @return
     */
    private boolean validateFileType(File file) {
        if(file.length() == 0){
            //文件无内容
            return false;
        }
        Map<String,String> map = this.parse4Validation(file);
        //get values
        String jtl = map.get("jsonTopLevel");
        String jbl = map.get("jsonBottomLevel");
        if(jtl.equals("")||jbl.equals("")){
            return false;
        }
        // delete the json-check method because windows log files are nicely-formatted
        JsonRight jr = new JsonRight();
        if(jr.getInstance().validate(jtl) &&jr.getInstance().validate(jbl)){
            JSON_TOP_LEVEL = jtl;
            JSON_BOTTOM_LEVEL = jbl;
        }
        return true;
    }

    /**
     * 获取文件编码方式
     * 2019-2-22 by hjhu
     * @param file
     * @return
     * @throws IOException
     */
    private String getCharset(File file) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        boolean checked = false;
        bis.mark(0);
        int read = bis.read(first3Bytes, 0, 3);
        if (read == -1) {
            return charset; //文件编码为 ANSI
        } else if (first3Bytes[0] == (byte) 0xFF
                && first3Bytes[1] == (byte) 0xFE) {
            charset = "UTF-16LE"; //文件编码为 Unicode
            checked = true;
        } else if (first3Bytes[0] == (byte) 0xFE
                && first3Bytes[1] == (byte) 0xFF) {
            charset = "UTF-16BE"; //文件编码为 Unicode big endian
            checked = true;
        } else if (first3Bytes[0] == (byte) 0xEF
                && first3Bytes[1] == (byte) 0xBB
                && first3Bytes[2] == (byte) 0xBF) {
            charset = "UTF-8"; //文件编码为 UTF-8
            checked = true;
        }
        bis.reset();
        if (!checked) {
            int loc = 0;
            while ((read = bis.read()) != -1) {
                loc++;
                if (read >= 0xF0)
                    break;
                if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                    break;
                if (0xC0 <= read && read <= 0xDF) {
                    read = bis.read();
                    if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                        // (0x80
                        // - 0xBF),也可能在GB编码内
                        continue;
                    else
                        break;
                } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                    read = bis.read();
                    if (0x80 <= read && read <= 0xBF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            charset = "UTF-8";
                            break;
                        } else
                            break;
                    } else
                        break;
                }
            }
        }
        bis.close();
        return charset;
    }
}
