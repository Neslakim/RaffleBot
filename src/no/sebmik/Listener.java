package no.sebmik;

import org.pircbotx.*;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listener extends ListenerAdapter {
    Log log = new Log(getClass().getSimpleName());
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
    private static String[] GROUP_SERVERS = {
            "192.16.64.212",
            "192.16.64.180",
            "199.9.253.119",
            "199.9.253.120"
    };
    //    private final String urlRegex2= "(?i)\\b((?:https?:(?:/{1,3}|[a-z0-9%])|[a-z0-9.\\-]+[.](?:com|net|org|edu|gov|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|post|pro|tel|travel|xxx|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|Ja|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw)/)(?:[^\\s()<>{}\\[\\]]+|\\([^\\s()]*?\\([^\\s()]+\\)[^\\s()]*?\\)|\\([^\\s]+?\\))+(?:\\([^\\s()]*?\\([^\\s()]+\\)[^\\s()]*?\\)|\\([^\\s]+?\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’])|(?:(?<!@)[a-z0-9]+(?:[.\\-][a-z0-9]+)*[.](?:com|net|org|edu|gov|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|post|pro|tel|travel|xxx|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|Ja|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw)\\b/?(?!@)))\n";
    private final String whisperRegex = "^(:\\S+[^!@\\s]+![^@\\s]+@\\S+) WHISPER \\S+ (:.*)$";
    private final String raffleStartRegex = "^A multi-raffle has begun, [0-9]+ points will be split among the winners. type !join to join the raffle! The raffle will end in [0-9]+ seconds";
    private final String raffleStartRegex2 = "^A raffle has begun for [0-9]+ points. type !join to join the raffle! The raffle will end in [0-9]+ seconds";
    private final String raffleResultRegex = "^The raffle has finished!.+\\b%s\\b.+won [0-9]+ points! PogChamp";
    private final String raffleResultRegex2 = "^(.+)?\\b%s\\b(.+)?won [0-9]+ points each!";
    private final String rouletteRegex = "^%s (won|lost) [0-9]+ points in roulette";
    private final String hsBetStart = "A new game has begun! Vote with !hsbet win/lose POINTS";
    private final String hsBetEnd = "The hearthstone betting has been closed! No longer accepting bets.";
    private final String URL = "irc.twitch.tv";
    private final int PORT = 6667;
    static MultiBotManager manager;
    private PircBotX main;
    static PircBotX whisper;
    private final Controller controller;
    private ArrayList<Long> lastUsed;
    private History history;
    private ArrayList<String> shownLinks;
    private HashSet<String> shownMessages;
    private HashSet<String> botNames;
    private String color = "#6441A5";
    private boolean mod;
    private boolean sub;
    private Bet bet;

    public Listener(Controller controller) {
        this.controller = controller;
    }

    public void init(Config config) {
        botNames = new HashSet<>();
        // TODO temporary
        botNames.add("snusbot");
        botNames.add("pajbot");
        lastUsed = new ArrayList<>();
        shownLinks = new ArrayList<>();
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
        main = new PircBotX(configuration.buildForServer(URL, PORT));
        whisper = new PircBotX(configuration.buildForServer(GROUP_SERVERS[2], PORT));
        manager = new MultiBotManager();
        manager.addBot(main);
        manager.addBot(whisper);
    }

    public void start() {
        new Thread(() -> {
            manager.start();
        }).start();
    }

    public void stop() {
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
        mentions(username, message, mod, sub, color);
        emotes(event.getTags(), message);
        message(username, color, message, mod, sub);
        bet(message);
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
            raffle(event, message);
            raffleWon(event, message);
            checkRoulette(displayName, message, mod, sub, color);
            checkBet(username, message, mod, sub, color);
            checkDuel(username, message, color, mod, sub);
        }
        if (!mentions(username, message, mod, sub, color)) {
            message(username, color, message, mod, sub);
        }
        emotes(event.getTags(), message);
    }

    public void message(String username, String color, String message, boolean mod, boolean sub) {
        if (controller.config.allMessages) {
            if (!filter(message)) {
                controller.printMessage(username, "", message, botNames.contains(username.toLowerCase()), mod, sub, color);
                shownMessages.add(message);
            }
        }
    }

    private boolean filter(String message) {
        return controller.config.filter && (shownMessages.contains(message) || emoteCount(message) + bttvEmoteCount(message) > 9 ||
                (message.length() > 250 && getAsciiRatio(message) > 0.8) || getAsciiRatio(message) > 0.9);
    }

    private void raffleWon(ActionEvent event, String message) {
        if (checkWon(message, controller.config.name)) {
            int price = getPrice(message);
            if (!controller.config.mentions) {
                controller.printMessage("", "", String.format("Congratulations, you won %s points in a raffle. PogChamp", price), false, false, false, color);
            }
            if (controller.config.autoRoulette) {
                sendMessage(event.getChannel(), String.format("!roulette %s", price));
            }
        }
    }

    private void raffle(ActionEvent event, String message) {
        if (checkStart(message)) {
            List<Integer> num = getNumbersWhitespace(message);
            if (controller.config.autoJoinRaffle && num.get(0) > 0) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendMessage(event.getChannel(), getJoinString());
                        if (message.contains("multi")) {
                            new Raffle(true, num.get(0), num.get(1), controller).start();
                            controller.printMessage("", "", String.format("Joining multi-raffle for %s points. The raffle ends in %s seconds.", num.get(0), num.get(1)), false, false, false, color);
                        } else {
                            new Raffle(false, num.get(0), num.get(1), controller).start();
                            controller.printMessage("", "", String.format("Joining raffle for %s points. The raffle ends in %s seconds.", num.get(0), num.get(1)), false, false, false, color);
                        }
                    }
                }, Math.min((num.get(1) * 1000) / 3, 19000));
            }
        }
    }

    private boolean mentions(String username, String message, boolean mod, boolean sub, String color) {
        if (controller.config.mentions && message.toLowerCase().contains(controller.config.name.toLowerCase())) {
            controller.printMessage(username, "", message, botNames.contains(username.toLowerCase()), mod, sub, color);
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

    private void checkBet(String username, String message, boolean mod, boolean sub, String color) {
        if (message.contains(hsBetStart)) {
            controller.printMessage(username, "", message, botNames.contains(username.toLowerCase()), mod, sub, color);
            bet = new Bet();
            bet.start();
            if (controller.config.betThreshold > 0 && controller.config.betChannels.toLowerCase().contains(controller.channelName.toLowerCase())) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (bet.winRate() > controller.config.betThreshold) {
                            sendMessage(String.format("!hsbet lose %s", controller.config.betAmount));
                        } else if (bet.loseRate() > controller.config.betThreshold) {
                            sendMessage(String.format("!hsbet win %s", controller.config.betAmount));
                        }
                    }
                }, 57000);
            }
        } else if (message.contains(hsBetEnd)) {
            controller.printMessage(username, "", message, botNames.contains(username.toLowerCase()), mod, sub, color);
            log.d(bet.toString(), false);
            log.d(String.format("Expected winnings: %s points", (int) (controller.config.betAmount / (Math.min(bet.winRate(), bet.loseRate()) + controller.config.betAmount))
                    * Math.max(bet.winRate(), bet.loseRate())), false);
            bet.stop();
        }
    }

    private void bet(String message) {
        Pattern p = Pattern.compile("^!hsbet (win|winner)");
        Matcher m = p.matcher(message.toLowerCase());
        Pattern p2 = Pattern.compile("^!hsbet (lose|loss|loser|loose)");
        Matcher m2 = p2.matcher(message.toLowerCase());
        if (bet != null && bet.running && m.find()) {
            List<Integer> num = getNumbers(message);
            bet.win(num.get(0));
            log.r(bet.toString());
        } else if (bet != null && bet.running && m2.find()) {
            List<Integer> num = getNumbers(message);
            bet.lose(num.get(0));
            log.r(bet.toString());
        }
    }

    private void checkDuel(String sender, String message, String color, boolean mod, boolean sub) {
        if (message.toLowerCase().contains(String.format("%s won the duel vs", controller.config.name).toLowerCase()) ||
                message.toLowerCase().contains(String.format("won the duel vs forsenGun %s", controller.config.name).toLowerCase())) {
            controller.printMessage(sender, "", message, botNames.contains(sender.toLowerCase()), mod, sub, color);
        }
    }

    private void checkRoulette(String sender, String message, boolean mod, boolean sub, String color) {
        Pattern p = Pattern.compile(String.format(rouletteRegex, sender.toLowerCase()));
        Matcher m = p.matcher(message.toLowerCase());
        if (m.find()) {
            controller.printMessage(sender, "", message, botNames.contains(sender.toLowerCase()), mod, sub, color);
        }
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
                        channel.send().message(message);
                    }
                };
                timer.schedule(action, toWait);
            } else {
                channel.send().message(message);
            }
        }
    }

    private void whisper(String username, String message) {
        log.d(String.format("Whispering %s: %s", username, message), false);
        whisper.send().message(username, String.format("/w %s %s", username, message));
    }

    public void sendMessage(String message) {
        long toWait = history.getTimeToWait();
        if (toWait > 0) {
            Timer timer = new Timer();
            TimerTask action = new TimerTask() {
                public void run() {
                    controller.printMessage(controller.config.name, "", message, botNames.contains(controller.config.name.toLowerCase()), mod, sub, color);
                    main.send().message("#" + controller.channelName, message);
                }
            };
            timer.schedule(action, toWait);
        } else {
            controller.printMessage(controller.config.name, "", message, botNames.contains(controller.config.name.toLowerCase()), mod, sub, color);
            main.send().message("#" + controller.channelName, message);
        }
    }

    public void sendMessageWithoutWait(String message) {
        main.send().message("#" + controller.channelName, message);
    }

    private boolean checkStart(String s) {
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
        controller.printMessage("", "", "Failed to authenticate, probably wrong username/token.", false, false, false, controller.sysColor);
    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        if (event.getChannel().getName().equalsIgnoreCase("#" + controller.channelName) && event.getUser().getNick().equalsIgnoreCase(controller.config.name)) {
            controller.setChannelStatus(true);
        }
    }

    @Override
    public void onUnknown(UnknownEvent event) throws Exception {
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
            log.d(event.getTags().toString(), false);
            String message = event.getLine().substring(event.getLine().indexOf(":", 1) + 1);
            String c = String.valueOf(tags.get("color"));
            String color = c != null && c.length() == 7 ? c : "#008000";
            boolean mod = event.getTags().get("user-type").contains("mod");
            controller.printMessage(username, controller.config.name, message, botNames.contains(username.toLowerCase()), mod, false, color);
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

    public History getHistory() {
        return history;
    }

    public PircBotX getMain() {
        return main;
    }

    public void clearShownMessages() {
        this.shownMessages = new HashSet<>();
    }

    public String getColor() {
        return color;
    }
}
