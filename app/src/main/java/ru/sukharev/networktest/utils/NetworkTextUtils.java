package ru.sukharev.networktest.utils;

import android.content.Context;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import ru.sukharev.networktest.R;

/**
 * TextUtils, used to show input data, such as speed, time, volume, in readable format.
 */
public class NetworkTextUtils {

    Context mContext;
    public static final String SPACE = " ";
    public static final String SLASH = "/";
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public NetworkTextUtils(Context ctx) {
        mContext = ctx;
    }

    public String formatVolume(ByteUnit byteUnit, long value){

        StringBuilder builder = new StringBuilder(DECIMAL_FORMAT.format(value));
        builder.append(SPACE);
        return appendVolumeSuffix(byteUnit, builder).toString();
    }

    public String formatSpeed(ByteUnit byteUnit, TimeUnit timeUnit, float value){
        StringBuilder builder = new StringBuilder(DECIMAL_FORMAT.format(value));
        builder.append(SPACE);
        appendVolumeSuffix(byteUnit, builder);
        builder.append(SLASH);
        return appendTimeSuffix(timeUnit, builder).toString();
    }

    public String formatTime(TimeUnit timeUnit, int time) {
        StringBuilder builder = new StringBuilder(String.valueOf(time));
        Log.i(getClass().getSimpleName(), builder.toString());
        builder.append(SPACE);
        Log.i(getClass().getSimpleName(), builder.toString());
        return appendTimeSuffix(timeUnit, builder).toString();
    }


    private StringBuilder appendVolumeSuffix(ByteUnit byteUnit, StringBuilder builder) {
        switch (byteUnit) {
            case BYTES: builder.append(mContext.getString(R.string.byte_suffix));
                return builder;
            case KILOBYTES: builder.append(mContext.getString(R.string.kilobyte_suffix));
                return builder;
            case MEGABYTES: builder.append(mContext.getString(R.string.megabyte_suffix));
                return builder;
            default: return builder;
        }
    }

    private StringBuilder appendTimeSuffix(TimeUnit timeUnit, StringBuilder builder) {
        switch (timeUnit) {
            case MILLISECONDS:
                builder.append(mContext.getString(R.string.millisecond));
                return builder;
            case SECONDS:
                builder.append(mContext.getString(R.string.second));
                return builder;
            default:
                return builder;
        }
    }
}
