package ca.hutch.convert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ConvertFile {
    Pattern MAIN_HEADING = Pattern.compile("#[^#]*$");
    Pattern PAGE_DIVIDER = Pattern.compile("---\\s*$");
    Pattern IMAGE_DIRECTIVE = Pattern.compile("@img.*\\(.*/(.*)\\)");
    Pattern IMAGE_DIRECTIVE2 = Pattern.compile("!\\[\\s*\\]\\s*\\(.*/(.*)\\)");
    Pattern IMAGE_DIRECTIVE3 = Pattern.compile("---\\?image=.*\\/(.*)&");
    Pattern QUOTE = Pattern.compile("@quote\\[(.*)\\]");
    Pattern SNAP_DIRECTIVE = Pattern.compile("@snap.*");
    Pattern NOTE = Pattern.compile("^Note:.*");
    Pattern END_NOTE = Pattern.compile("(---$|\\s*)");
    Pattern U_LIST = Pattern.compile("@ul");
    
    private Queue<String> queue;
    private boolean inNote = false;

    private String filename;
 //   private PrintStream printStream;

    PrintWriter printWriter;
    
    public ConvertFile (String filename) {
        this.filename = filename;
        File file = new File(filename);
        File parent = file.getParentFile();
        String base = Paths.get(parent.getName()).toString();

        File grandparent = parent.getParentFile();
        File newDirectory = new File(grandparent, "src/docs/asciidoc/partials/");
        newDirectory.mkdirs();
        File newFile = new File(newDirectory, "_" + base + ".adoc");
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
        try (Stream<String> stream = Files.lines(Paths.get(filename))) {
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
        if (inNote) {
            matcher = END_NOTE.matcher(line);
            if (matcher.matches()) {
                inNote = false;
                printWriter.printf("[.notes]\n");
                printWriter.printf("--\n");
                queue.stream().forEach((str) -> {
                    printWriter.printf("%s\n", str);
                });
                printWriter.printf("--\n");
                return;
            }
            queue.add(line);
            return;
        }
        matcher = NOTE.matcher(line);
        if (matcher.matches()) {
            inNote = true;
            queue = new LinkedList<String>();
            return;
        }
        matcher = MAIN_HEADING.matcher(line);
        if (matcher.matches()) {
            printWriter.printf("#%s\n", line);
            printWriter.printf("ifndef::imagesdir[:imagesdir: images]\n" +
                    ":revealjs_theme: solarized\n" +
                    ":revealjs_hash: true\n" +
                    ":tip-caption: \uD83D\uDCA1\n", line);
            return;
        }

        matcher = PAGE_DIVIDER.matcher(line);
        if (matcher.matches()) {
            printWriter.printf("\n");
            return;
        }

        matcher = IMAGE_DIRECTIVE.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group(1);
            printWriter.printf("\nimage::%s[%s,640,480]\n", name,name);
            return;
        }
        matcher = IMAGE_DIRECTIVE2.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group(1);
            printWriter.printf("\nimage::%s[%s,640,480]\n", name,name);
            return;
        }
        matcher = IMAGE_DIRECTIVE3.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group(1);
            printWriter.printf("\nimage::%s[%s,640,480]\n", name,name);
            return;
        }
        matcher = SNAP_DIRECTIVE.matcher(line);
        if (matcher.matches()) {
            printWriter.printf("// %s\n", line);
            return;
        }
        matcher = QUOTE.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group(1);
            printWriter.printf("[quote, unknown]\n");
            printWriter.printf("----\n%s\n----\n", name);
            return;
        }
        matcher = U_LIST.matcher(line);
        if (matcher.matches()) {
            printWriter.printf("[%step]\n");
            return;
        }
        printWriter.printf("%s\n", line);
    }

}
