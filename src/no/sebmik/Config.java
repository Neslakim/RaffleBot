package no.sebmik;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class Config {
    private Logger log = LoggerFactory.getLogger(RaffleBot.class);
    public String botName;
    public String oauth;
    public String winMessage;
    public boolean autoRoulette;
    private static final String filename = "bot.cfg";

    public Config() {
        File f = new File(filename);
        if (f.exists() && !f.isDirectory()) {
            load();
        }
    }

    public Config(String bn, String oa, String wm, boolean ar) {
        this.botName = bn;
        this.oauth = oa;
        this.winMessage = (wm != null && wm.length() > 0) ? wm : "";
        this.autoRoulette = ar;
    }

    public void save() {
        try {
            Properties p = new Properties();
            p.setProperty("botname", botName);
            p.setProperty("oauth", oauth);
            p.setProperty("win-message", winMessage);
            p.setProperty("auto-roulette", String.valueOf(autoRoulette));
            p.store(new FileWriter(filename), null);
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    private void load() {
        Properties p = new Properties();
        try (InputStream i = new FileInputStream(filename)) {
            p.load(i);
            botName = p.getProperty("botname");
            oauth = p.getProperty("oauth");
            winMessage = p.getProperty("win-message");
            autoRoulette = Boolean.parseBoolean(p.getProperty("auto-roulette"));
            i.close();
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    public void setBotName(String botName) {
        this.botName = botName;
        save();
    }

    public void setOauth(String oauth) {
        this.oauth = oauth;
        save();
    }

    public void setWinMessage(String winMessage) {
        this.winMessage = winMessage;
        save();
    }

    public void setAutoRoulette(boolean autoRoulette) {
        this.autoRoulette = autoRoulette;
        save();
    }
}
