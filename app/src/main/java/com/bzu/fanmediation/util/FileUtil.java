package com.bzu.fanmediation.util;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 文件工具类
 */
public class FileUtil {

    public static void writeStringToFile(@Nullable String txt, @Nullable String filePath) {
        FileOutputStream outStream = null;
        try {
            File file = new File(filePath);
            file.deleteOnExit();
            boolean createSuccess = file.createNewFile();
            outStream = new FileOutputStream(file);
            outStream.write(txt.getBytes());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static @Nullable
    String readStringFromFile(@Nullable String filePath) {
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = openInputStream(filePath);
            if (null != is) {
                StringBuilder sb = new StringBuilder();
                InputStreamReader in = new InputStreamReader(is);
                br = new BufferedReader(in);
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static @Nullable
    InputStream openInputStream(@Nullable String dir) {
        if (TextUtils.isEmpty(dir)) return null;
        InputStream is = null;
        try {
            File file = new File(dir);
            if (!file.exists()) return null;
            is = new FileInputStream(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }
}