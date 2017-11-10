package top.cmoon.commons.exvo;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Administrator on 2017/8/28.
 */
public class StringUtil extends StringUtils {


    /**
     * rabbit is rubbish
     *
     * @param idList eg: ["1","2"]
     * @return
     */
    public static long[] parseRabbitIdListStr(String idList) {
        String[] arr = idList.replace("[", "").replace("]", "").split(",");

        long[] result = new long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = Long.valueOf(arr[i].replace("＂", ""));
        }
        return result;
    }


    public static String lowerFirstChar(String str) {

        if (str == null) {
            throw new NullPointerException();
        }

        if (str.isEmpty()) {
            return str;
        }

        if (str.length() == 1) {
            return lowerCase(str);
        }

        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }


    public static String upperFirstChar(String str) {

        if (str == null) {
            throw new NullPointerException();
        }

        if (str.isEmpty()) {
            return str;
        }

        if (str.length() == 1) {
            return upperCase(str);
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String questionMark(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0)
                sb.append(",");
            sb.append("?");
        }
        return sb.toString();
    }


    public static void main(String[] args) {
        System.out.println(upperFirstChar("id"));
        System.out.println(lowerFirstChar("Id"));

    }


}
