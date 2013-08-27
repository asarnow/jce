package asarnow.jce.job;

import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/9/11
 * Time: 12:27 PM
 */
public class StandardOutputJob implements Runnable{

    BlockingQueue<String> queue;
    PrintStream stream;

    public StandardOutputJob(BlockingQueue<String> queue) {
        this(queue,false);
    }

    public StandardOutputJob(BlockingQueue<String> queue, boolean stderr) {
        this.queue = queue;
        this.stream = stderr ? System.err : System.out;
    }

    public void run(){
        try {
            int k = 0;
            while (true) {
//                System.out.println("FileOutputJob:30");
                String line = queue.take();
                if (line.equals("STOP")){
                    break;
                } else {
                    stream.print(line);
//                    System.out.println(++k);
                    k++;
                }
            }
            System.out.println(String.valueOf(k) + " lines written");
        } catch (InterruptedException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
