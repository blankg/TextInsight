package com.github.blankg.textinsight.nlp.input.subs;

import com.github.blankg.textinsight.nlp.NLPManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Parses a .srt file and creates a Track for it.
 */
public class SrtParser {
    public static SrtSubtitles parse(InputStream is) throws IOException {
        LineNumberReader r = new LineNumberReader(new InputStreamReader(is, "UTF-8"));
        SrtSubtitles track = new SrtSubtitles();
        String numberString;
        while ((numberString = r.readLine()) != null) {
            String timeString = r.readLine();
            String lineString = "";
            String s;
            while (!((s = r.readLine()) == null || s.trim().equals(""))) {
                lineString += s + "\n";
            }

            long startTime = parse(timeString.split("-->")[0]);
            long endTime = parse(timeString.split("-->")[1]);
            
            List<NLPManager.NLPData> nlpData = NLPManager.getInstance().getNLPData(lineString);
            
            SrtSubtitles.Line line = new SrtSubtitles.Line(startTime, endTime, lineString);
            line.setNLPData(nlpData);
            line.setSentiment(NLPManager.getInstance().analyzeSentiment(lineString));

            track.getSubs().add(line);

        }
        return track;
    }

    public static long parse(String in) {
        long hours = Long.parseLong(in.split(":")[0].trim());
        long minutes = Long.parseLong(in.split(":")[1].trim());
        long seconds = Long.parseLong(in.split(":")[2].split(",")[0].trim());
        long millies = Long.parseLong(in.split(":")[2].split(",")[1].trim());

        return hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000 + millies;

    }
    
    public static String parse(long in) {
    	long hours = TimeUnit.MILLISECONDS.toHours(in);
    	long minutes = TimeUnit.MILLISECONDS.toMinutes(in) - TimeUnit.HOURS.toMinutes(hours);
    	long seconds = TimeUnit.MILLISECONDS.toSeconds(in) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours);
    	long milies = TimeUnit.MILLISECONDS.toMillis(in) - TimeUnit.MILLISECONDS.toMillis(seconds) - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.HOURS.toMillis(hours);
    	return String.format("%d:%d:%d:%d", hours, minutes, seconds, milies);

    }
}
