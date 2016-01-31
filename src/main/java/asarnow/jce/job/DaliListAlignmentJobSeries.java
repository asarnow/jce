package asarnow.jce.job;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author Daniel Asarnow
 */
public class DaliListAlignmentJobSeries {


    private final List<String> ids;
    private final BlockingQueue<String> queue;
    private int i = 0;

    public DaliListAlignmentJobSeries(List<String> ids, BlockingQueue<String> queue) {
        this.ids = ids;
        this.queue = queue;

        for (int j=0; j<ids.size(); j++) {
            int st,en,ch;
            String id = ids.get(j);
            if ( id.length() == 7 ) { st = 1; en = 5; ch = 5; } // SCOP domain (d1hhbA1)
            else if ( id.length()==6 ) { st = 0; en = 4; ch = 6; } // BioJava style (1hhb.A)
            else { st = 0; en = 4; ch = 4; } // PDB style (1hhbA), ignore extra characters
            ids.set(j, id.substring(st,en) + id.substring(ch,ch+1).toUpperCase());
        }
    }

    public boolean hasNext() {
        return i<ids.size()-1;
    }

    public DaliListAlignmentJob next() {
        String id = ids.get(i);
        List<String> others = ids.subList(i+1,ids.size());
//        others.remove(id);
        i++;
        try {
            return new DaliListAlignmentJob(queue,id,others);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int remaining() {
        return ids.size() - i;
    }

    public int completed() {
        return i;
    }

    public int total() {
        return ids.size();
    }
}
