package asarnow.jce.job;

import asarnow.jce.io.DaliImport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author Daniel Asarnow
 */
public class DaliImportDCCPJob extends FileOutputJob {

    public DaliImportDCCPJob(BlockingQueue<String> queue, File file) {
        super(queue, file);
    }

    public DaliImportDCCPJob(BlockingQueue<String> queue, String filePath) {
        this(queue, new File(filePath));
    }

    @Override
    public void run() {
        try (Writer output = new BufferedWriter(new FileWriter(this.file))) {
            while( true ) {
                String fileName = queue.take();
                if (fileName.equals("STOP")){
                    break;
                } else {
                    File dccp = new File(fileName);
                    List<String> lines = null;
                    try {
                        if (dccp.exists()) {
                            lines = DaliImport.parseDCCP(dccp);
                        }
                    } catch (IOException | ParseException e) {
                        //TODO Log anomalous event
                        e.printStackTrace();
                    }
                    if (lines!=null)
                        for (String line : lines) {
                            output.write(line + System.lineSeparator());
                        }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
