package ca.hutch.convert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ConvertSingleFile {
    Pattern MAIN_HEADING = Pattern.compile("#[^#]*$");
    Pattern PAGE_DIVIDER = Pattern.compile("---\\s*$");
    private Page page;
    private String filename;

    PrintWriter printWriter;
    public static void main(String[] args) {
        String filename = "/Users/hutchig1/Documents/git-repos/refactoring/PITCHME.md";
        new ConvertSingleFile(filename).processFile();
    }

    public ConvertSingleFile(String filename) {
        this.filename = filename;
        File file = new File(filename);
        File parent = file.getParentFile();
        File newDirectory = new File(parent, "src/docs/asciidoc");
        boolean success = newDirectory.mkdirs();
        System.out.println ("" + success + "\n");
        File newFile = new File(newDirectory, "index.adoc");
        OutputStream output = null;
        try {
            FileWriter fileWriter = new FileWriter(newFile.getAbsolutePath());
            printWriter = new PrintWriter(fileWriter);
        }
        catch (Exception e) {
            throw new IllegalStateException("File Not Found", e);
        }
    }
    public void processFile() {
        Path path = Paths.get(filename);
        try (Stream<String> stream = Files.lines(path)) {
            stream.forEach((line) -> {
                processLine(line);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        printWriter.flush();
        printWriter.close();
        System.out.println ("Everything closed");
    }

    private void processLine(String line) {
        Matcher matcher;

        matcher = MAIN_HEADING.matcher(line);
        if (matcher.matches()) {
            printWriter.printf("#%s\n", line);
            printWriter.printf("ifndef::imagesdir[:imagesdir: images]\n" +
                    ":revealjs_theme: solarized\n" +
                    ":revealjs_hash: true\n" +
                    ":tip-caption: \uD83D\uDCA1\n", line);
            page = new Page(printWriter);
            return;
        }

        matcher = PAGE_DIVIDER.matcher(line);
        if (matcher.matches()) {
            page.processPage();
            page = new Page(printWriter);
            return;
        }
        page.addLine(line);
    }

}
