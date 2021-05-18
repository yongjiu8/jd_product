package com.jd.product.utils;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author chenYongJin
 * @Date 2021-05-14
 */
public class StringUtil {

    /**
     * 取两个文本之间的文本值
     * @param text 源文本 比如：欲取全文本为 12345
     * @param left 文本前面
     * @param right 后面文本
     * @return 返回 String
     */
    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }

    public static String getSkuid(String url) {
        if (StrUtil.isNotEmpty(url)) {
            int start = url.indexOf(".html");
            String skuid = "";
            while (start >= 0) {
                String tmp = url.substring(start-1,start);
                if (tmp.equals("/")) {
                    break;
                }
                skuid = tmp + skuid;
                start--;
            }
            return skuid;
        }else {
            throw new RuntimeException("获取skuid的url不能为空");
        }
    }

    public static String removeStrTeShu(String str) {
        //去除特殊字符内容 其中的“（）”为特殊字符
        return str.replaceAll("[^0-9a-zA-Z\u4e00-\u9fa5.，,。?“”#!@$%^&]+","");
    }

    public static String getFileName(String url) {
        return url.substring(url.lastIndexOf("/")+1);
    }

    public static List<String> getSubStrArray(String str, String start, String end) {
        List<String> res = new ArrayList<>();
        String tmp = str;
        int s = -1;
        do {
            s = tmp.indexOf(start);
            int e = tmp.indexOf(end,s+start.length());

            if (s==-1 || e==-1){
                break;
            }

            res.add(tmp.substring(s+start.length(),e));
            if (e == tmp.length()-1){
                break;
            }
            tmp = tmp.substring(e+1);

        }while (s > -1);
        return res;
    }

    public static void main(String[] args) {
        System.out.println(StringUtil.getSubStrArray("abcdeaaccfabcfgjsabfefaijfejfijc","ab","c").toString());
    }

}
