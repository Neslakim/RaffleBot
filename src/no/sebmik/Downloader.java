package no.sebmik;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;

public class Downloader {
    Log log = new Log(getClass().getSimpleName());
    private String twitch = "https://static-cdn.jtvnw.net/emoticons/v1/%s/%s";
    private String bttv = "https://cdn.betterttv.net/emote/%s/%sx";

    public void downloadTwitchEmotes(int size) {
        try {
            JSONObject jsonObject = new JSONObject(readUrl("https://twitchemotes.com/api_cache/v2/global.json"));
            JSONObject e = jsonObject.getJSONObject("emotes");
            for (int i = 0; i < e.length(); i++) {
                String name = String.valueOf(e.names().get(i));
                JSONObject emote = e.getJSONObject(name);
                int id = (int) emote.get("image_id");
                download(String.format(twitch, id, (double) size), String.format("emotes/%sx/%s.png", size, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadTwitchEmotes(String channelName, int size) {
        try {
            JSONObject jsonObject = new JSONObject(readUrl("https://twitchemotes.com/api_cache/v2/subscriber.json"));
            JSONObject e = jsonObject.getJSONObject("channels");
            JSONObject e2 = e.getJSONObject(channelName);
            JSONArray e3 = e2.getJSONArray("emotes");
            for (int i = 0; i < e3.length(); i++) {
                JSONObject emote = (JSONObject) e3.get(i);
                String name = emote.getString("code");
                int id = (int) emote.get("image_id");
                download(String.format(twitch, id, (double) size), String.format("emotes/%sx/%s.png", size, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadBTTVEmotes(int size) {
        try {
            JSONObject jsonObject = new JSONObject(readUrl("https://api.betterttv.net/2/emotes"));
            JSONArray e3 = jsonObject.getJSONArray("emotes");
            for (int i = 0; i < e3.length(); i++) {
                JSONObject emote = (JSONObject) e3.get(i);
                String name = emote.getString("code");
                String id = emote.getString("id");
                String imageType = emote.getString("imageType");
                download(String.format(bttv, id, size), String.format("emotes/bttv/%sx/%s.%s", size, name, imageType));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadBTTVEmotes(String channel, int size) {
        try {
            JSONObject jsonObject = new JSONObject(readUrl("https://api.betterttv.net/2/channels/" + channel));
            JSONArray e3 = jsonObject.getJSONArray("emotes");
            for (int i = 0; i < e3.length(); i++) {
                JSONObject emote = (JSONObject) e3.get(i);
                String name = emote.getString("code");
                String id = emote.getString("id");
                String imageType = emote.getString("imageType");
                download(String.format(bttv, id, size), String.format("emotes/bttv/%sx/%s.%s", size, name, imageType));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void download(String url, String name) throws IOException {
        File file = new File(name);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            URL u = new URL(url);
            InputStream is = u.openStream();
            FileOutputStream fos = new FileOutputStream(file, false);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            is.close();
        }
    }

    public void downloadBadge(String channelName) {
        try {
            JSONObject jsonObject = new JSONObject(readUrl("https://twitchemotes.com/api_cache/v2/subscriber.json"));
            JSONObject e = jsonObject.getJSONObject("channels");
            JSONObject e2 = e.getJSONObject(channelName);
            download(e2.getString("badge"), String.format("badge/%s.png", channelName));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getServerCluster(String channelName) {
        try {
            JSONObject jsonObject = new JSONObject(readUrl(String.format("http://api.twitch.tv/api/channels/%s/chat_properties", channelName)));
            return (String) jsonObject.get("cluster");
        } catch (Exception e) {
            log.e(e, true);
        }
        return "";
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
}
