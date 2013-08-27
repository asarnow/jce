package asarnow.jce.job;

import java.io.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/9/11
 * Time: 12:27 PM
 */
public class FileOutputJob implements Runnable{

    BlockingQueue<String> queue;
    File file;

    public FileOutputJob(BlockingQueue<String> queue, File file) {
        this.queue = queue;
        this.file = file;
    }

    public FileOutputJob(BlockingQueue<String> queue, String filePath) {
        this(queue,new File(filePath));
    }

    public void run(){
        try (Writer output = new BufferedWriter(new FileWriter(this.file))){
            int k = 0;

            while (true) {
//                System.out.println("FileOutputJob:30");
                String line = queue.take();
                if (line.equals("STOP")){
                    break;
                } else {
                    output.write(line);
//                    System.out.println(++k);
                    k++;
                }
            }
            System.out.println(String.valueOf(k) + " lines written");
        } catch (InterruptedException | IOException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
