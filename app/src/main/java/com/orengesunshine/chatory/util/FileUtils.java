package com.orengesunshine.chatory.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.Calendar;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    /**
     * copy cache file from crop activity and save to internal file
     * @param src cached file
     * @param dst file with new file name
     * @throws IOException for streams
     */
    public static void copy(File src, File dst) throws FileNotFoundException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = inStream.getChannel();
            outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            Log.d(TAG, "copy success: ");

        } catch (IOException e) {
            Log.d(TAG, "copy failed: "+e.toString());
        } finally {
            try {
                if (inChannel != null) {
                    inChannel.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
                inStream.close();
                outStream.close();
            } catch (IOException e) {
                Log.d(TAG, "copy, could not close: "+e.toString());
            }
        }
    }

    public static void copyDb(FileInputStream src, File dst) throws FileNotFoundException {
        FileInputStream inStream = src;
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = inStream.getChannel();
            outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            Log.d(TAG, "copy success: ");

        } catch (IOException e) {
            Log.d(TAG, "copy failed: "+e.toString());
        } finally {
            try {
                if (inChannel != null) {
                    inChannel.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
                inStream.close();
                outStream.close();
            } catch (IOException e) {
                Log.d(TAG, "copy, could not close: "+e.toString());
            }
        }
    }


    public static String writeToFile(Context context,String data) {
        String path = String.valueOf(Calendar.getInstance().getTimeInMillis());
        File file = new File(context.getFilesDir(),path);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Log.d(TAG, "writeToFile: copy success ");
        }
        catch (IOException e) {
            Log.d(TAG, "File write failed: " + e.toString());

        }
        return file.getAbsolutePath();
    }

}
