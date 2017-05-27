package youtube.spider.util;

/**
 * @author jianlin
 */
public class FormatUtils {
    private static final String REPLACE_STR_REGEX = ",|\\.|\\(|\\)|\"";
    private static final String EMPTY_STR = "";


    /**
     * 常用播放量单位
     */
    public enum UNIT {
        TEN_THOUSAND("万", 10000L),
        MILLION("百万", 1000000L),
        TEN_MILLION("千万", 10000000L),
        ONE_HUNDRED_MILLION("亿", 100000000L);
        private String name;
        private Long num;

        UNIT(String name, Long num) {
            this.name = name;
            this.num = num;
        }

        public static Long getNumByName(String name) {
            for (UNIT unit : UNIT.values()) {
                if (unit.name.equals(name)) {
                    return unit.num;
                }
            }
            return 1L;
        }

        public static String combineName() {
            StringBuilder sb = new StringBuilder();
            for (UNIT o : UNIT.values()) {
                sb.append(o.name).append("|");
            }
            return sb.toString();
        }
    }

    private static final String POINT = "\\.";
    private static final String UNIT_STR = UNIT.combineName();
    private static final String EXCLUDE_UNIT_STR = "[^" + UNIT_STR + "]";
    private static final String EXCLUDE_STR = UNIT_STR + POINT;
    private static final String FILTER_STR_REGEX = "[\\D && [^" + EXCLUDE_STR + "]]"; //过滤非数字，除了小数点,单位等


    public static Long formatNumber(String numberText) {
        return Long.valueOf(numberText.replaceAll(REPLACE_STR_REGEX, EMPTY_STR));
    }

    /**
     * 向上取整    例如: 30条数据，每页8条  一共4页
     *
     * @param total    数据总数
     * @param pageSize 每次取得数据数
     * @return 一共拆分成多少份
     */
    public static int getPageTotal(long total, int pageSize) {
        if (pageSize == 0) {//分母不能为0
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 向上取整    例如: 30条数据，每页8条  一共4页
     *
     * @param total    数据总数
     * @param pageSize 每次取得数据数
     * @return 一共拆分成多少份
     */
    public static int getPageTotal(int total, int pageSize) {
        if (pageSize == 0) {//分母不能为0
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }


    /**
     * 通用播放量相关字符串转成播放量值
     * 如果有新的单位可以修改UNIT枚举类
     *
     * @param originalString
     * @return
     * @see UNIT
     */
    public static Long getCount(String originalString) {
        String newString = originalString.replaceAll(FILTER_STR_REGEX, EMPTY_STR);
        String unitName = newString.replaceAll(EXCLUDE_UNIT_STR, EMPTY_STR);
        Long unitNum = UNIT.getNumByName(unitName);
        return (long) (Double.valueOf(newString.replaceAll(UNIT_STR, EMPTY_STR)) * unitNum);
    }

    public static void main(String[] args) {
        String str = "135.8亿观看";
        System.out.println(getCount(str));
    }
}
