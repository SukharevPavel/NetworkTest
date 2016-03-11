package ru.sukharev.networktest.utils;

/**
 * Like {@link java.util.concurrent.TimeUnit}, using to convert volume of information.
 */
public enum  ByteUnit {

    BYTES {
        public long toBytes(long b) { return b; };
        public float toKilobytes(long b) { return (float) b/ONE_KILOBYTE; };
        public float toMegabytes(long b) { return (float) b/ONE_MEGABYTE; };
    },

    KILOBYTES {
        public long toBytes(long b) { return b * ONE_KILOBYTE; };
        public float toKilobytes(long b) { return b; };
        public float toMegabytes(long b) { return (float) b * ONE_KILOBYTE / ONE_MEGABYTE; };
    },

    MEGABYTES {
        public long toBytes(long b) { return b * ONE_MEGABYTE; };
        public float toKilobytes(long b) { return b * ONE_MEGABYTE / ONE_KILOBYTE; };
        public float toMegabytes(long b) { return b; };
    };

    public final static long ONE_KILOBYTE = 1024;
    public final static long ONE_MEGABYTE = ONE_KILOBYTE * 1024;

    public long toBytes(long b){
        throw new AbstractMethodError();
    }

    public float toKilobytes(long b){
        throw new AbstractMethodError();
    }

    public float toMegabytes(long b){
        throw new AbstractMethodError();
    }

}
