package com.github.djaquels.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.json.JSONException;

public final class Labels {
    private static Labels labels;
    public String local_lang;
    private JSONObject conf;

    private Labels(String lang) {
        this.local_lang = lang;
        String fileName = lang + ".json";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr);
                StringBuilder jsonContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }

                conf = new JSONObject(jsonContent.toString());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        

    }

    public static Labels getInstance(String lang) {
        if (labels == null) {
            labels = new Labels(lang);
        }
        return labels;
    }

    public String getValue(String key){
        String value = this.conf.getString(key);
        return value;
    }

    public JSONObject getWindowLabels(String key){
        JSONObject window = this.conf.getJSONObject(key);
        return window;
    }

    public String getWindowValue(JSONObject window, String key){
        return window.getString(key);
    }
}
