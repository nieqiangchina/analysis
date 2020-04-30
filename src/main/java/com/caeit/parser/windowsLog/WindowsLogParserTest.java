package com.caeit.parser.windowsLog;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class WindowsLogParserTest {

    /**
     * 命令行运行
     * 2019-02-23 by hjhu
     * @param args
     * @throws IOException
     */
    public static void main(String[] args){
        while (true){
            System.out.println("请输入文件的绝对路径（按回车键结束输入或退出）：");
            Scanner in =new Scanner(System.in);
            String filePath = in.nextLine();
            if(null == filePath || "".equals(filePath)){
                System.out.println("退出解析器");
                System.exit(0);
            }
            //处理路径中的斜杆
            File file = new File(filePath.replace("\\","\\\\"));
            if(file.isDirectory()){
                System.out.println("该路径指向一个目录，请重新输入");
                continue;
            }
            if(!file.exists()){
                System.out.println("找不到指定文件或路径错误，请重新输入");
                continue;
            }
            if (!filePath.toLowerCase().endsWith("csv")&&!filePath.toLowerCase().endsWith("xml")&&!filePath.toLowerCase().endsWith("txt")){
                System.out.println("文件类型有误，请重新输入");
                continue;
            }
            WindowsLogParser windowsLogParser = new WindowsLogParser();
            Map<String, String> map = windowsLogParser.parseWindowsLog(file);
            System.out.println("解析结果：");
            System.out.println("validateFileType================================================");
            System.out.println(map.get("validateFileType"));
            System.out.println("parmsInfo=======================================================");
            System.out.println(windowsLogParser.getParmsInfo());
            System.out.println("jsonTopLevel====================================================");
            System.out.println(Util.formatJson(map.get("jsonTopLevel")));
            System.out.println("jsonBottomLevel=================================================");
            System.out.println(Util.formatJson(map.get("jsonBottomLevel")));
        }
    }
}
