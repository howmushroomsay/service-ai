package com.fr.dp.service.utils;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * @author sandzhang[sandzhangtoo@gmail.com]
 */
public class StringUtils {
    private static final Pattern SQL_FLAT_PATTERN = Pattern.compile("\t|\r|\n");
    private static final Pattern MULTI_BLANK_SPACE_PATTERN = Pattern.compile("\\s{2,}");
    public static final Pattern PARAM_SYMBOL = Pattern.compile("\\$\\{(.*?)\\}");

    public static final String EMPTY = "";
    public static final String COMMA = ",";
    public static final String CHINESE_COMMA = "，";
    public static final String SPACE = " ";
    public static final String OR = "|";
    public static final String CHINESE_OR = "｜";

    public static final char SINGLE_LINE_COMMENT_START = '-';
    public static final char MULTI_LINE_COMMENT_RIM = '/';
    public static final char MULTI_LINE_COMMENT_INNER_RIM = '*';
    public static final char BLANK_CHAR = ' ';
    public static final char SEMICOLON = ';';
    public static final char SINGLE_QUOTES = '\'';
    public static final char DOUBLE_QUOTES = '"';
    public static final char LF = '\n';
    public static final char CR = '\r';
    public static final char HT = '\t';

    private static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Example: subString("12345","1","4")=23
     *
     * @param src
     * @param start
     * @param to
     * @return
     */
    public static Integer subStringToInteger(String src, String start, String to) {
        return stringToInteger(subString(src, start, to));
    }

    /**
     * Example: subString("abcd","a","c")="b"
     *
     * @param src
     * @param start null while start from index=0
     * @param to    null while to index=src.length
     * @return
     */
    public static String subString(String src, String start, String to) {
        int indexFrom = start == null ? 0 : src.indexOf(start);
        int indexTo = to == null ? src.length() : src.indexOf(to);
        if (indexFrom < 0 || indexTo < 0 || indexFrom > indexTo) {
            return null;
        }

        if (null != start) {
            indexFrom += start.length();
        }

        return src.substring(indexFrom, indexTo);

    }

    /**
     * Example: subString("abcdc","a","c",true)="bcd"
     *
     * @param src
     * @param start  null while start from index=0
     * @param to     null while to index=src.length
     * @param toLast true while to index=src.lastIndexOf(to)
     * @return
     */
    public static String subString(String src, String start, String to, boolean toLast) {
        if (!toLast) {
            return subString(src, start, to);
        }
        int indexFrom = start == null ? 0 : src.indexOf(start);
        int indexTo = to == null ? src.length() : src.lastIndexOf(to);
        if (indexFrom < 0 || indexTo < 0 || indexFrom > indexTo) {
            return null;
        }

        if (null != start) {
            indexFrom += start.length();
        }

        return src.substring(indexFrom, indexTo);

    }

    /**
     * @param in
     * @return
     */
    public static Integer stringToInteger(String in) {
        if (in == null) {
            return null;
        }
        in = in.trim();
        if (in.length() == 0) {
            return null;
        }

        try {
            return Integer.parseInt(in);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean equals(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    public static boolean equalsIgnoreCase(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equalsIgnoreCase(b);
    }

    public static boolean isEmpty(CharSequence value) {
        return value == null || value.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence value) {
        return !isEmpty(value);
    }

    public static boolean isEmptyButNotNull(CharSequence value) {
        return value != null && value.length() == 0;
    }

    public static int lowerHashCode(String text) {
        if (text == null) {
            return 0;
        }
//        return text.toLowerCase().hashCode();
        int h = 0;
        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                ch = (char) (ch + 32);
            }

            h = 31 * h + ch;
        }
        return h;
    }

    public static boolean isNumber(String str) {
        if (str.length() == 0) {
            return false;
        }
        int sz = str.length();
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (str.charAt(0) == '-') ? 1 : 0;
        if (sz > start + 1) {
            if (str.charAt(start) == '0' && str.charAt(start + 1) == 'x') {
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < str.length(); i++) {
                    char ch = str.charAt(i);
                    if ((ch < '0' || ch > '9')
                            && (ch < 'a' || ch > 'f')
                            && (ch < 'A' || ch > 'F')) {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            char ch = str.charAt(i);
            if (ch >= '0' && ch <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (ch == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (ch == 'e' || ch == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (ch == '+' || ch == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < str.length()) {
            char ch = str.charAt(i);

            if (ch >= '0' && ch <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (ch == 'e' || ch == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (!allowSigns
                    && (ch == 'd'
                    || ch == 'D'
                    || ch == 'f'
                    || ch == 'F')) {
                return foundDigit;
            }
            if (ch == 'l'
                    || ch == 'L') {
                // not allowing L with an exponent
                return foundDigit && !hasExp;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

    public static boolean isNumber(char[] chars) {
        if (chars.length == 0) {
            return false;
        }
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1) {
            if (chars[start] == '0' && chars[start + 1] == 'x') {
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    char ch = chars[i];
                    if ((ch < '0' || ch > '9')
                            && (ch < 'a' || ch > 'f')
                            && (ch < 'A' || ch > 'F')) {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            char ch = chars[i];
            if (ch >= '0' && ch <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (ch == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (ch == 'e' || ch == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (ch == '+' || ch == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            char ch = chars[i];
            if (ch >= '0' && ch <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (ch == 'e' || ch == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (!allowSigns
                    && (ch == 'd'
                    || ch == 'D'
                    || ch == 'f'
                    || ch == 'F')) {
                return foundDigit;
            }
            if (ch == 'l'
                    || ch == 'L') {
                // not allowing L with an exponent
                return foundDigit && !hasExp;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

    public static String formatDateTime19(long millis, TimeZone timeZone) {
        Calendar cale = timeZone == null
                ? Calendar.getInstance()
                : Calendar.getInstance(timeZone);
        cale.setTimeInMillis(millis);

        int year = cale.get(Calendar.YEAR);
        int month = cale.get(Calendar.MONTH) + 1;
        int dayOfMonth = cale.get(Calendar.DAY_OF_MONTH);
        int hour = cale.get(Calendar.HOUR_OF_DAY);
        int minute = cale.get(Calendar.MINUTE);
        int second = cale.get(Calendar.SECOND);

        char[] chars = new char[19];
        chars[0] = (char) (year / 1000 + '0');
        chars[1] = (char) ((year / 100) % 10 + '0');
        chars[2] = (char) ((year / 10) % 10 + '0');
        chars[3] = (char) (year % 10 + '0');
        chars[4] = '-';
        chars[5] = (char) (month / 10 + '0');
        chars[6] = (char) (month % 10 + '0');
        chars[7] = '-';
        chars[8] = (char) (dayOfMonth / 10 + '0');
        chars[9] = (char) (dayOfMonth % 10 + '0');
        chars[10] = ' ';
        chars[11] = (char) (hour / 10 + '0');
        chars[12] = (char) (hour % 10 + '0');
        chars[13] = ':';
        chars[14] = (char) (minute / 10 + '0');
        chars[15] = (char) (minute % 10 + '0');
        chars[16] = ':';
        chars[17] = (char) (second / 10 + '0');
        chars[18] = (char) (second % 10 + '0');
        return new String(chars);
    }

    public static String removeNameQuotes(String s) {
        if (s == null || s.length() <= 1) {
            return null;
        }
        int len = s.length();
        char c0 = s.charAt(0);
        char last = s.charAt(len - 1);

        if (c0 == last && (c0 == '`' || c0 == '\'' || c0 == '\"')) {
            return s.substring(1, len - 1);
        }
        return s;
    }

    // 把sql中的换行符、制表符都换成空格，方便替换
    public static String flatString(String s) {
        // 去除换行符等
        String oneLineSQL = SQL_FLAT_PATTERN.matcher(s).replaceAll(" ");
        // 去除多余空格
        return MULTI_BLANK_SPACE_PATTERN.matcher(oneLineSQL).replaceAll(" ");
    }


    /**
     * 通过逗号切割字符串，且忽略引号内的逗号
     * “a,b,'c,d'” => ["a","b","c,d"]
     * “a,b,\"c,d\“” => ["a","b","c,d"]
     */
    public static List<String> splitByComma(String s) {
        return splitBySeparator(s, ',');
    }

    /**
     * 通过分号切割字符串，且忽略引号内的逗号
     * “a;b;'c;d'” => ["a","b","c,d"]
     * “a;b,\"c;d\“” => ["a","b","c,d"]
     */
    public static List<String> splitBySemicolon(String s) {
        return splitBySeparator(s, ';');
    }

    /**
     * 通过指定分割付切割字符串，且忽略引号内的逗号
     */
    public static List<String> splitBySeparator(String s, char separator) {
        List<String> result = new ArrayList<>();
        int start = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\"') {
                inDoubleQuote = !inDoubleQuote;
            } else if (s.charAt(i) == '\'') {
                inSingleQuote = !inSingleQuote;
            }
            boolean atLastChar = (i == s.length() - 1);
            if (atLastChar) {
                if (s.charAt(s.length() - 1) == separator) {
                    result.add(s.substring(start, s.length() - 1));
                } else {
                    result.add(s.substring(start));
                }
            } else if (s.charAt(i) == separator && !inDoubleQuote && !inSingleQuote) {
                result.add(s.substring(start, i));
                start = i + 1;
            }
        }
        return result;
    }

    /**
     * 返回字符c在字符串s中引号之外的第一次出现的索引
     */
    public static int indexOfOuterQuote(String s, char c) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\"') {
                inDoubleQuote = !inDoubleQuote;
            } else if (s.charAt(i) == '\'') {
                inSingleQuote = !inSingleQuote;
            }
            if (s.charAt(i) == c && !inDoubleQuote && !inSingleQuote) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 清了个寂寞，先占个位置吧。
     *
     * @param sql
     * @return
     */
    public static String clearSqlLonely(String sql) {
        return sql;
    }

    /**
     * 去除注释
     * <p>
     * <li>-- 注释1</li>
     * <li>--注释2</li>
     * <li>|*注释3*|</li>
     * <li>|**注释4**|</li>
     * </p>
     *
     * @param sql
     * @return
     */
    public static String clearSqlComments(String sql) {
        final char[] sqlCharArray = sql.toCharArray();

        boolean singleLineCommentsStarted = false;
        boolean multiLineCommentsStarted = false;
        boolean lastBlank = false;
        boolean insideSingleQuotes = false;
        boolean insideDoubleQuotes = false;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sqlCharArray.length; i++) {
            char current = sqlCharArray[i];
            char next = (i < sqlCharArray.length - 1) ? sqlCharArray[i + 1] : BLANK_CHAR;

            if (current == SINGLE_QUOTES) {
                if (!insideSingleQuotes && !insideDoubleQuotes) {
                    insideSingleQuotes = true;
                } else if (insideSingleQuotes) {
                    insideSingleQuotes = false;
                }
            }

            if (current == DOUBLE_QUOTES) {
                if (!insideSingleQuotes && !insideDoubleQuotes) {
                    insideDoubleQuotes = true;
                } else if (insideDoubleQuotes) {
                    insideDoubleQuotes = false;
                }
            }

            if (current == SINGLE_LINE_COMMENT_START && next == SINGLE_LINE_COMMENT_START && !insideSingleQuotes && !insideDoubleQuotes) {
                singleLineCommentsStarted = true;
            }

            if (current == MULTI_LINE_COMMENT_RIM && next == MULTI_LINE_COMMENT_INNER_RIM && !insideSingleQuotes && !insideDoubleQuotes) {
                multiLineCommentsStarted = true;
            }

            if (!(singleLineCommentsStarted || multiLineCommentsStarted)) {
                if (current == LF) {
                    if (!lastBlank) {
                        sb.append(BLANK_CHAR);
                        lastBlank = true;
                    }
                } else {
                    if (!lastBlank) {
                        sb.append(current);
                        lastBlank = (current == BLANK_CHAR);
                    } else if (current != BLANK_CHAR) {
                        sb.append(current);
                        lastBlank = false;
                    }
                }
            }

            if (singleLineCommentsStarted && (current == LF)) {
                singleLineCommentsStarted = false;
                if (!lastBlank) {
                    sb.append(BLANK_CHAR);
                    lastBlank = true;
                }
            }

            if (multiLineCommentsStarted && (current == MULTI_LINE_COMMENT_INNER_RIM) && next == MULTI_LINE_COMMENT_RIM) {
                multiLineCommentsStarted = false;
                i++;
            }
        }

        return sb.toString().trim();
    }

    public static String getURLDecodeString(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取提供的字节数组并将其转换为十六进制编码的String
     *
     * @param toBeConverted 要转换的字节数。
     * @return 十六进制编码的String
     */
    public static String convertToHexString(byte[] toBeConverted) {
        if (toBeConverted == null) {
            throw new NullPointerException("Parameter to be converted can not be null");
        }

        char[] converted = new char[toBeConverted.length * 2];
        for (int i = 0; i < toBeConverted.length; i++) {
            byte b = toBeConverted[i];
            converted[i * 2] = HEX_CHARS[b >> 4 & 0x0F];
            converted[i * 2 + 1] = HEX_CHARS[b & 0x0F];
        }

        return String.valueOf(converted);
    }

    /**
     * 还原编码为十六进制字符串的字节数组
     * <p>
     *
     * @param hexString 要还原的十六进制字符串
     * @return 十六进制编码的字节数组
     */
    public static byte[] hexStringToByteArray(String hexString) {
        if (hexString == null) {
            return null;
        }

        int length = hexString.length();

        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }

        return bytes;
    }



    public static String cutOffStr(String str, int maxLength) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        if (str.length() <= maxLength) {
            return str; // 如果字符串长度已经小于或等于最大长度，直接返回
        }
        return str.substring(0, maxLength) + "..."; // 截断并添加省略号
    }
}
