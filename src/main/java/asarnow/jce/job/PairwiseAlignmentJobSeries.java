package asarnow.jce.job;

import org.biojava.nbio.structure.align.ce.ConfigStrucAligParams;
import org.biojava.nbio.structure.align.util.AtomCache;

import java.util.List;

/**
 * @author Daniel Asarnow
 */
public class PairwiseAlignmentJobSeries implements JobSeries<AlignmentResult> {

    private final List<String> ids;
    private final AtomCache cache;
    private int i = -1;
    private int j;
    private int cnt = 0;
    private int n;
    private String algorithmName;
    private ConfigStrucAligParams params;

    public PairwiseAlignmentJobSeries(List<String> ids, AtomCache cache, String algorithmName, ConfigStrucAligParams params) {
        this.ids = ids;
        this.j = ids.size();
        this.cache = cache;
        n = ids.size() * (ids.size()-1) / 2;
        this.algorithmName = algorithmName;
        this.params = params;
    }

    @Override
    public boolean hasNext() {
        return cnt < n;
    }

    @Override
    public AlignmentJob next() {
        if (j==ids.size()) j = (++i)+1;
        cnt++;
        return new AlignmentJob(cache, ids.get(i), ids.get(j++), algorithmName, params);
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
