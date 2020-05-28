package com.bluesgao.demo.protocol;

import lombok.Data;

import java.io.Serializable;
/**
 * ----------------------------------------------------------
 * |magicNum|version|flag|serialize|data_length|data_body   |
 * ----------------------------------------------------------
 * 4字节        1字节 1字节  1字节       4字节           N字节
 */
@Data
public class Header implements Serializable {
    private Integer magic;//魔数
    private Byte version = 1;//版本
    //private Byte flag;//0-请求，1-响应
    private Byte serialize;//序列化方式
    private Integer length;//body数据长度
}
