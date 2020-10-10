import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Main {

    private static final String NEW_DIRECTORY = "C:/Users/roma/*****";
    private static final String ALL_PHOTOS = "C:/Users/roma_/*****";
    private static final String NEEDED_PHOTOS = "C:/Users/roma_/*****";
    private static final String RAW_PHOTO_EXTENSION = ".CR2";

    private static AtomicLong neededCopiedCount = new AtomicLong(0);
    private static AtomicLong generatedUniqueNamesCount = new AtomicLong(0);

    private static List<File> neededJpgFiles;
    private static List<File> allFiles;

    public static void main(String[] args) {
        runFileManager();
    }

    private static void runFileManager() {
        initializeFiles();
        createDirIfNotExists();
        filterAndGenerateFiles(neededJpgFiles, allFiles);
    }

    private static void initializeFiles() {
        neededJpgFiles = getNeededFiles().orElseThrow(IllegalArgumentException::new);
        allFiles = getAllFilesToFilter().orElseThrow(IllegalArgumentException::new);
        System.out.println("Files to copy: " + neededJpgFiles.size());
        System.out.println("Source files: " + allFiles.size());
    }

    private static Optional<List<File>> getNeededFiles() {
        try {
            return Optional.ofNullable(Files.walk(Paths.get(NEEDED_PHOTOS))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            System.out.println("Error while getting needed files!");
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static Optional<List<File>> getAllFilesToFilter() {
        try {
            return Optional.ofNullable(Files.walk(Paths.get(ALL_PHOTOS))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            System.out.println("Error while getting all files to filter!");
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static void createDirIfNotExists() {
        File dir = new File(NEW_DIRECTORY);
        if (!dir.exists())
            dir.mkdirs();
    }

    private static void filterAndGenerateFiles(List<File> neededJpgFiles, List<File> allFiles) {
        for (File jpg : neededJpgFiles) {
            String cr2FileName = getOnlyFileNameWithNumberWithoutDuplicatedNumbers(jpg.getName())
                    .concat(RAW_PHOTO_EXTENSION);
            for (File rawOrJpg : allFiles) {
                generateIfSameFile(cr2FileName, rawOrJpg);
            }
        }
        logFilteringAndGenerationResult();
    }

    private static String getOnlyFileNameWithNumberWithoutDuplicatedNumbers(String fileName) {
        return fileName.substring(0, 8);
    }

    private static void logFilteringAndGenerationResult() {
        System.out.println(neededCopiedCount.toString() + " Files copied from: " + neededJpgFiles.size() + " needed");
        System.out.println("Generated unique names for: " + generatedUniqueNamesCount + " files");
    }

    private static Optional<File> generateIfSameFile(String required, File actual) {
        if (required.equals(actual.getName())) {
            return Optional.of(generateAndCopyFile(required, actual));
        }
        return Optional.empty();
    }

    private static File generateAndCopyFile(String cr2FileName, File rawOrJpg) {
        File copy = new File(rawOrJpg.toPath().toString());
        File newFromCopy = new File(NEW_DIRECTORY + "/" + cr2FileName);
        return generateAndCopyWithUniqueNameOrRegular(copy, newFromCopy, cr2FileName);
    }

    private static File generateAndCopyWithUniqueNameOrRegular(File copy, File newFromCopy, String cr2FileName) {
        if (newFromCopy.exists()) {
            neededCopiedCount.incrementAndGet();
            generatedUniqueNamesCount.incrementAndGet();
            return generateAndCopyFileWithUniqueName(cr2FileName, copy);
        } else {
            copyFile(copy, newFromCopy);
            neededCopiedCount.incrementAndGet();
            return newFromCopy;
        }
    }

    private static File generateAndCopyFileWithUniqueName(String cr2FileName, File copy) {
        cr2FileName = generateRandomFileName(cr2FileName);
        File newWithUniqueGeneratedNumber = new File(NEW_DIRECTORY + "/" + cr2FileName);
        copyFile(copy, newWithUniqueGeneratedNumber);
        return newWithUniqueGeneratedNumber;
    }

    private static String generateRandomFileName(String fileName) {
        Long randomNumber = new Random().nextLong();
        return fileName.replace(RAW_PHOTO_EXTENSION, randomNumber.toString().concat(RAW_PHOTO_EXTENSION));
    }

    private static void copyFile(File copy, File newFromCopy) {
        try {
            Files.copy(copy.toPath(), newFromCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error wile copying file: " + copy.getPath());
            System.out.println("New file from copy: " + newFromCopy.getPath());
            e.printStackTrace();
        }
    }
}
