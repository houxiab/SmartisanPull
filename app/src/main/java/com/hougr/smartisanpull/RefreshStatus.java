package com.hougr.smartisanpull;

/**
 * Created by hougr on 16/8/29.
 */
public class RefreshStatus{      //四种状态。
    public static final int STATUS_ORIGIN = 0;
    public static final int STATUS_DISTANCE_UNFINISHED = 1;
    public static final int STATUS_DISTANCE_UNFINISHED_BACK = 2;
    public static final int STATUS_DISTANCE_FINISHED = 3;
    public static final int STATUS_REFRESH_PREPARE = 4;
    public static final int STATUS_REFRESHING =5;
    public static final int STATUS_REFRESH_FINISHED_CIRCLE = 6;
//        public static final int STATUS_REFRESH_FINISHED_HIDE = 7;
}