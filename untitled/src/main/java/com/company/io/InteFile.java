package com.company.io;

import java.io.File;

public class InteFile {
    public static void main(String[] args) {
        File file=new File("E:");
        File[] files=file.listFiles();
        readFiles(files);
    }
    private static  void readFiles(File[] files){
        if(files==null){
            return;
        }
        for(File f:files){
            if(f.isFile()){
                System.out.println(f.getName());
            }else if(f.isDirectory()){
                readFiles(f.listFiles());
            }
        }
    }
}
