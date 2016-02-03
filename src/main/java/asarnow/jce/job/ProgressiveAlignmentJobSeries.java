package asarnow.jce.job;

import asarnow.jce.Utility;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.align.ce.ConfigStrucAligParams;
import org.biojava.nbio.structure.align.util.AtomCache;

import java.util.List;

/**
 * (C) 1/28/16 Daniel Asarnow
 */
public class ProgressiveAlignmentJobSeries implements JobSeries<Structure> {
    private final List<String> ids;
    private final String root;
    private final AtomCache cache;
    private int i = 0;
    private int n;
    private String algorithmName;
    private ConfigStrucAligParams params;

    public ProgressiveAlignmentJobSeries(List<String> ids, String root, AtomCache cache, String algorithmName, ConfigStrucAligParams params) {
        this.ids = ids;
        this.root = root;
        this.cache = cache;
        this.n = ids.size();
        this.algorithmName = algorithmName;
        this.params = params;
    }

    @Override
    public boolean hasNext() {
        return i < n;
    }

    @Override
    public AlignmentJob<Structure> next() {
        return new AlignmentJob<>(cache, root, ids.get(i++), algorithmName, params, Utility::createArtificialStructure);
    }

    @Override
    public int remaining() {
        return n - i;
    }

    @Override
    public int completed() {
        return i;
    }

    @Override
    public int total() {
        return n;
    }
}
