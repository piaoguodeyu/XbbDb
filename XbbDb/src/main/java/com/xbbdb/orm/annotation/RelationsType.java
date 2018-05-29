package com.xbbdb.orm.annotation;

/**
 * Copyright (c) 2012 All rights reserved
 * 名称：RelationsType.java
 * 描述：关联关系类型
 *
 * @author zhaoqp
 * @version v1.0
 * @date：2013-10-15 上午9:55:13
 */
public interface RelationsType {
    String one2one = "one2one";
    String one2many = "one2many";
    String many2many = "many2many";
}
