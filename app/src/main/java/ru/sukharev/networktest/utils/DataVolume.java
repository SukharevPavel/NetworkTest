package ru.sukharev.networktest.utils;

/**
 * Class to format volume to appropriate unit (Bytes, KB, MB), also good to {@link NetworkTextUtils},
 * because it has info about volume unit.
 */
public class DataVolume {

    public ByteUnit unit;
    public float value;
    
    public DataVolume(ByteUnit unit, float value){
        this.unit = unit;
        this.value = value;
    }

    public static DataVolume appropriateFormat(long data){
        if (data>ByteUnit.ONE_MEGABYTE)
            return new DataVolume(ByteUnit.MEGABYTES, ByteUnit.BYTES.toMegabytes(data));
        else if (data>ByteUnit.ONE_KILOBYTE)
            return new DataVolume(ByteUnit.KILOBYTES, ByteUnit.BYTES.toKilobytes(data));
        else return new DataVolume(ByteUnit.BYTES, ByteUnit.BYTES.toBytes(data));

    }
}
