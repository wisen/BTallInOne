package swordbearer.audio;

import android.util.Log;

/**
 * Created by wisen on 2016-06-28.
 */
public class AudioCodec {

    private static final String TAG = "AudioDecoder";
    private static final boolean D = true;

    static {
        if (D) Log.d(TAG, "load audioWraper jni");
        System.loadLibrary("audiowrapper");
    }

    // initialize decoder and encoder
    public static native int audio_codec_init(int mode);

    // encode
    public static native int audio_encode(byte[] sample, int sampleOffset,
                                          int sampleLength, byte[] data, int dataOffset);

    // decode
    public static native int audio_decode(byte[] data, int dataOffset,
                                          int dataLength, byte[] sample, int sampleLength);
}
