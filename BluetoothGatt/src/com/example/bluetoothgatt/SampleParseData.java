package com.example.bluetoothgatt;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SampleParseData {
    private String mAd;
    private String mIndiRecords;

    public SampleParseData(byte[] scanRecord) {
        try {
            String decodedRecord = new String(scanRecord, "UTF-8");
//            Log.d("DEBUG", "decoded String : " + ByteArrayToString(scanRecord));
            mAd = ByteArrayToString(scanRecord);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Parse data bytes into individual records
        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);


        // Print individual records
        if (records.size() == 0) {
            Log.i("DEBUG", "Scan Record Empty");
        } else {
//            Log.i("DEBUG", "Scan Record: " + TextUtils.join(",", records));
        }
    }

    public String getStringBAtoS() {
        return mAd;
    }

    public static String ByteArrayToString(byte[] ba) {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }

    public static class AdRecord {

        public AdRecord(int length, int type, byte[] data) {
            String decodedRecord = "";
            try {
                decodedRecord = new String(data, "utf-8");
Log.v("Hello","Lenth="+length+"Type="+type);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

//            Log.d("DEBUG", "Length: " + length + " Type : " + type + " Data : " + ByteArrayToString(data));
        }

        // ...

        public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
            List<AdRecord> records = new ArrayList<AdRecord>();

            int index = 0;
            while (index < scanRecord.length) {
                int length = scanRecord[index++];
                //Done once we run out of records
                if (length == 0) break;

                int type = scanRecord[index];
                //Done if our record isn't a valid type
                if (type == 0) break;
                Log.v("len1",length+" "+type);
                byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

                records.add(new AdRecord(length, type, data));
                //Advance
                index += length;

                Log.v("len2",length+" "+type);
            }

            return records;
        }

        // ...
    }
}
