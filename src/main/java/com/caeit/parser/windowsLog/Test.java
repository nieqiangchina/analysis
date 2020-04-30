package com.caeit.parser.windowsLog;

import java.io.File;
import java.util.Map;

public class Test {
    public static void main(String[]args){
        File file = new File("D:\\doucment\\201903\\evtx\\windowsLog-evtx\\windowsLog_application.evtx");
    //    File file = new File("E:\\14-Windows日志文件解析器源码\\14-Windows日志文件解析器\\04-测试文件\\xml\\UTF-8\\WindowsLogParser_testFile.xml");
    //    File file = new File("E:\\14-Windows日志文件解析器源码\\14-Windows日志文件解析器\\04-测试文件\\txt\\GB-2312\\WindowsLogParser_testFile.txt");
        WindowsLogParser csvParser = new WindowsLogParser();
        Map map = csvParser.parseWindowsLog(file);
        System.out.println(csvParser.getParmsInfo());
        System.out.println(map.get("validateFileType"));
        System.out.println(map.get("jsonTopLevel"));
        System.out.println(map.get("jsonBottomLevel"));

        System.out.println("Correct Jar File!");
    }
}
