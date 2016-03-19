package no.sebmik;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class Config {
    private final Log log = new Log(getClass().getSimpleName());
    private static final String filename = "bot.cfg";
    public String name;
    String oauth;
    boolean autoRoulette;
    boolean autoJoinRaffle;
    boolean mentions;
    boolean allMessages;
    boolean filter;
    Integer scrollBack;
    boolean darkMode;
    boolean whispers;
    int betThreshold;
    int betAmount;
    String betChannels;
    File file;

    Config() {
        file = null;

        try {
            String url = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            String s = url.substring(0, url.lastIndexOf("/"));
            file = new File(String.format("%s/%s", s, filename));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (file.exists() && !file.isDirectory()) {
            load();
        }
    }

    private void save() {
        try {
            Properties p = new Properties();
            p.setProperty("botname", name != null ? name : "");
            p.setProperty("oauth", oauth != null ? oauth : "");
            p.setProperty("auto-roulette", String.valueOf(autoRoulette));
            p.setProperty("auto-join-raffle", String.valueOf(autoJoinRaffle));
            p.setProperty("mentions", String.valueOf(mentions));
            p.setProperty("all-messages", String.valueOf(allMessages));
            p.setProperty("scrollback", String.valueOf(scrollBack != null && scrollBack > 0 ? scrollBack : 150));
            p.setProperty("filter", String.valueOf(filter));
            p.setProperty("dark-mode", String.valueOf(darkMode));
            p.setProperty("whispers", String.valueOf(whispers));
            p.setProperty("bet-threshold", String.valueOf(betThreshold));
            p.setProperty("bet-amount", String.valueOf(betAmount));
            p.setProperty("bet-channels", String.valueOf(betChannels));
            p.store(new FileWriter(file), null);
        } catch (IOException e) {
            log.e(e.toString(), true);
        }
    }

    private void load() {
        Properties p = new Properties();
        try (InputStream i = new FileInputStream(file)) {
            p.load(i);
            name = p.getProperty("botname");
            oauth = p.getProperty("oauth");
            autoRoulette = Boolean.parseBoolean(p.getProperty("auto-roulette"));
            autoJoinRaffle = Boolean.parseBoolean(p.getProperty("auto-join-raffle"));
            mentions = Boolean.parseBoolean(p.getProperty("mentions"));
            allMessages = Boolean.parseBoolean(p.getProperty("all-messages"));
            scrollBack = Integer.parseInt(p.getProperty("scrollback"));
            filter = Boolean.parseBoolean(p.getProperty("filter"));
            darkMode = Boolean.parseBoolean(p.getProperty("dark-mode"));
            whispers = Boolean.parseBoolean(p.getProperty("whispers"));
            String t = p.getProperty("bet-threshold");
            betThreshold = t != null ? Integer.parseInt(t) : 0;
            String a = p.getProperty("bet-amount");
            betAmount = a != null ? Integer.parseInt(a) : 0;
            betChannels = p.getProperty("bet-channels");
            i.close();
        } catch (IOException e) {
            log.e(e, true);
        }
    }

    public void setName(String name) {
        log.d("Setting Name: " + name, false);
        this.name = name;
        save();
    }

    void setOauth(String oauth) {
        log.d("Setting Oauth: " + oauth, false);
        this.oauth = oauth;
        save();
    }

    void setAutoRoulette(boolean b) {
        log.d("Setting AutoRoulette: " + b, false);
        this.autoRoulette = b;
        save();
    }

    void setAutoJoinRaffle(boolean b) {
        this.autoJoinRaffle = b;
        log.d("Setting AutoJoinRaffle: " + b, false);
        save();
    }

    void setAllMessages(boolean b) {
        log.d("Setting AllMessage: " + b, false);
        this.allMessages = b;
        save();
    }

    public void setScrollBack(int s) {
        log.d("Setting ScrollBack: " + s, false);
        this.scrollBack = s;
        save();
    }

    void setFilter(boolean b) {
        log.d("Setting Filter: " + b, false);
        this.filter = b;
        save();
    }

    void setDarkMode(boolean b) {
        log.d("Setting DarkMode: " + b, false);
        this.darkMode = b;
        save();
    }

    boolean validNameAndToken() {
        return name.length() > 0 && oauth.length() == 36 && oauth.contains("oauth:");
    }

    void setMentions(boolean mentions) {
        log.d("Setting Mentions: " + mentions, false);
        this.mentions = mentions;
        save();
    }

    void setWhispers(boolean whispers) {
        log.d("Setting Whispers: " + whispers, false);
        this.whispers = whispers;
        save();
    }
}
