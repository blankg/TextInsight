package com.github.blankg.textinsight.cli;

import com.github.blankg.textinsight.nlp.NLPException;
import com.github.blankg.textinsight.nlp.WordNetService;
import com.github.blankg.textinsight.nlp.input.subs.SrtParser;
import com.github.blankg.textinsight.nlp.input.subs.SrtSubtitles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class CLIService {

    public static void main(String[] args) {

        if (args.length < 0) {
            throw new IllegalArgumentException("No input provided");
        }

        String srtPath = args[0];

        try (InputStream is = Files.newInputStream(Paths.get(srtPath))) {
            SrtSubtitles subs = SrtParser.parse(is);
            List<SrtSubtitles.Line> lines = subs.getSubs();

            WordNetService wnService = new WordNetService();
            String content = wnService.createContentFromNLPData(lines);

            URL jarLocation = CLIService.class.getProtectionDomain().getCodeSource().getLocation();
            URL outputUrl = new URL(jarLocation, "output.txt");
            writeContentToFile(content, outputUrl.getPath());



        } catch (IOException | NLPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }



    static void writeContentToFile(String content, String path) {
        FileOutputStream fop = null;
        File file;

        try {

            file = new File(path);
            if(!file.getParentFile().exists())
            {
                file.getParentFile().mkdirs();
            }
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
