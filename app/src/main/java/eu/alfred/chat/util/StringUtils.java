package eu.alfred.chat.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StringUtils {
    public static String getReadableString(Map<String, String> map) {
        String readableString = "{";
        if (map != null) {
            boolean first = true;
            for (String key : map.keySet()) {
                if (!first) {
                    readableString += ", " + key + ":" + map.get(key);
                } else {
                    first = false;
                    readableString += key + ":" + map.get(key);
                }
            }
        }
        readableString += "}";
        return readableString;
    }
    public static String getReadableString(List list) {
        String readableString = "[";
        if (list != null) {
            boolean first = true;
            for (Object value : list) {
                if (!first) {
                    readableString += ", " + value;
                } else {
                    first = false;
                    readableString += value;
                }
            }
        }
        readableString += "]";
        return readableString;
    }
}
