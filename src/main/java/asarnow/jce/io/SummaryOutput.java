package asarnow.jce.io;

import asarnow.jce.Utility;
import asarnow.jce.job.AlignmentResult;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/9/11
 * Time: 12:27 PM
 */
public class SummaryOutput implements OutputHandler {

    File file;
    private Writer output;
    private boolean opened;

    public SummaryOutput(File file) {
        this.file = file;
        open();
    }

    public SummaryOutput(String filePath) {
        this(new File(filePath));
    }

    private void open() {
        try {
            output = new BufferedWriter(new FileWriter(this.file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        opened = true;
    }

    @Override
    public void handle(AlignmentResult result) {
        try {
            output.write(Utility.summarizeAfpChain(result.getAfpChain()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        opened = false;
    }

    public boolean isOpened() {
        return opened;
    }
}
