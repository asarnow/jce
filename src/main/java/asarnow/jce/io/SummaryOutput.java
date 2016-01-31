package asarnow.jce.io;

import asarnow.jce.Utility;
import asarnow.jce.job.AlignmentResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/9/11
 * Time: 12:27 PM
 */
public class SummaryOutput implements OutputHandler {

    File file;
    private PrintStream output;
    private boolean opened;

    public SummaryOutput(File file) {
        this.file = file;
        open();
    }

    public SummaryOutput(String filePath) {
        this(filePath == null ? null : new File(filePath));
    }

    private void open() {
        try {
            if (file == null) {
                output = new PrintStream(System.out);
            } else {
                output = new PrintStream(new FileOutputStream(this.file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        opened = true;
    }

    @Override
    public void handle(AlignmentResult result) {
        output.print(Utility.summarizeAfpChain(result.getAfpChain()));
    }

    @Override
    public void close() {
        output.close();
        opened = false;
    }

    public boolean isOpened() {
        return opened;
    }
}
