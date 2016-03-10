package no.sebmik;

import java.io.*;
import java.util.Properties;

public class Config {
    Log log = new Log(getClass().getSimpleName());
    private static final String filename = "bot.cfg";
    public String name;
    public String oauth;
    public boolean autoRoulette;
    public boolean autoJoinRaffle;
    public boolean showLinks;
    public boolean mentions;
    public boolean allMessages;
    public boolean filter;
    public Integer scrollBack;
    public boolean darkMode;
    public boolean whispers;
    public int betThreshold;
    public int betAmount;
    public String betChannels;

    public Config() {
        File f = new File(filename);
        if (f.exists() && !f.isDirectory()) {
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
            p.setProperty("show-links", String.valueOf(showLinks));
            p.setProperty("mentions", String.valueOf(mentions));
            p.setProperty("all-messages", String.valueOf(allMessages));
            p.setProperty("scrollback", String.valueOf(scrollBack != null && scrollBack > 0 ? scrollBack : 150));
            p.setProperty("filter", String.valueOf(filter));
            p.setProperty("dark-mode", String.valueOf(darkMode));
            p.setProperty("whispers", String.valueOf(whispers));
            p.setProperty("bet-threshold", String.valueOf(betThreshold));
            p.setProperty("bet-amount", String.valueOf(betAmount));
            p.setProperty("bet-channels", String.valueOf(betChannels));
            p.store(new FileWriter(filename), null);
        } catch (IOException e) {
            log.e(e.toString(), true);
        }
    }

    private void load() {
        Properties p = new Properties();
        try (InputStream i = new FileInputStream(filename)) {
            p.load(i);
            name = p.getProperty("botname");
            oauth = p.getProperty("oauth");
            autoRoulette = Boolean.parseBoolean(p.getProperty("auto-roulette"));
            autoJoinRaffle = Boolean.parseBoolean(p.getProperty("auto-join-raffle"));
            showLinks = Boolean.parseBoolean(p.getProperty("show-links"));
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

    public void setOauth(String oauth) {
        log.d("Setting Oauth: " + oauth, false);
        this.oauth = oauth;
        save();
    }

    public void setAutoRoulette(boolean b) {
        log.d("Setting AutoRoulette: " + b, false);
        this.autoRoulette = b;
        save();
    }

    public void setAutoJoinRaffle(boolean b) {
        this.autoJoinRaffle = b;
        log.d("Setting AutoJoinRaffle: " + b, false);
        save();
    }

    public void setShowLinks(boolean b) {
        log.d("Setting ShowLinks: " + b, false);
        this.showLinks = b;
        save();
    }

    public void setAllMessages(boolean b) {
        log.d("Setting AllMessage: " + b, false);
        this.allMessages = b;
        save();
    }

    public void setScrollBack(int s) {
        log.d("Setting ScrollBack: " + s, false);
        this.scrollBack = s;
        save();
    }

    public void setFilter(boolean b) {
        log.d("Setting Filter: " + b, false);
        this.filter = b;
        save();
    }

    public void setDarkMode(boolean b) {
        log.d("Setting DarkMode: " + b, false);
        this.darkMode = b;
        save();
    }

    public boolean validNameAndToken() {
        return name.length() > 0 && oauth.length() == 36 && oauth.contains("oauth:");
    }

    public void setMentions(boolean mentions) {
        log.d("Setting Mentions: " + mentions, false);
        this.mentions = mentions;
        save();
    }

    public void setWhispers(boolean whispers) {
        log.d("Setting Whispers: " + whispers, false);
        this.whispers = whispers;
        save();
    }
}
