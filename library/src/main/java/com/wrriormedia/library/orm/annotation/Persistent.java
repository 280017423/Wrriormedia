package com.wrriormedia.library.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Transient 标识该字段能被持久化（保存数据库）
 * 
 * @author huang.b 2013-9-23 下午2:36:17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD
})
public @interface Persistent {

}
