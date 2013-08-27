package asarnow.jce.job;

import asarnow.jce.Constants;
import asarnow.jce.io.DaliImport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.concurrent.BlockingQueue;

/**
 * @author Daniel Asarnow
 */
public class DaliImportDCCPDirJob extends FileOutputJob {

    public DaliImportDCCPDirJob(BlockingQueue<String> queue, String filePath) {
        this(queue, new File(filePath));
    }

    public DaliImportDCCPDirJob(BlockingQueue<String> queue, File filePath) {
        super(queue, filePath);
    }

    @Override
    public void run() {
        try (Writer output = new BufferedWriter(new FileWriter(this.file))) {
            int k = 0;
            while (true) {
                String directory = queue.take();
                if (directory.equals("STOP")){
                    break;
                } else {
                    File cwd = new File(directory);
                    BufferedReader logReader = null;
                    BufferedReader dccpReader = null;
                    try {
                        if (! (cwd.exists() && cwd.isDirectory()) ) throw new IOException();
                        logReader = new BufferedReader(new FileReader(new File(cwd, Constants.DALI_LOG_FILE_NAME)));
                        String line;
                        while ( ( line = logReader.readLine() ) != null ) {
                            if (line.contains("error")) {
                                throw new IOException();
                            }
                        }
//                        List<String> targets = Utility.listFromFile(new File(cwd, Constants.TARGET_LIST_FILE_NAME));
                        File[] dccps = cwd.listFiles(new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    return name.endsWith(".dccp");
                                }
                            });

                            for (File dccp : dccps) {
                                dccpReader = new BufferedReader(new FileReader(dccp));
                                while ( (line = dccpReader.readLine()) != null ) {
                                    output.write(DaliImport.parseDCCPLine(line));
                                    k++;
                                }
                                dccpReader.close();
                            }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (dccpReader!=null) dccpReader.close();
                            if (logReader!=null) logReader.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            //TODO auto-generated catch block
            e.printStackTrace();
        }


    }
}
