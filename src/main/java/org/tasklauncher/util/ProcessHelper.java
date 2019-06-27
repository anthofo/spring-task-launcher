package org.tasklauncher.util;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ProcessHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessHelper.class);

    /**
     * Runs the specified jar as a sub-process. I/O are inherited from parents.
     * Sub-process should be automatically killed if parent is killed but it may be platform dependant.
     *
     * @param resource The jar to run as as sub-process
     * @param parameters Command line parameters
     * @return the running Process
     * @throws IOException
     */
    public static Process runJarProcess(Resource resource, String... parameters) throws IOException {
        List<String> commands = new ArrayList<>();
        commands.add(deduceJavaCommand());
        commands.addAll(Arrays.asList(parameters));
        commands.add("-jar");
        commands.add(resolveFile(resource).getAbsolutePath());

        LOGGER.info("Running new process: {}", commands.stream().collect(Collectors.joining(" ")));

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.inheritIO(); // Redirect sub-process stdout to current process stdout
        processBuilder.command(commands);

        return processBuilder.start();
    }

    /**
     * Wait for the process to end, then completes the CompletableFuture with the process exit status
     */
    public static CompletableFuture<Integer> waitForProcessEnds(Process p) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        new Thread(() -> {
            int exitValue = -1;
            try {
                LOGGER.info("Waiting for process exit");
                exitValue = p.waitFor();
            } catch (InterruptedException ignored) {
            } finally {
                LOGGER.info("Process exited with exit value {}", exitValue);
                future.complete(exitValue);
            }
        }).start();
        return future;
    }

    private static boolean isWindows() {
        String osName = null;
        try {
            osName = System.getProperty("os.name");
        } catch (Exception ignored) {
        }
        return osName != null && osName.toLowerCase().startsWith("windows");
    }

    private static String deduceJavaCommand() {
        String javaExecutablePath = ProcessHelper.isWindows() ? "java.exe" : "java";
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) javaExecutablePath = new File(javaHome, "bin" + File.separator + javaExecutablePath).getAbsolutePath();
        else LOGGER.warn("System property 'java.home' is not set. Defaulting to the java executable path as java assuming it's in PATH.");
        return javaExecutablePath;
    }

    /**
     * Returns a File from a Resource. If the the Resource cannot be resolved to a file (e.g. s3 file), its content is downloaded in a tmp file.
     */
    private static File resolveFile(Resource resource) throws IOException {
        try {
            LOGGER.info("Resolving resource {}", resource);
            return resource.getFile();
        } catch (UnsupportedOperationException e) {
            // If if is an s3 file, we can only access the input stream
            // resource.getFilename() contains the complete path, for instance "my-app/may-app.jar"
            Path tmpFilePath = Files.createTempFile(com.google.common.io.Files.getNameWithoutExtension(resource.getFilename()), "." + com.google.common.io.Files.getFileExtension(resource.getFilename()));
            LOGGER.info("Resource {} cannot be resolve and will be downloaded in a tmp file {}", resource, tmpFilePath);
            try (OutputStream os = Files.newOutputStream(tmpFilePath);
                 InputStream is = resource.getInputStream()) {
                IOUtils.copy(is, os);
            }
            LOGGER.info("Resource resolved in tmp file: {}", tmpFilePath);
            File file = tmpFilePath.toFile();
            file.deleteOnExit(); // Only deleted on graceful kill (not -9)
            return file;
        }
    }
}
