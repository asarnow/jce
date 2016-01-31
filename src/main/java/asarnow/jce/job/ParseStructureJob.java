package asarnow.jce.job;

import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.ce.ConfigStrucAligParams;
import org.biojava.nbio.structure.align.util.AtomCache;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.Callable;

/**
 * (C) 1/30/16 Daniel Asarnow
 */
public class ParseStructureJob implements Callable<AlignmentJob> {

    private AtomCache cache;
    private String id1;
    private String id2;
    private String algorithmName;
    private ConfigStrucAligParams params;

    public ParseStructureJob(AtomCache cache, String id1, String id2, String algorithmName, ConfigStrucAligParams params) {
        this.cache = cache;
        this.id1 = id1;
        this.id2 = id2;
        this.params = params;
        this.algorithmName = algorithmName;
    }

    @Override
    public AlignmentJob call() throws IOException, StructureException {
        Structure structure1 = cache.getStructure( id1 );
        Structure structure2 = cache.getStructure( id2 );

        SimpleEntry<String,Atom[]> target1, target2;
        if (structure1.getChains().size() > 1) {
            Chain chain1 = structure1.getChain(0);
            Chain chain2 = structure2.getChain(0);
            Atom[] ca1 = StructureTools.getAtomCAArray(chain1);
            Atom[] ca2 = StructureTools.getAtomCAArray(chain2);
            target1 = new SimpleEntry<>(id1 + "." + chain1.getChainID(), ca1);
            target2 = new SimpleEntry<>(id2 + "." + chain2.getChainID(), ca2);
        } else {
            Atom[] ca1 = StructureTools.getAtomCAArray(structure1);
            Atom[] ca2 = StructureTools.getAtomCAArray(structure2);
            target1 = new SimpleEntry<>(id1, ca1);
            target2 = new SimpleEntry<>(id2, ca2);
        }
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(algorithmName);
        aligner.setParameters(params);
        return new AlignmentJob(target1, target2, aligner);
    }
}
