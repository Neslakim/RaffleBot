package no.sebmik;

import org.pircbotx.*;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listener extends ListenerAdapter {
    private final Log log = new Log(getClass().getSimpleName());
    private final String[] EVENT_SERVERS = {
            "199.9.255.149:443",
            "192.16.64.181:443",
            "199.9.255.146:443",
            "192.16.64.182:443",
            "192.16.64.214:443",
            "199.9.255.148:443",
            "192.16.64.213:443",
            "199.9.255.147:443",
            "192.16.64.173:443"
    };
    private static final String[] GROUP_SERVERS = {
            "192.16.64.212",
            "192.16.64.180",
            "199.9.253.119",
            "199.9.253.120"
    };
    private final String whisperRegex = "^(:\\S+[^!@\\s]+![^@\\s]+@\\S+) WHISPER \\S+ (:.*)$";
    private final String raffleStartRegex = "^A multi-raffle has begun, [0-9]+ points will be split among the winners. type !join to join the raffle! The raffle will end in [0-9]+ seconds";
    private final String raffleStartRegex2 = "^A raffle has begun for [0-9]+ points. type !join to join the raffle! The raffle will end in [0-9]+ seconds";
    private final String raffleResultRegex = "^The raffle has finished!.+\\b%s\\b.+won [0-9]+ points! PogChamp";
    private final String raffleResultRegex2 = "^(.+)?\\b%s\\b(.+)?won [0-9]+ points each!";
    private final String twitchOnlyBingo = "^A bingo has started! Guess the right target to win [0-9]+ points! Only one target per message! Use TWITCH global emotes.";
    private final String bttvOnlyBingo = "^A bingo has started! Guess the right target to win [0-9]+ points! Only one target per message! Use BTTV global emotes.";
    private final String twitchAndBttvBingo = "^A bingo has started! Guess the right target to win [0-9]+ points! Only one target per message! Use BTTV and TWITCH global emotes.";
    private final String rouletteRegex = "^%s (won|lost) [0-9]+ points in roulette";
    private final String hsBetStart = "A new game has begun! Vote with !hsbet win/lose POINTS";
    private final String hsBetEnd = "The hearthstone betting has been closed! No longer accepting bets.";
    private final int PORT = 6667;
    private static MultiBotManager manager;
    private PircBotX main;
    private static PircBotX whisper;
    private final Controller controller;
    private ArrayList<Long> lastUsed;
    private History history;
    private HashSet<String> shownMessages;
    private HashSet<String> botNames;
    private String color = "#6441A5";
    private boolean mod;
    private boolean sub;
    private Bet bet;
    private Bingo bingo;
    private Random r;
    private boolean subMode;

    public Listener(Controller controller) {
        this.controller = controller;
    }

    void init(Config config) {
        r = new Random();
        botNames = new HashSet<>();
        // TODO temporary
        botNames.add("snusbot");
        botNames.add("pajbot");
        lastUsed = new ArrayList<>();
        shownMessages = new HashSet<>();
        Configuration.Builder configuration = new Configuration.Builder()
                .setServerPassword(config.oauth)
                .setOnJoinWhoEnabled(false)
                .setCapEnabled(true)
                .addCapHandler(new EnableCapHandler("twitch.tv/membership"))
                .addCapHandler(new EnableCapHandler("twitch.tv/commands"))
                .addCapHandler(new EnableCapHandler("twitch.tv/tags"))
                .setName(config.name)
                .setAutoNickChange(false)
                .setMessageDelay(0)
                .addListener(this)
                .setAutoReconnect(true)
                .setAutoReconnectAttempts(99999)
                .setAutoReconnectDelay(1000);
        Configuration.Builder configurationWithAutoJoin = new Configuration.Builder(configuration)
                .addAutoJoinChannel("#" + controller.channelName);
        String[] split = new Downloader().getServers(controller.channelName)[0].split(":");
        main = new PircBotX(configurationWithAutoJoin.buildForServer(split[0], Integer.parseInt(split[1])));
        whisper = new PircBotX(configuration.buildForServer(GROUP_SERVERS[2], PORT));
        manager = new MultiBotManager();
        manager.addBot(main);
        manager.addBot(whisper);
    }

    void start() {
        new Thread(() -> {
            manager.start();
        }).start();
    }

    void stop() {
        main.stopBotReconnect();
        whisper.stopBotReconnect();
        manager.stop();
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        User user = event.getUser();
        String color = event.getTags().get("color");
        boolean sub = event.getTags().get("subscriber").equals("1");
        boolean mod = event.getTags().get("user-type").contains("mod");
        String displayName = event.getTags().get("display-name");
        String username;
        if (displayName == null || displayName.length() == 0) {
            username = user != null ? user.getNick() : null;
        } else {
            username = displayName;
        }
        String message = event.getMessage();
        String[] split = message.toLowerCase().split(" ");
        mentions(username, message, color, mod, sub);
        emotes(event.getTags(), message);
        message(username, message, color, mod, sub);
        if (split[0].equalsIgnoreCase("!hsbet")) {
            log.d(message, false);
            bet(split);
        }
    }

    @Override
    public void onAction(ActionEvent event) {
        User user = event.getUser();
        String displayName = event.getTags().get("display-name");
        String username;
        if (displayName == null || displayName.length() == 0) {
            username = user != null ? user.getNick() : null;
        } else {
            username = displayName;
        }
        boolean sub = event.getTags().get("subscriber").equals("1");
        boolean mod = event.getTags().get("user-type").contains("mod");
        String message = event.getMessage();
        String color = event.getTags().get("color");
        if (username != null && botNames.contains(username.toLowerCase())) {
            // TODO return boolean and do if/else
            boolean done;
            try {
                done = raffle(event.getChannel(), username, message, color, mod, sub);
                if (!done)
                    done = raffleWon(event, message);
                if (!done)
                    done = checkRoulette(username, message, color, sub, mod);
                if (!done)
                    done = checkBet(username, message, color, mod, sub);
                if (!done)
                    done = checkDuel(username, message, color, mod, sub);
                if (!done)
                    checkBingo(username, message, color, mod, sub);
            } catch (Exception e) {
                log.e(e, true);
            }
        }
        if (!mentions(username, message, color, mod, sub)) {
            message(username, message, color, mod, sub);
        }
        emotes(event.getTags(), message);
    }

    private void message(String username, String message, String color, boolean mod, boolean sub) {
        if (controller.config.allMessages) {
            if (!filter(message)) {
                controller.printMessage(username, "", message, color, mod, sub, botNames.contains(username.toLowerCase()));
                shownMessages.add(message);
            }
        }
    }

    private boolean filter(String message) {
        return controller.config.filter && (shownMessages.contains(message) || emoteCount(message) + bttvEmoteCount(message) > 9 ||
                (message.length() > 250 && getAsciiRatio(message) > 0.8) || getAsciiRatio(message) > 0.9);
    }

    private boolean raffleWon(ActionEvent event, String message) {
        if (checkWon(message, controller.config.name)) {
            int price = getPrice(message);
            if (!controller.config.mentions) {
                controller.printMessage("", "", String.format("Congratulations, you won %s points in a raffle. PogChamp", price), color, false, false, false);
            }
            if (controller.config.autoRoulette) {
                sendMessage(event.getChannel(), String.format("!roulette %s", price));
            }
            return true;
        }
        return false;
    }

    private boolean raffle(Channel channel, String username, String message, String color, boolean mod, boolean sub) {
        if (checkStart(message)) {
            List<Integer> num = getNumbersWhitespace(message);
            if (controller.config.autoJoinRaffle && num.get(0) > 0) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendMessage(channel, getJoinString());
                    }
                }, (r.nextInt(num.get(1) / 2) + (num.get(1) / 4)) * 1000);
            }
            new Raffle(true, num.get(0), num.get(1), controller).start();
            controller.printMessage(username, "", message, color, mod, sub, botNames.contains(username.toLowerCase()));
            return true;
        }
        return false;
    }

    private boolean mentions(String username, String message, String color, boolean mod, boolean sub) {
        Pattern p = Pattern.compile(String.format(controller.usernameRegex, controller.config.name));
        Matcher m = p.matcher(message);
        if (controller.config.mentions && m.find()) {
            controller.printMessage(username, "", message, color, mod, sub, botNames.contains(username.toLowerCase()));
            return true;
        }
        return false;
    }

    private void emotes(Map<String, String> tags, String message) {
        if (controller.overlay) {
            try {
                String emotes = tags.get("emotes");
                if (emotes != null && emotes.length() > 0) {
                    controller.addEmotes(getEmotes(emotes, message));
                }
                if (controller.bttvOverlay) {
                    controller.addBTTVEmotes(message);
                }
            } catch (Exception e) {
                log.e(e, true);
            }
        }
    }

    private List<String> getEmotes(String emotes, String message) {
        ArrayList<String> ret = new ArrayList<>();
        String[] split = emotes.split("/");
        for (String emote : split) {
            String[] a = emote.split(":")[1].split(",")[0].split("-");
            ret.add(message.substring(Integer.parseInt(a[0]), Integer.parseInt(a[1]) + 1));
        }
        return ret;
    }

    private void checkBingo(String username, String message, String color, boolean mod, boolean sub) {
        Pattern pattern = Pattern.compile(twitchOnlyBingo);
        Matcher m = pattern.matcher(message);
        // TODO only global
        if (m.find()) {
            bingo = new Bingo(controller.emotes, this);
            bingo.start();
            controller.printMessage(username, "", message, color, mod, sub, botNames.contains(username.toLowerCase()));
            return;
        }

        Pattern pattern2 = Pattern.compile(twitchAndBttvBingo);
        Matcher m2 = pattern2.matcher(message);
        if (m2.find()) {
            List<String> emotes = new ArrayList<>(controller.emotes);
            controller.bttvEmotes.entrySet().stream().filter(e -> message.contains((String) e.getKey())).forEach(e -> emotes.add(e.getKey()));
            bingo = new Bingo(emotes, this);
            bingo.start();
            controller.printMessage(username, "", message, color, mod, sub, botNames.contains(username.toLowerCase()));
            return;
        }

        Pattern pattern3 = Pattern.compile(bttvOnlyBingo);
        Matcher m3 = pattern3.matcher(message);
        if (m3.find()) {
            List<String> emotes = new ArrayList<>(controller.bttvEmotes.size());
            controller.bttvEmotes.entrySet().stream().filter(e -> message.contains((String) e.getKey())).forEach(e -> emotes.add(e.getKey()));
            bingo = new Bingo(emotes, this);
            bingo.start();
            controller.printMessage(username, "", message, color, mod, sub, botNames.contains(username.toLowerCase()));
            return;
        }

        if (message.toLowerCase().contains("bingo cancelled by") || message.toLowerCase().contains("won the bingo!")) {
            log.d("bingo done", false);
            bingo.stop();
            controller.printMessage(username, "", message, color, mod, sub, botNames.contains(username.toLowerCase()));
        }
    }

    private boolean checkBet(String username, String message, String color, boolean mod, boolean sub) {
        if (message.contains(hsBetStart)) {
            controller.printMessage(username, "", message, color, mod, sub, botNames.contains(username.toLowerCase()));
            bet = new Bet();
            bet.start();
            if (controller.config.betThreshold > 0 && controller.config.betChannels.toLowerCase().contains(controller.channelName.toLowerCase())) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (bet.getTotalWinPoints() > controller.config.betAmount * 2 && bet.winRate() > controller.config.betThreshold) {
                            int amount = bet.getTotalLosePoints() == 0 ? 1 : controller.config.betAmount;
                            if (subMode) {
                                whisper("snusbot", String.format("!hsbet lose %s", amount));
                            } else {
                                sendMessage(String.format("!hsbet lose %s", amount));
                            }
                            bet.lose(amount);
                        } else if (bet.getTotalLosePoints() > controller.config.betAmount * 2 && bet.loseRate() > controller.config.betThreshold) {
                            int amount = bet.getTotalWinPoints() == 0 ? 1 : controller.config.betAmount;
                            if (subMode) {
                                whisper("snusbot", String.format("!hsbet win %s", amount));
                            } else {
                                sendMessage(String.format("!hsbet win %s", amount));
                            }
                            bet.win(amount);
                        }
                    }
                }, 57000);
                return true;
            }
        } else if (message.contains(hsBetEnd)) {
            controller.printMessage(username, "", message, color, mod, sub, botNames.contains(username.toLowerCase()));
            log.d(bet.toString(), false);
            int winnings = (int) (bet.getTotalWinPoints() >= bet.getTotalLosePoints() ?
                    (controller.config.betAmount * (bet.getTotalWinPoints() / bet.getTotalLosePoints())) :
                    (controller.config.betAmount * (bet.getTotalLosePoints() / bet.getTotalWinPoints())));
            log.d(String.format("(Roughly) estimated winnings: %d points", winnings), false);
            bet.stop();
            return true;
        }
        return false;
    }

    private void bet(String[] message) {
        if (bet != null && bet.running && message.length >= 3) {
            if (message[1].equalsIgnoreCase("win") || message[1].equalsIgnoreCase("winner")) {
                List<Integer> num = getNumbers(message[2]);
                if (num.get(0) > 0) {
                    bet.win(num.get(0));
                }
//                log.r(bet.toString());
            } else if (message[1].equalsIgnoreCase("lose") || message[1].equalsIgnoreCase("loss")
                    || message[1].equalsIgnoreCase("loser") || message[1].equalsIgnoreCase("loose")) {
                List<Integer> num = getNumbers(message[2]);
                if (num.get(0) > 0) {
                    bet.lose(num.get(0));
                }
//                log.r(bet.toString());
            }
        }
    }

    private boolean checkDuel(String sender, String message, String color, boolean mod, boolean sub) {
        if (message.toLowerCase().contains(String.format("%s won the duel vs", controller.config.name).toLowerCase()) ||
                message.toLowerCase().contains(String.format("won the duel vs forsenGun %s", controller.config.name).toLowerCase())) {
            controller.printMessage(sender, "", message, color, mod, sub, botNames.contains(sender.toLowerCase()));
            return true;
        }
        return false;
    }

    private boolean checkRoulette(String sender, String message, String color, boolean sub, boolean mod) {
        Pattern p = Pattern.compile(String.format(rouletteRegex, sender.toLowerCase()));
        Matcher m = p.matcher(message.toLowerCase());
        if (m.find()) {
            controller.printMessage(sender, "", message, color, mod, sub, botNames.contains(sender.toLowerCase()));
            return true;
        }
        return false;
    }

    private List<Integer> getNumbersWhitespace(String s) {
        ArrayList<Integer> ret = new ArrayList<>();
        Pattern p = Pattern.compile("\\s\\d+\\s");
        Matcher m = p.matcher(s);
        while (m.find()) {
            ret.add(Integer.valueOf(m.group().trim()));
        }
        return ret;
    }

    private List<Integer> getNumbers(String s) {
        ArrayList<Integer> ret = new ArrayList<>();
        Pattern p = Pattern.compile("\\d+");
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
                        controller.printMessage(controller.config.name, "", message, color, mod, sub, botNames.contains(controller.config.name.toLowerCase()));
                        channel.send().message(message);
                    }
                };
                timer.schedule(action, toWait);
            } else {
                controller.printMessage(controller.config.name, "", message, color, mod, sub, botNames.contains(controller.config.name.toLowerCase()));
                channel.send().message(message);
            }
        }
    }

    private void whisper(String username, String message) {
        log.d(String.format("Whispering %s: %s", username, message), false);
        whisper.send().message(username, String.format("/w %s %s", username, message));
    }

    void sendMessage(String message) {
        long toWait = history.getTimeToWait();
        if (toWait > 0) {
            Timer timer = new Timer();
            TimerTask action = new TimerTask() {
                public void run() {
                    controller.printMessage(controller.config.name, "", message, color, mod, sub, botNames.contains(controller.config.name.toLowerCase()));
                    main.send().message("#" + controller.channelName, message);
                }
            };
            timer.schedule(action, toWait);
        } else {
            controller.printMessage(controller.config.name, "", message, color, mod, sub, botNames.contains(controller.config.name.toLowerCase()));
            main.send().message("#" + controller.channelName, message);
        }
    }

    void sendMessageWithoutWait(String message) {
        main.send().message("#" + controller.channelName, message);
    }

    private boolean checkStart(String s) {
        // todo change to contains?
        Pattern pattern = Pattern.compile(raffleStartRegex);
        Matcher m = pattern.matcher(s);
        Pattern pattern2 = Pattern.compile(raffleStartRegex2);
        Matcher m2 = pattern2.matcher(s);
        return m.find() || m2.find();
    }

    public boolean checkWon(String s, String username) {
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
        controller.printMessage("", "", "Failed to authenticate, probably wrong username/token.", controller.sysColor, false, false, false);
    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        if (event.getChannel().getName().equalsIgnoreCase("#" + controller.channelName) && event.getUser().getNick().equalsIgnoreCase(controller.config.name)) {
            controller.setChannelStatus(true);
        }
    }

    @Override
    public void onUnknown(UnknownEvent event) throws Exception {
//        log.d(event.toString(), false);
//        log.d(event.getTags().toString(), false);
        Map tags = event.getTags();
        String username = (String) tags.get("display-name");
        if (controller.config.name.equalsIgnoreCase(username)) {
            this.color = (String) tags.get("color");
            this.sub = event.getTags().get("subscriber").equals("1");
            this.mod = event.getTags().get("user-type").contains("mod");
            if (mod || username.equalsIgnoreCase(controller.channelName)) {
                history = new History(100);
            } else {
                history = new History(20);
            }
            return;
        }
        Pattern p = Pattern.compile(whisperRegex);
        Matcher m = p.matcher(event.getLine());
        if (m.find() && (controller.config.whispers || botNames.contains(username.toLowerCase()))) {
//            log.d(event.getTags().toString(), false);
            String message = event.getLine().substring(event.getLine().indexOf(":", 1) + 1);
            String c = String.valueOf(tags.get("color"));
            String color = c != null && c.length() == 7 ? c : "#008000";
            boolean mod = event.getTags().get("user-type").contains("mod");
            controller.printMessage(username, controller.config.name, message, color, mod, false, botNames.contains(username.toLowerCase()));
        }
        if (event.getLine().contains(":tmi.twitch.tv ROOMSTATE")) {
            if (tags.get("subs-only").equals("1")) {
                log.d("Sub only mode enabled", false);
                subMode = true;
            } else {
                log.d("Sub only mode disabled", false);
                subMode = false;
            }
        }
    }

    @Override
    public void onPart(PartEvent event) throws Exception {
        if (event.getChannel().getName().equalsIgnoreCase("#" + controller.channelName) && event.getUser().getNick().equalsIgnoreCase(controller.config.name)) {
            controller.setChannelStatus(false);
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

    private int emoteCount(String s) {
        int c = 0;
        String[] emotes = s.split("/");
        for (String emote : emotes) {
            String[] e = emote.split(",");
            c += e.length;
        }
        return c;
    }

    private int bttvEmoteCount(String msg) {
        String[] split = msg.split(" ");
        int c = 0;
        for (String s : Arrays.asList(split)) {
            if (controller.bttvEmotes.get(s) != null) {
                c++;
            }
        }
        return c;
    }

    private double getAsciiRatio(String msg) {
        int n = 0;
        for (char c : msg.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                n++;
            }
        }
        return n / msg.length();
    }

    History getHistory() {
        return history;
    }

    public PircBotX getMain() {
        return main;
    }

    void clearShownMessages() {
        this.shownMessages = new HashSet<>();
    }

    String getColor() {
        return color;
    }
}
