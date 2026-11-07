package de.waterdu.atlantis.util.java;

public class TimeUtils {

    private TimeUtils() {
        ExceptionUtils.throwCannotInstantiateClassException();
    }

    public static String[] getMMSSForTicks(long ticks) {
        return TimeUtils.getMMSSForMilliseconds(ticks * 50);
    }

    public static String[] getMMSSForMilliseconds(long milliseconds) {
        var secondsLeft = milliseconds / 1000L;
        var minutesLeft = secondsLeft / 60L;
        secondsLeft = secondsLeft - (minutesLeft * 60);
        return new String[]{String.valueOf(minutesLeft), (secondsLeft < 10 ? "0" : "") + secondsLeft, String.valueOf(secondsLeft)};
    }
}
