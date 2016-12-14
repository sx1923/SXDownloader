package com.jasminx.test;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void Test() {
        String ss = Thread.currentThread().getName();
        String url = "http://www.cnblogs.com/lisperl/archive/2012/05/21/2511224.html";
        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        int hashCode = url.hashCode();
    }
}