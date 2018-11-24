package org.josso.agent.http.util;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Properties;

/**
 * This is just an utility class to create a bots.properties file from a newer bots.jason file.
 */
public class BotsFromJSONToPropertiesTransaltor {


    private Writer out;

    public static void main(String[] args) throws Exception {

        //OutputStream out = new ByteArrayOutputStream();

        OutputStream out = new FileOutputStream("/tmp/bots.properties");

        BotsFromJSONToPropertiesTransaltor jsonBot = new BotsFromJSONToPropertiesTransaltor(out);

        InputStream json = jsonBot.getClass().getResourceAsStream("/bots.json");

        jsonBot.translate(json);

    }

    public BotsFromJSONToPropertiesTransaltor(OutputStream out) throws IOException {
        this.out = new PrintWriter(out);
    }

    public void translate(InputStream json) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();


        // use a 1024 buffer to read JSON
        byte[] buff = new byte[1024];
        int read = json.read(buff, 0, 1024);
        while (read > 0) {
            baos.write(buff, 0, read);
            read = json.read(buff, 0, 1024);
        }

        json.close();

        translate(baos.toString());
    }

    public void translate(File json) throws IOException {
        translate(new FileInputStream(json));
    }


    public void translate(String json) {
        try {


            Properties botsProps = new Properties();
            JSONObject botsJson = new JSONObject(json);

            JSONArray botsArray = botsJson.getJSONArray("bots");

            int botsNumber = botsArray.length();
            for (int i = 0 ; i < botsNumber ; i++) {

                JSONObject botDefinition = botsArray.getJSONObject(i);

                String pattern = botDefinition.getString("pattern");
                String url = botDefinition.optString("url", "");

                JSONArray instances = botDefinition.getJSONArray("instances");

                int j =  instances.length();
                for (j = 0 ; j < instances.length() ; j++) {
                    String instance = instances.getString(j);

                    // TODO : Use the pattern as regular expression pattern!1
                    writeProperty("robot-id", pattern + "-0" + j);
                    writeProperty("robot-name", pattern + "-0" + j);
                    writeProperty("robot-cover-url", url);
                    writeProperty("robot-useragent", instance);
                    writeProperty("robot-status", "Active");
                    out.write("\n");
                }

            }

            out.flush();
            out.close();

            System.out.println(botsJson.toString());

        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected void writeProperty(String name, String value) throws IOException {
        out.write(name + ": " + value + "\n");
    }


}
