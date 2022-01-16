package com.juaned;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.json.JSONArray;
import org.json.JSONObject;

public class TaskInfo {

  public static JSONArray taskInfoArray;
  public static String taskDay;

  public TaskInfo() {

    LocalDate today = LocalDate.now();
    String yesterday = (today.minusDays(1)).format(DateTimeFormatter.ISO_DATE);

    /* taskDay is a global variable string of the day we are evaluating */
    taskDay = yesterday;

    String command = String.format("/opt/homebrew/bin/timew export %s", yesterday);
    Process proc = null;
    try {
      proc = Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Read the output
    BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

    String line = "";
    String jsonString = "";
    try {
      while ((line = reader.readLine()) != null) {
        jsonString += line;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      proc.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // configure time formats and constants
    ZoneId central = ZoneId.of("America/Chicago");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    LocalDateTime midnightNext = today.atStartOfDay();
    LocalDateTime midnightBefore = (today.minus(1, ChronoUnit.DAYS)).atStartOfDay();
    ZonedDateTime midnightNextTZ = ZonedDateTime.of(midnightNext, central);
    ZonedDateTime midnightBeforeTZ = ZonedDateTime.of(midnightBefore, central);

    // turn to JSONObject
    taskInfoArray = new JSONArray(jsonString);

    for (int i = 0, size = taskInfoArray.length(); i < size; i++) {
      JSONObject objectInArray = taskInfoArray.getJSONObject(i);
      String startString = objectInArray.getString("start").replace("T", "").replace("Z", "");

      // convert to LocalDateTime
      LocalDateTime startdateTime = LocalDateTime.parse(startString, formatter);
      // convert to local time zone
      ZonedDateTime startTZ = startdateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(central);

      // convert to decimal form
      Double startTimeMinuteDecimal = startTZ.getMinute() / 60.0;
      Double startTimeHourDecimal = startTZ.getHour() + startTimeMinuteDecimal;

      // add a new entry that contains the start time represented in decimal
      objectInArray.put("startx", startTimeHourDecimal);

      // determine how many seconds in task spent
      String endString = objectInArray.getString("end").replace("T", "").replace("Z", "");
      // convert to LocalDateTime
      LocalDateTime enddateTime = LocalDateTime.parse(endString, formatter);
      // convert to local time zone
      ZonedDateTime endTZ = enddateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(central);
      Duration duration = null;
      long seconds = 0;
      if (endTZ.isAfter(midnightNextTZ)) {
        duration = Duration.between(midnightNextTZ, startTZ);
        seconds = Math.abs(duration.getSeconds());
      } else if (startTZ.isBefore(midnightBeforeTZ)) {
        duration = Duration.between(endTZ, midnightBeforeTZ);
        seconds = Math.abs(duration.getSeconds());

        // start time is midnight day before
        startTimeMinuteDecimal = midnightBeforeTZ.getMinute() / 60.0;
        startTimeHourDecimal = midnightBeforeTZ.getHour() + startTimeMinuteDecimal;
        // add a new entry that contains the start time represented in decimal
        objectInArray.put("startx", startTimeHourDecimal);
      } else {
        duration = Duration.between(endTZ, startTZ);
        seconds = Math.abs(duration.getSeconds());
      }
      // add a new entry that contains the duration of task in seconds
      objectInArray.put("seconds", seconds);

      // find the tag that will be displayed in pie chart key
      JSONArray tags = objectInArray.getJSONArray("tags");

      for (int ti = 0; ti < tags.length(); ti++) {
        String tag = tags.getString(ti);
        if (tag.equals(tag.toUpperCase()) && isNumeric(tag) != true) {
          // add a new entry that contains the duration of task in seconds
          objectInArray.put("displaytag", tag);
        }
      }
    }
  }

  public static boolean isNumeric(String strNum) {
    if (strNum == null) {
      return false;
    }
    try {
      double d = Double.parseDouble(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }
}
