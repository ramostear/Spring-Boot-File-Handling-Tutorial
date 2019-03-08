package com.ramostear.application.model;

import lombok.Data;

import java.util.Date;

/**
 * @author : ramostear
 * @date  : 2019/3/8 0008-15:25
 */
@Data
public class FileInfo {

    private String name;
    private Date uploadTime = new Date();

    public FileInfo setFileName(String name){
        this.setName ( name );
        return this;
    }

}
