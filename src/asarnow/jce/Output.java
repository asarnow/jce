package asarnow.jce;

import java.io.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/9/11
 * Time: 12:27 PM
 */
public class Output implements Runnable{

    BlockingQueue<String> queue;
    String filePath;

    public Output(BlockingQueue<String> queue, String filePath) {
        this.queue = queue;
        this.filePath = filePath;
    }

    public void run(){
        try {
            int k = 0;
            Writer output = new BufferedWriter(new FileWriter(this.filePath));
            while (true) {
//                System.out.println("Output:30");
                String line = queue.take();
                if (line.equals("STOP")){
                    break;
                } else {
                    output.write(line);
//                    System.out.println(++k);
                    k++;
                }
            }
            output.close();
            System.out.println(String.valueOf(k) + " lines written");
        } catch (InterruptedException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
