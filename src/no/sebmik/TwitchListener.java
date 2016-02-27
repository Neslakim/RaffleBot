package no.sebmik;

import ch.qos.logback.classic.Level;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TwitchListener extends ListenerAdapter {
    private Logger log = LoggerFactory.getLogger(RaffleBot.class);
    private final String raffleStartRegex = "^A multi-raffle has begun, [0-9]+ points will be split among the winners. type !join to join the raffle! The raffle will end in [0-9]+ seconds";
    private final String raffleStartRegex2 = "^A raffle has begun for [0-9]+ points. type !join to join the raffle! The raffle will end in [0-9]+ seconds";
    private final String raffleResultRegex = "^The raffle has finished!.+\\b%s\\b,.+won [0-9]+ points! PogChamp";
    private final String raffleResultRegex2 = "^(.+)?\\b%s\\b(.+)?won [0-9]+ points each!";
    private final String rouletteWonRegex = "^%s won [0-9]+ points in roulette and now has [0-9]+ points!";
    private final String rouletteLostRegex = "^%s lost [0-9]+ points in roulette and now has [0-9]+ points!";
    private final String URL = "irc.twitch.tv";
    private final int PORT = 6667;
    private Config config;
    public PircBotX bot;
    private final Controller controller;
    private long lastJoined;

    public TwitchListener(Controller controller) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);
        this.controller = controller;
    }

    public void init(Config config) {
        this.config = config;
        Configuration configuration = new Configuration.Builder()
                .setServerPassword(this.config.oauth)
                .setOnJoinWhoEnabled(false)
                .setCapEnabled(true)
                .addCapHandler(new EnableCapHandler("twitch.tv/membership"))
                .addCapHandler(new EnableCapHandler("twitch.tv/commands"))
                .addCapHandler(new EnableCapHandler("twitch.tv/tags"))
                .setName(this.config.botName)
                .addServer(URL, PORT)
                .setAutoNickChange(false)
                .setMessageDelay(0)
                .addListener(this)
//                .addAutoJoinChannels(channels)
                .setAutoReconnect(true)
                .setAutoReconnectAttempts(99999)
                .setAutoReconnectDelay(1000)
                .buildConfiguration();
        bot = new PircBotX(configuration);
    }

    public void start() {
        new Thread(() -> {
            try {
                bot.startBot();
            } catch (IOException | IrcException e) {
                log.error(e.toString());
            }
        }).start();
    }

    public void stop() {
        bot.stopBotReconnect();
        bot.send().quitServer();
    }

    @Override
    public void onAction(ActionEvent event) {
        String channelName = event.getChannel().getName();
        User user = event.getUser();
        String username = user.getNick();
        String message = event.getMessage();
        if (username.equalsIgnoreCase("snusbot") || username.equalsIgnoreCase("pajbot")) {
            if (checkStart(message)) {
                joinRaffle(event);
                List<Integer> num = getNumbers(message);
                if (message.contains("multi")) {
                    controller.addTextToMain(String.format("(%s) Joining multi-raffle for %s points. The raffle ends in %s seconds.", channelName, num.get(0), num.get(1)));
                } else {
                    controller.addTextToMain(String.format("(%s) Joining raffle for %s points. The raffle ends in %s seconds.", channelName, num.get(0), num.get(1)));
                }
            }
            if (checkWon(message, config.botName)) {
                int price = getPrice(message);
                controller.addTextToMain(String.format("(%s) Congratulations, you won %s points in a raffle.", channelName, price));
                if (controller.config.autoRoulette) {
                    sendMessage(event.getChannel(), String.format("!roulette %s", price));
                }
                String winMessage = controller.config.winMessage;
                if (winMessage != null && winMessage.length() > 0) {
                    sendMessage(event.getChannel(), winMessage);
                }
            }
            checkRoulette(message, config.botName, channelName);
        }
    }

    private void checkRoulette(String s, String username, String channelName) {
        Pattern p = Pattern.compile(String.format(rouletteWonRegex, username));
        Matcher m = p.matcher(s.toLowerCase());
        Pattern p2 = Pattern.compile(String.format(rouletteLostRegex, username));
        Matcher m2 = p2.matcher(s.toLowerCase());
        List<Integer> num = getNumbers(s);
        if (m.find()) {
            controller.addTextToMain(String.format("(%s) You won %s points in roulette. You now have %s points", channelName, num.get(0), num.get(1)));
        } else if (m2.find()) {
            controller.addTextToMain(String.format("(%s) You lost %s points in roulette. You now have %s points", channelName, num.get(0), num.get(1)));
        }
    }

    private void joinRaffle(ActionEvent event) {
        if (System.currentTimeMillis() - lastJoined > 30000) {
            sendMessage(event.getChannel(), "!join");
        } else {
            sendMessage(event.getChannel(), "!Join");
        }
        lastJoined = System.currentTimeMillis();
    }

    private List<Integer> getNumbers(String s) {
        ArrayList<Integer> ret = new ArrayList<>();
        Pattern p = Pattern.compile("\\s\\d+\\s");
        Matcher m = p.matcher(s);
        while (m.find()) {
            ret.add(Integer.valueOf(m.group().trim()));
        }
        return ret;
    }

    private static int getPrice(String s) {
        Pattern p = Pattern.compile("\\s[0-9]+\\s");
        Matcher m = p.matcher(s.toLowerCase());
        if (m.find()) {
            return Integer.valueOf(m.group().trim());
        }
        return 0;
    }

    private void sendMessage(Channel channel, String message) {
        if (channel != null) {
            channel.send().message(message);
        }
    }

    private boolean checkStart(String s) {
        Pattern pattern = Pattern.compile(raffleStartRegex);
        Matcher m = pattern.matcher(s);
        Pattern pattern2 = Pattern.compile(raffleStartRegex2);
        Matcher m2 = pattern2.matcher(s);
        return m.find() || m2.find();
    }

    private boolean checkWon(String s, String username) {
        Pattern pattern = Pattern.compile(String.format(raffleResultRegex, username).toLowerCase());
        Matcher m = pattern.matcher(s.toLowerCase());
        Pattern pattern2 = Pattern.compile(String.format(raffleResultRegex2, username).toLowerCase());
        Matcher m2 = pattern2.matcher(s.toLowerCase());
        return m.find() || m2.find();
    }

    @Override
    public void onConnect(ConnectEvent event) throws Exception {
        controller.setConnectionStatus(true);
    }

    @Override
    public void onDisconnect(DisconnectEvent event) throws Exception {
        controller.setConnectionStatus(false);
    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        if (event.getChannel().getName().equalsIgnoreCase("#forsenlol") && event.getUser().getNick().equalsIgnoreCase(config.botName)) {
            controller.setForsenJoined(true);
        } else if (event.getChannel().getName().equalsIgnoreCase("#pajlada") && event.getUser().getNick().equalsIgnoreCase(config.botName)) {
            controller.setPajladaJoined(true);
        }
    }

    @Override
    public void onPart(PartEvent event) throws Exception {
        if (event.getChannel().getName().equalsIgnoreCase("#forsenlol") && event.getUser().getNick().equalsIgnoreCase(config.botName)) {
            controller.setForsenJoined(false);
        } else if (event.getChannel().getName().equalsIgnoreCase("#pajlada") && event.getUser().getNick().equalsIgnoreCase(config.botName)) {
            controller.setPajladaJoined(false);
        }
    }
}
