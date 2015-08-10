package com.warriormedia.app.util;

import com.warriormedia.library.authentication.BaseActionResult;

/**
 * 动作执行函数
 *
 * @author zou.sq
 * @version 1.1.0<br>
 *          2013-03-21，tan.xx，修改继承BaseActionResult
 */
public class ActionResult extends BaseActionResult {

    /**
     * 网络异常
     */
    public static final String RESULT_CODE_SUCCESS = "0";
    /**
     * 网络异常
     */
    public static final String RESULT_CODE_NET_ERROR = "111";
}
