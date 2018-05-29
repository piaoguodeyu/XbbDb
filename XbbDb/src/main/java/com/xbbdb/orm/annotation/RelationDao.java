package com.xbbdb.orm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface Relations.
 * 表示关联表
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { java.lang.annotation.ElementType.FIELD })
public @interface RelationDao {
	
	/**
	 * 关联名,对象内唯一即可.
	 *
	 * @return the string
	 */
	String name();
	
	/**
	 * 外键.
	 *
	 * @return the string
	 */
	String foreignKey();
	
	/**
	 * 关联类型.
	 *
	 * @return the string  one2one  one2many many2many
	 */
	String type();
	
}
