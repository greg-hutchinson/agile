package ca.fcc.robot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConversionApp {
    Pattern MAIN_HEADING = Pattern.compile("#[^#]*$");
    Pattern PAGE_DIVIDER = Pattern.compile("---\\s*$");
    Pattern IMAGE_DIRECTIVE = Pattern.compile("@img.*\\(.*/(.*)\\)");
    Pattern IMAGE_DIRECTIVE2 = Pattern.compile("!\\[.*\\]\\s*\\(.*/(.*)\\)");
    Pattern IMAGE_DIRECTIVE3 = Pattern.compile("---\\?image=.*\\/(.*)&");
    Pattern QUOTE = Pattern.compile("@quote\\[(.*)\\]");
    Pattern SNAP_DIRECTIVE = Pattern.compile("@snap.*");
    Pattern NOTE = Pattern.compile("^Note:.*");
    Pattern END_NOTE = Pattern.compile("(---$|\\s*)");
    Pattern U_LIST = Pattern.compile("@ul");
    String s = "";
    Pattern SAMPLE = Pattern.compile("");
    private Queue<String> queue;
    private boolean inNote = false;

    public static void main(String[] args) {
        new ConversionApp().run();
    }

    void run() {
        String directory = "/Users/hutching/git-repos/agile/";
        String filename = "/Users/hutching/git-repos/agile/complicated-vs-complex/PITCHME.md";
        List<String> subdirectories = getDirectoryNames(directory);
        for (String sub: subdirectories) {
            //System.out.println(sub);
        }
        processFile(filename);
    }

    public List<String> getDirectoryNames(String dir) {
        List<String> strings =  Stream.of(new File(dir).listFiles())
                .filter(file -> !shouldIgnoreFile(file))
                .map(File::getName)
                .collect(Collectors.toList());
        Collections.sort(strings);
        return strings;
    }

    boolean shouldIgnoreFile(File file) {
        if (!file.isDirectory())
            return true;
        Path path = Paths.get(file.getName());
        String fileName = path.getFileName().toString();
        if (fileName.startsWith("."))
            return true;
        return getSpecialDirectories().contains(fileName);
    }

    private List<String> getSpecialDirectories() {
        String[] special = new String[] { "conversion", "src", "build", "target", "assets" };
        return Arrays.asList(special);
    }

    private void processFile(String filename) {
        //File file = new File(fileName);
        try (Stream<String> stream = Files.lines(Paths.get(filename))) {
            stream.forEach((line) -> {
                processLine(line);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line) {
        Matcher matcher;
        if (inNote) {
            matcher = END_NOTE.matcher(line);
            if (matcher.matches()) {
                inNote = false;
                System.out.printf("[.notes]\n");
                System.out.printf("--\n");
                queue.stream().forEach((str) -> {
                    System.out.printf("%s\n", str);
                });
                System.out.printf("--\n");
                return;
            }
            queue.add(line);
        }
        matcher = NOTE.matcher(line);
        if (matcher.matches()) {
            inNote = true;
            queue = new LinkedList<String>();
            return;
        }
        matcher = MAIN_HEADING.matcher(line);
        if (matcher.matches()) {
            System.out.printf("%s\n", line);
            System.out.printf("ifndef::imagesdir[:imagesdir: images]\n" +
                    ":revealjs_theme: solarized\n" +
                    ":revealjs_hash: true\n" +
                    ":tip-caption: \uD83D\uDCA1\n", line);
            return;
        }

        matcher = PAGE_DIVIDER.matcher(line);
        if (matcher.matches()) {
            System.out.printf("\n");
            return;
        }

        matcher = IMAGE_DIRECTIVE.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group(1);
            System.out.printf("image::%s[%s,640,480]\n", name,name);
            return;
        }
        matcher = IMAGE_DIRECTIVE2.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group(1);
            System.out.printf("image::%s[%s,640,480]\n", name,name);
            return;
        }
        matcher = IMAGE_DIRECTIVE3.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group(1);
            System.out.printf("image::%s[%s,640,480]\n", name,name);
            return;
        }
        matcher = SNAP_DIRECTIVE.matcher(line);
        if (matcher.matches()) {
            System.out.printf("// %s\n", line);
            return;
        }
        matcher = QUOTE.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group(1);
            System.out.printf("[quote, unknown]\n");
            System.out.printf("----\n%s\n----\n", name);
            return;
        }
        matcher = U_LIST.matcher(line);
        if (matcher.matches()) {
            System.out.printf("[%step]\n");
            return;
        }
        System.out.printf("%s\n", line);
    }
}