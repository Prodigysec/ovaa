package com.google.gson.internal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
/* loaded from: classes.dex */
public class PreJava9DateFormatProvider {
    public static DateFormat getUSDateFormat(int style) {
        return new SimpleDateFormat(getDateFormatPattern(style), Locale.US);
    }

    public static DateFormat getUSDateTimeFormat(int dateStyle, int timeStyle) {
        String pattern = getDatePartOfDateTimePattern(dateStyle) + " " + getTimePartOfDateTimePattern(timeStyle);
        return new SimpleDateFormat(pattern, Locale.US);
    }

    private static String getDateFormatPattern(int style) {
        if (style != 0) {
            if (style != 1) {
                if (style != 2) {
                    if (style == 3) {
                        return "M/d/yy";
                    }
                    throw new IllegalArgumentException("Unknown DateFormat style: " + style);
                }
                return "MMM d, y";
            }
            return "MMMM d, y";
        }
        return "EEEE, MMMM d, y";
    }

    private static String getDatePartOfDateTimePattern(int dateStyle) {
        if (dateStyle != 0) {
            if (dateStyle != 1) {
                if (dateStyle != 2) {
                    if (dateStyle == 3) {
                        return "M/d/yy";
                    }
                    throw new IllegalArgumentException("Unknown DateFormat style: " + dateStyle);
                }
                return "MMM d, yyyy";
            }
            return "MMMM d, yyyy";
        }
        return "EEEE, MMMM d, yyyy";
    }

    private static String getTimePartOfDateTimePattern(int timeStyle) {
        if (timeStyle == 0 || timeStyle == 1) {
            return "h:mm:ss a z";
        }
        if (timeStyle != 2) {
            if (timeStyle == 3) {
                return "h:mm a";
            }
            throw new IllegalArgumentException("Unknown DateFormat style: " + timeStyle);
        }
        return "h:mm:ss a";
    }
}
