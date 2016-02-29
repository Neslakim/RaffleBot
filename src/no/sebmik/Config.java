package no.sebmik;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class Config {
    private final Logger log = LoggerFactory.getLogger(RaffleBot.class);
    public String name;
    public String oauth;
    public String winMessage;
    public boolean autoRoulette;
    public boolean autoJoinRaffle;
    public boolean showLinks;
    private static final String filename = "bot.cfg";

    public Config() {
        File f = new File(filename);
        if (f.exists() && !f.isDirectory()) {
            load();
        }
    }

    private void save() {
        try {
            Properties p = new Properties();
            p.setProperty("botname", name);
            p.setProperty("oauth", oauth);
            p.setProperty("win-message", winMessage);
            p.setProperty("auto-roulette", String.valueOf(autoRoulette));
            p.setProperty("auto-join-raffle", String.valueOf(autoJoinRaffle));
            p.setProperty("show-links", String.valueOf(showLinks));
            p.store(new FileWriter(filename), null);
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    private void load() {
        Properties p = new Properties();
        try (InputStream i = new FileInputStream(filename)) {
            p.load(i);
            name = p.getProperty("botname");
            oauth = p.getProperty("oauth");
            winMessage = p.getProperty("win-message");
            autoRoulette = Boolean.parseBoolean(p.getProperty("auto-roulette"));
            autoJoinRaffle = Boolean.parseBoolean(p.getProperty("auto-join-raffle"));
            showLinks = Boolean.parseBoolean(p.getProperty("show-links"));
            i.close();
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    public void setName(String name) {
        this.name = name;
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

    public void setAutoJoinRaffle(boolean autoJoinRaffle) {
        this.autoJoinRaffle = autoJoinRaffle;
        save();
    }

    public void setShowLinks(boolean l) {
        this.showLinks = l;
        save();
    }

    public boolean validNameAndToken() {
        return name.length() > 0 && oauth.length() == 36 && oauth.contains("oauth:");
    }
}
