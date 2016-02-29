package no.sebmik;

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
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TwitchListener extends ListenerAdapter {
    private final Logger log = LoggerFactory.getLogger(RaffleBot.class);
    private static final String urlRegex = "\\(?(?:(http|https):\\/\\/)?(?:((?:[^\\W\\s]|\\.|-|[:]{1})+)@{1})?((?:www.)?(?:[^\\W\\s]|\\.|-)+[\\.][^\\W\\s]{2,4}|localhost(?=\\/)|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(?::(\\d*))?([\\/]?[^\\s\\?]*[\\/]{1})*(?:\\/?([^\\s\\n\\?\\[\\]\\{\\}\\#]*(?:(?=\\.)){1}|[^\\s\\n\\?\\[\\]\\{\\}\\.\\#]*)?([\\.]{1}[^\\s\\?\\#]*)?)?(?:\\?{1}([^\\s\\n\\#\\[\\]]*))?([\\#][^\\s\\n]*)?\\)?";
    private static final String raffleStartRegex = "^A multi-raffle has begun, [0-9]+ points will be split among the winners. type !join to join the raffle! The raffle will end in [0-9]+ seconds";
    private static final String raffleStartRegex2 = "^A raffle has begun for [0-9]+ points. type !join to join the raffle! The raffle will end in [0-9]+ seconds";
    private final String raffleResultRegex = "^The raffle has finished!.+\\b%s\\b,.+won [0-9]+ points! PogChamp";
    private final String raffleResultRegex2 = "^(.+)?\\b%s\\b(.+)?won [0-9]+ points each!";
    private final String rouletteWonRegex = "^%s won [0-9]+ points in roulette and now has [0-9]+ points!";
    private final String rouletteLostRegex = "^%s lost [0-9]+ points in roulette and now has [0-9]+ points!";
    private final String URL = "irc.twitch.tv";
    private final int PORT = 6667;
    private PircBotX bot;
    private final Controller controller;
    private ArrayList<Long> lastUsed;
    private History history;
    private ArrayList<String> shownLinks;

    public TwitchListener(Controller controller) {
        this.controller = controller;
    }

    public void init(Config config) {
        lastUsed = new ArrayList<>();
        shownLinks = new ArrayList<>();
        history = new History(20);
        Configuration configuration = new Configuration.Builder()
                .setServerPassword(config.oauth)
                .setOnJoinWhoEnabled(false)
                .setCapEnabled(true)
                .addCapHandler(new EnableCapHandler("twitch.tv/membership"))
                .addCapHandler(new EnableCapHandler("twitch.tv/commands"))
                .addCapHandler(new EnableCapHandler("twitch.tv/tags"))
                .setName(config.name)
                .addServer(URL, PORT)
                .setAutoNickChange(false)
                .setMessageDelay(0)
                .addListener(this)
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
    public void onMessage(MessageEvent event) throws Exception {
        if (event.getMessage().toLowerCase().contains(controller.config.name.toLowerCase())) {
            controller.addTextToMain(String.format("%s: %s", event.getUser().getNick(), event.getMessage()), event.getChannel().getName());
        }
        if (controller.config.showLinks) {
            List<String> urls = findUrls(event.getMessage());
            urls.stream().filter(url -> !shownLinks.contains(url)).forEach(url -> {
                controller.addURL(url, event.getChannel().getName(), event.getUser().getNick());
                shownLinks.add(url);
            });
        }
    }

    @Override
    public void onAction(ActionEvent event) {
        String channelName = event.getChannel().getName();
        User user = event.getUser();
        String username = user.getNick();
        String message = event.getMessage();
        if (username.equalsIgnoreCase("snusbot") || username.equalsIgnoreCase("pajbot")) {
            if (checkStart(message)) {
                List<Integer> num = getNumbers(message);
                if (controller.config.autoJoinRaffle) {
                    sendMessage(event.getChannel(), getJoinString());
                    if (message.contains("multi")) {
                        new Raffle(true, num.get(0), num.get(1), channelName, controller).start();
                        controller.addTextToMain(String.format("Joining multi-raffle for %s points. The raffle ends in %s seconds.", num.get(0), num.get(1)), channelName);
                    } else {
                        new Raffle(false, num.get(0), num.get(1), channelName, controller).start();
                        controller.addTextToMain(String.format("Joining raffle for %s points. The raffle ends in %s seconds.", num.get(0), num.get(1)), channelName);
                    }
                }
            }
            if (checkWon(message, controller.config.name)) {
                int price = getPrice(message);
                controller.addTextToMain(String.format("Congratulations, you won %s points in a raffle.", price), channelName);
                if (controller.config.autoRoulette) {
                    sendMessage(event.getChannel(), String.format("!roulette %s", price));
                }
                String winMessage = controller.config.winMessage;
                if (winMessage != null && winMessage.length() > 0) {
                    sendMessage(event.getChannel(), winMessage);
                }
            }
            checkRoulette(message, controller.config.name, channelName);
        }
    }

    private static List<String> findUrls(String s) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile(urlRegex).matcher(s);
        while (m.find()) {
            String u = m.group();
            if (!allMatches.contains(u)) {
                allMatches.add(u);
            }
        }
        return allMatches;
    }

    private void checkRoulette(String s, String username, String channelName) {
        Pattern p = Pattern.compile(String.format(rouletteWonRegex, username.toLowerCase()));
        Matcher m = p.matcher(s.toLowerCase());
        Pattern p2 = Pattern.compile(String.format(rouletteLostRegex, username.toLowerCase()));
        Matcher m2 = p2.matcher(s.toLowerCase());
        List<Integer> num = getNumbers(s);
        if (m.find()) {
            controller.addTextToMain(String.format("You won %s points in roulette. You have %s points FeelsGoodMan", num.get(0), num.get(1)), channelName);
        } else if (m2.find()) {
            controller.addTextToMain(String.format("You lost %s points in roulette. You have %s points FeelsBadMan", num.get(0), num.get(1)), channelName);
        }
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
            long toWait = history.getTimeToWait();
            if (toWait > 0) {
                Timer timer = new Timer();
                TimerTask action = new TimerTask() {
                    public void run() {
                        channel.send().message(message);
                    }
                };
                timer.schedule(action, toWait);
            } else {
                channel.send().message(message);
            }
        }
    }

    private static boolean checkStart(String s) {
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
    public void onConnectAttemptFailed(ConnectAttemptFailedEvent event) throws Exception {
        controller.addTextToMain("Failed to authenticate, probably wrong username/token.", null);
    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        log.info(event.toString());
        if (event.getChannel().getName().equalsIgnoreCase("#forsenlol") && event.getUser().getNick().equalsIgnoreCase(controller.config.name)) {
            controller.setForsenStatus(true);
        } else if (event.getChannel().getName().equalsIgnoreCase("#pajlada") && event.getUser().getNick().equalsIgnoreCase(controller.config.name)) {
            controller.setPajladaStatus(true);
        }
    }

    @Override
    public void onPart(PartEvent event) throws Exception {
        if (event.getChannel().getName().equalsIgnoreCase("#forsenlol") && event.getUser().getNick().equalsIgnoreCase(controller.config.name)) {
            controller.setForsenStatus(false);
        } else if (event.getChannel().getName().equalsIgnoreCase("#pajlada") && event.getUser().getNick().equalsIgnoreCase(controller.config.name)) {
            controller.setPajladaStatus(false);
        }
    }

    private String getJoinString() {
        int c = getNextNotUsed();
        String toSend = comb("join", c + 1);
        lastUsed.add(c, System.currentTimeMillis());
        return "!" + toSend;
    }

    private int getNextNotUsed() {
        int c = 0;
        for (Long l : lastUsed) {
            if (l == 0L || System.currentTimeMillis() - l > 30000) {
                return c;
            }
            c++;
        }
        return c;
    }

    private static String comb(String word, int c) {
        if (c > 32) {
            c = 32;
        }
        word = word.toLowerCase();
        String[] res = new String[c];
        for (int i = 0; i < c; i++) {
            char[] result = word.toCharArray();
            for (int j = 0; j < word.length(); j++) {
                if (((i >> j) & 1) == 1) {
                    result[j] = Character.toUpperCase(word.charAt(j));
                }
            }
            res[i] = new String(result);
        }
        return res[c - 1];
    }

    private int getAccess(String username, String channelName, int sub, boolean mod) {
        int access = 1;
        if (username != null
                && username.equalsIgnoreCase(channelName)) {
            access = 4;
        } else if (mod) {
            access = 3;
        } else if (sub == 1) {
            access = 2;
        }
        return access;
    }

    public static void main(String[] args) {
        System.out.println(checkStart("A multi-raffle has begun, 20000 points will be split among the winners. type !join to join the raffle! The raffle will end in 120 seconds"));
    }

    public History getHistory() {
        return history;
    }

    public PircBotX getBot() {
        return bot;
    }
}
