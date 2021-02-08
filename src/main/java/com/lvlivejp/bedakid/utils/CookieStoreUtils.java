package com.lvlivejp.bedakid.utils;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

public class CookieStoreUtils {

    private static CookieStore cookieStore = new BasicCookieStore();
    public static CookieStore get(){
        return cookieStore;
    }
}
