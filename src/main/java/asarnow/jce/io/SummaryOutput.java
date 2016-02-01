package asarnow.jce.io;

import asarnow.jce.Utility;
import asarnow.jce.job.AlignmentResult;
import org.apache.log4j.Logger;

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
    private static Logger logger = Logger.getLogger(SummaryOutput.class);

    File file;
    private PrintStream output;
    private boolean opened;
    private int counter;

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
                output = new PrintStream(System.out, true);
            } else {
                output = new PrintStream(new FileOutputStream(this.file), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        opened = true;
        counter = 0;
    }

    @Override
    public void handle(AlignmentResult result) {
        logger.debug("Got result for chains " + result.getAfpChain().getName1() + ", " + result.getAfpChain().getName2());
        output.print(Utility.summarizeAfpChain(result.getAfpChain()));
        counter++;
        if (counter % 10 == 0) {
            output.flush();
            logger.debug("Flushed output file on count " + counter);
        }
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
