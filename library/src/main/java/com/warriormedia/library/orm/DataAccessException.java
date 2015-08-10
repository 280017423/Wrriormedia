/**
 *
 */
package com.warriormedia.library.orm;

/**
 * Sqlite访问异常类，主要用于ORM框架
 *
 * @author 王先佑
 */
public class DataAccessException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -2846484017141855467L;

    /**
     *
     */
    public DataAccessException() {
    }

    /**
     * @param detailMessage 数据库访问异常的详细信息
     */
    public DataAccessException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * @param throwable 异常
     */
    public DataAccessException(Throwable throwable) {
        super(throwable);
    }

    /**
     * @param detailMessage 异常描述信息
     * @param throwable     异常
     */
    public DataAccessException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
