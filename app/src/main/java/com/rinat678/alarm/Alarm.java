package com.rinat678.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.annotation.NonNull;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Alarm implements Serializable, Comparable<Alarm> {

    //    private static final long serialVersionUID = 8699489847426803789L;
    private int id;
    private boolean alarmActive = true;
    private Calendar alarmTime = Calendar.getInstance();
    private Day[] days = {Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY};
    private String alarmTonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
    private boolean vibrate = true;
    private String alarmName = "Hardcore Alarm";
    private Difficulty difficulty = Difficulty.EASY;

    @Override
    public int compareTo(@NonNull Alarm another) {
        if (this.getTimeToCompare() < another.getTimeToCompare())
            return -1;
        else if (this.getTimeToCompare() == another.getTimeToCompare())
            return 0;
        else return 1;
    }

    private int getTimeToCompare() {
        return Integer.parseInt(getAlarmTimeString().replace(':', '0'));
    }

    public boolean isAlarmActive() {
        return alarmActive;
    }

    public void setAlarmActive(boolean alarmActive) {
        this.alarmActive = alarmActive;
    }

    public Calendar getAlarmTime() {
        if (alarmTime.before(Calendar.getInstance()))
            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        while (!Arrays.asList(getDays()).contains(Day.values()[alarmTime.get(Calendar.DAY_OF_WEEK) - 1])) {  // day of week -1
            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        }
        return alarmTime;
    }

    public void setAlarmTime(String time) {
        String[] pieces = time.split(":");
        Calendar newAlarmTime = Calendar.getInstance();
        newAlarmTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(pieces[0]));
        newAlarmTime.set(Calendar.MINUTE, Integer.parseInt(pieces[1]));
        newAlarmTime.set(Calendar.SECOND, 0);
        this.alarmTime = newAlarmTime;
    }

    public String getAlarmTimeString() {
        DateFormat dateFormat = new SimpleDateFormat("H:mm", Locale.getDefault());
        return dateFormat.format(alarmTime.getTime());
    }

    public void setAlarmTime(Calendar alarmTime) {
        this.alarmTime = alarmTime;
    }

    public Day[] getDays() {
        return days;
    }

    public void setDays(Day[] days) {
        this.days = days;
    }

    public void addDay(Day day) {
        boolean contains = false;
        for (Day d : getDays())
            if (d.equals(day))
                contains = true;
        if (!contains) {
            List<Day> result = new LinkedList<>();
            Collections.addAll(result, getDays());
            result.add(day);
            setDays(result.toArray(new Day[result.size()]));
        }
    }

    public void removeDay(Day day) {

        List<Day> result = new LinkedList<>();
        for (Day d : getDays())
            if (!d.equals(day))
                result.add(d);
        setDays(result.toArray(new Day[result.size()]));
    }

    public String getAlarmTonePath() {
        return alarmTonePath;
    }

    public void setAlarmTonePath(String alarmTonePath) {
        this.alarmTonePath = alarmTonePath;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRepeatDaysString() {
        StringBuilder daysStringBuilder = new StringBuilder();
        Arrays.sort(days, new Comparator<Day>() {
            @Override
            public int compare(Day lhs, Day rhs) {

                return lhs.ordinal() - rhs.ordinal();
            }
        });
        if (getDays().length == Day.values().length) {
            daysStringBuilder.append("Ежедневно");
        } else if (days.length == 5 && days[0].equals(Day.MONDAY) && days[1].equals(Day.TUESDAY) &&  // 5 буднейй
                days[2].equals(Day.WEDNESDAY) && days[3].equals(Day.THURSDAY) &&
                days[4].equals(Day.FRIDAY)) {
            daysStringBuilder.append("По Будням");
        } else if (days.length == 2 && days[1].equals(Day.SATURDAY) && days[0].equals(Day.SUNDAY)) { // 2 выходней
            daysStringBuilder.append("По выходным");
        } else {
            for (Day d : getDays()) {
                daysStringBuilder.append(d.toString().substring(0, 3));
                daysStringBuilder.append(',');
            }
            daysStringBuilder.setLength(daysStringBuilder.length() - 1);
        }

        return daysStringBuilder.toString();
    }

    public void schedule(Context context) {
        setAlarmActive(true);

        Intent myIntent = new Intent(context, AlarmAlertBroadcastReceiver.class);
        myIntent.putExtra("alarm", this);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, getAlarmTime().getTimeInMillis(), pendingIntent);
    }

    public String getTimeUntilNextAlarmMessage() {
        long timeDifference = getAlarmTime().getTimeInMillis() - System.currentTimeMillis();
        long days = timeDifference / (1000 * 60 * 60 * 24);
        long hours = timeDifference / (1000 * 60 * 60) - (days * 24);
        long minutes = timeDifference / (1000 * 60) - (days * 24 * 60) - (hours * 60);
        long seconds = timeDifference / (1000) - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60);
        String alert = "Будильник прозвенит через ";
        String sDays;
        if (days % 10 > 4 || (days > 10 && days < 15))
            sDays = String.format(Locale.getDefault(), "%d дней", days);
        else if (days % 10 == 1) sDays = String.format(Locale.getDefault(), "%d день", days);
        else sDays = String.format(Locale.getDefault(), "%d дня", days);

        String sHours;
        if (hours % 10 > 4 || days % 10 == 0 || (hours > 10 && hours < 15))
            sHours = String.format(Locale.getDefault(), "%d часов", hours);
        else if (hours % 10 == 1) sHours = String.format(Locale.getDefault(), "%d час", hours);
        else sHours = String.format(Locale.getDefault(), "%d часа", hours);

        String sMinutes;
        if (minutes % 10 > 4 || minutes % 10 == 0 || (minutes > 10 && minutes < 15))
            sMinutes = String.format(Locale.getDefault(), "%d минут и", minutes);
        else if (minutes % 10 == 1)
            sMinutes = String.format(Locale.getDefault(), "%d минуту и", minutes);
        else sMinutes = String.format(Locale.getDefault(), "%d минуты и", minutes);

        String sSecs;
        if (seconds % 10 > 4 || seconds % 10 == 0 || (seconds > 10 && seconds < 15))
            sSecs = String.format(Locale.getDefault(), "%d секунд", seconds);
        else if (seconds % 10 == 1)
            sSecs = String.format(Locale.getDefault(), "%d секунду", seconds);
        else sSecs = String.format(Locale.getDefault(), "%d секунды", seconds);


        if (days > 0) alert += (sDays + " " + sHours + " " + sMinutes + " " + sSecs);
        else if (hours > 0) alert += (sHours + " " + sMinutes + " " + sSecs);
        else if (minutes > 0) alert += (sMinutes + " " + sSecs);
        else alert += (sSecs);
        return alert;
    }

    public enum Difficulty {
        EASY;

        @Override
        public String toString() {
            switch (this.ordinal()) {
                case 0:
                    return "Easy";
                case 1:
                    return "Medium";
                case 2:
                    return "Hard";
            }
            return super.toString();
        }
    }

    public enum Day {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY;

        @Override
        public String toString() {
            switch (this.ordinal()) {
                case 0:
                    return "Sunday";
                case 1:
                    return "Monday";
                case 2:
                    return "Tuesday";
                case 3:
                    return "Wednesday";
                case 4:
                    return "Thursday";
                case 5:
                    return "Friday";
                case 6:
                    return "Saturday";
            }
            return super.toString();
        }

    }
}
