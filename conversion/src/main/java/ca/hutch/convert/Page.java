package ca.hutch.convert;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Page {
    Pattern IMAGE_DIRECTIVE = Pattern.compile("@img.*\\(.*/(.*)\\)");
    Pattern IMAGE_DIRECTIVE2 = Pattern.compile("!\\[.*\\]\\s*\\(.*/(.*)\\)");
    Pattern IMAGE_DIRECTIVE3 = Pattern.compile("---\\?image=.*\\/(.*)&");
    Pattern QUOTE = Pattern.compile("@quote\\[(.*)\\]");
    Pattern SNAP_DIRECTIVE = Pattern.compile("@snap.*");
    Pattern NOTE = Pattern.compile("^Note:.*");
    Pattern END_NOTE = Pattern.compile("(---$|\\s*)");
    Pattern U_LIST = Pattern.compile("@ul\\[.*");
    Pattern U_LIST_END = Pattern.compile("@ulend.*");
    Pattern TITLE = Pattern.compile("@tit.*");
    Pattern BOX = Pattern.compile("@box\\[.*\\]\\s*\\((.*)\\)");
    Pattern BREAK_ONLY = Pattern.compile("\\s*<br>\\s*");
    Pattern OTHER_HEADING = Pattern.compile("##*(.*)");

    private Queue<String> noteQueue;
    private boolean inNote = false;
    int imageCount = 0;
    int textCount = 0;

    PrintWriter printWriter;

    List<String> lines = new ArrayList<>();

    public Page(PrintWriter writer) {
        printWriter = writer;
    }

    public void addLine(String line) {
        lines.add(line);
    }
    public void processPage() {
        printWriter.printf("[.columns]\n");
        for (String line : lines) {
            processLine(line);
        }
        if (inNote) {
            processLine("---");
        }
        printWriter.printf("\n");
    }

    public void processLine(String line) {
        Matcher matcher;
        matcher = BREAK_ONLY.matcher(line);
        if (matcher.matches()) {
            printWriter.printf("\n", line);
            return;
        }
        line = line.replace("<br>", " +\n");
        if (inNote) {
            processInNote(line);
            return;
        }
        matcher = OTHER_HEADING.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group(1);
            printWriter.printf("##%s\n", name);
            return;
        }
        matcher = NOTE.matcher(line);
        if (matcher.matches()) {
            inNote = true;
            noteQueue = new LinkedList<String>();
            return;
        }

        if (processImage(line)) return;

        matcher = TITLE.matcher(line);
        if (matcher.matches()) {
            return;
        }

        matcher = SNAP_DIRECTIVE.matcher(line);
        if (matcher.matches()) {
            printWriter.printf("// %s\n", line);
            return;
        }
        matcher = QUOTE.matcher(line);
        if (matcher.matches()) {
            textCount++;
            String name = matcher.group(1);
            printWriter.printf("[quote, unknown]\n");
            printWriter.printf("----\n%s\n----\n", name);
            return;
        }
        matcher = BOX.matcher(line);
        if (matcher.matches()) {
            String bullet = matcher.group(1);
            textCount++;
            printWriter.printf("- %s\n", bullet);
            return;
        }
        matcher = U_LIST.matcher(line);
        if (matcher.matches()) {
            textCount++;
            printWriter.println("[%step]");
            return;
        }
        matcher = U_LIST_END.matcher(line);
        if (matcher.matches()) {
            return;
        }
        textCount++;
        printWriter.printf("%s\n", line);
    }

    private boolean processImage(String line) {
        Matcher matcher;
        matcher = IMAGE_DIRECTIVE.matcher(line);
        if (! matcher.matches()) {
            matcher = IMAGE_DIRECTIVE2.matcher(line);
            if (!matcher.matches()) {
                matcher = IMAGE_DIRECTIVE3.matcher(line);
                if (!matcher.matches())
                    return false;
            }
        }
        String name = matcher.group(1);
        imageCount++;
        printWriter.printf("[.column.is-one-third]\n");
        printWriter.printf("\nimage::%s[%s,640,480]\n", name,name);
        return true;
    }

    private void processInNote(String line) {
        Matcher matcher;
        matcher = END_NOTE.matcher(line);
        if (matcher.matches()) {
            inNote = false;
            printWriter.printf("[.notes]\n");
            printWriter.printf("--\n");
            noteQueue.stream().forEach((str) -> {
                printWriter.printf("%s\n", str);
            });
            printWriter.printf("--\n");
            return;
        }
        noteQueue.add(line);
        return;
    }
}
