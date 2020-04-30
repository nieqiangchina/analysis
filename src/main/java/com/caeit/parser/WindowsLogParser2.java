package com.caeit.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class WindowsLogParser2 {

    public static void main(String[] args) {
        File f = new File("C://Users//25183//Desktop//demo.evtx");
    //    File f = new File("C://Users//25183//Desktop//test3.mdf");

        String filename = f.getName();
        WindowsLogParser2 wlp = new WindowsLogParser2();
        wlp.getFileType(filename);


        try {
            //通过传入File实例化Scanner类
            Scanner sc = new Scanner(new File("C://Users//25183//Desktop//demo.evtx"));
            //按行读取test.txt文件内容
            while (sc.hasNextLine()) {
                System.out.println(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
        }
    }

    //定义parseevtx()
    public Map<String,String> parseEvtx(File f){
        Map<String,String> map = new HashMap<String, String>();

//        if(validateFileType(File f) ==  false){
//            return map;
//        }
        //文件类型有效，继续解析
        map.put("jsonTopLevel",paresEvtxTop(f));
        map.put("jsonBottomLevel",paresEvtxBottom(f));
        map.put("validateFileType","true");
        return map;
    }

    //验证文件是否正确
    private String getFileType(String fileName) {
        String[] strArray = fileName.split("\\.");
        int suffixIndex = strArray.length -1;
        System.out.println("文件后缀是："+strArray[suffixIndex]);
        return "strArray[suffixIndex]";
    }

    //验证文件是否正确
    private boolean validateFileType(File f) throws Exception{
    return true;
    }


    //解析 paresEvtxTop()
    private String paresEvtxTop(File f){
        String jsonTopLevel = "";
        return jsonTopLevel;
    }
    //解析 paresEvtxBottom()
    private String paresEvtxBottom(File f){
        String jsonEvtxBottom = "";
        return jsonEvtxBottom;
    }
}


