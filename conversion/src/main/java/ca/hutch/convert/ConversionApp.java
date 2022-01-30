package ca.hutch.convert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConversionApp {

    public static void main(String[] args) {
        new ConversionApp().run();
    }
    void run() {
        String directory = "/Users/hutching/git-repos/agile/";
        String filename = "/Users/hutching/git-repos/agile/daily-scrum/PITCHME.md";
        List<String> subdirectories = getDirectoryNames(directory);
        for (String sub: subdirectories) {
            System.out.println(sub);
               new ConvertFile(sub+"/PITCHME.md").processFile();
        }
 //       new ConvertFile(filename).processFile();
    }

    public List<String> getDirectoryNames(String dir) {
        List<String> strings =  Stream.of(new File(dir).listFiles())
                .filter(file -> !shouldIgnoreFile(file))
                .map(File::getAbsolutePath)
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
        String[] special = new String[] { "conversion", "src", "build", "target", "assets", "gradle" };
        return Arrays.asList(special);
    }

}