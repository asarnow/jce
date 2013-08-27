package asarnow.jce.job;

import org.biojava.bio.structure.align.util.AtomCache;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author Daniel Asarnow
 */
public class PairwiseAlignmentJobSeries implements JobSeries {

    private final List<String> ids;
    private final AtomCache cache;
    private final BlockingQueue<String> queue;
    private int i = -1;
    private int j;
    private int cnt = 0;
    private int n;
    private int alignerFlag;

    public PairwiseAlignmentJobSeries(List<String> ids, int alignerFlag, AtomCache cache, BlockingQueue<String> queue) {
        this.ids = ids;
        this.j = ids.size();
        this.alignerFlag = alignerFlag;
        this.cache = cache;
        this.queue = queue;
        n = ids.size() * (ids.size()-1) / 2;
    }

    @Override
    public boolean hasNext() {
        return cnt < n;
    }

    @Override
    public PairwiseAlignmentJob next() {
        if (j==ids.size()) j = (++i)+1;
        cnt++;
        return new PairwiseAlignmentJob(cache, ids.get(i), ids.get(j++), alignerFlag, queue);
    }

    @Override
    public int remaining() {
        return n - cnt;
    }

    @Override
    public int completed() {
        return cnt;
    }

    @Override
    public int total() {
        return n;
    }
}
