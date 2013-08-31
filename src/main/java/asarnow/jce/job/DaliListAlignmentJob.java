package asarnow.jce.job;

import asarnow.jce.Constants;
import asarnow.jce.Utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author Daniel Asarnow
 */
public class DaliListAlignmentJob implements Runnable {

    private String id;
    private File workingDir;
    private File listFile;
    private File logFile;
    private BlockingQueue<String> output;

    public DaliListAlignmentJob( BlockingQueue<String> output,String id, List<String> others) throws IOException {
        this.id = id;
        this.workingDir = Files.createTempDirectory(Constants.TEMP_DIR_PREFIX).toFile();
        this.output = output;
        this.listFile = new File(this.workingDir.getAbsolutePath(),Constants.TARGET_LIST_FILE_NAME);
        this.logFile = new File(this.workingDir.getAbsolutePath(),Constants.DALI_LOG_FILE_NAME);
        Utility.listToFile(listFile,others);
    }

    @Override
    public void run() {
        String script = Constants.DALI_BINARY_PATH + " " + Constants.DALI_LIST_ARG + " " + id + " " + listFile;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.logFile))) {
            ProcessBuilder pb = new ProcessBuilder("/bin/bash","-c",script);
            pb.redirectErrorStream(true); // merge error output with stdout
            pb.directory(workingDir);
            Process shell = pb.start();
            BufferedReader processOutput = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            int c;
            while ((c = processOutput.read()) != -1)
            {
                writer.write(c);
            }
            shell.waitFor();
            output.put(new File(workingDir,id+".dccp").getAbsolutePath());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
