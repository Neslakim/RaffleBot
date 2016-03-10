package no.sebmik;

public class Bet {
    Log log = new Log(getClass().getSimpleName());
    private double win;
    private double lose;
    public boolean running;

    public Bet() {
        win = 0;
        lose = 0;
    }

    public void win(double w) {
        win += w;
    }

    public void lose(double l) {
        lose += l;
    }

    public String winRatio() {
        if (win == 0 && lose == 0) {
            return "0";
        }
        double percent = (win * 100) / (win + lose);
        return String.format("%.2f", percent);
    }

    public String loseRatio() {
        if (win == 0 && lose == 0) {
            return "0";
        }
        double percent = (lose * 100) / (win + lose);
        return String.format("%.2f", percent);
    }

    public double winRate() {
        if (win == 0 && lose == 0) {
            return 0;
        }
        return (win * 100) / (win + lose);
    }

    public double loseRate() {
        if (win == 0 && lose == 0) {
            return 0;
        }
        return (lose * 100) / (win + lose);
    }

    public void start() {
        log.d("Betting started", false);
        this.running = true;
    }

    public void stop() {
        log.d("Betting done", false);
        this.running = false;
    }

    public String toString() {
        return String.format("Win: %s (%s), Lose: %s (%s)", winRatio(), (int) win, loseRatio(), (int) lose);
    }
}
