package youtube.spider.util;

import org.json.JSONObject;

/**
 * @author jianlin210349
 */
public class ParseJsonUtils {


    public static String getValueByKey(String jsonStr, String key) {
        JSONObject jsonObject = new JSONObject(jsonStr);
        return jsonObject.getString(key);
    }
}
