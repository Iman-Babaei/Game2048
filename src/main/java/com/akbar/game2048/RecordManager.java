package com.akbar.game2048;

import java.io.*;
import java.util.Properties;

public class RecordManager {
    public final String SESSION_PROPERTIES_FILENAME;
    private final Properties props = new Properties();

    public RecordManager(int gridSize) {
        SESSION_PROPERTIES_FILENAME = "game2048_" + gridSize + "_record.properties";
    }
    //todo make using of props simple
    public void saveRecord(Integer score) {
        try {
            props.setProperty("record", Integer.toString(score));
            props.store(new FileWriter(SESSION_PROPERTIES_FILENAME), SESSION_PROPERTIES_FILENAME);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public int restoreRecord() {
        Reader reader = null;
        try {
            reader = new FileReader(SESSION_PROPERTIES_FILENAME);
            props.load(reader);
        } catch (FileNotFoundException e) {
            return 0;
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
        String score = props.getProperty("record");
//        if (score != null)
//            return new Integer(score);
        return 0;
    }
}