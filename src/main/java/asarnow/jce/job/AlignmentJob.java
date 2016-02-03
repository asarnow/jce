package asarnow.jce.job;

import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.ce.ConfigStrucAligParams;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AtomCache;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/8/11
 * Time: 10:42 PM
 */
public class AlignmentJob<T> implements Callable<T> {

    private AtomCache cache;
    private String id1;
    private String id2;
    private String algorithmName;
    private ConfigStrucAligParams params;
    private Function<AlignmentResult,T> handler;

    public AlignmentJob(AtomCache cache, String id1, String id2, String algorithmName, ConfigStrucAligParams params, Function<AlignmentResult,T> handler) {
        this.cache = cache;
        this.id1 = id1;
        this.id2 = id2;
        this.algorithmName = algorithmName;
        this.params = params;
        this.handler = handler;
    }

    @Override
    public T call() throws IOException, StructureException {

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

        AFPChain result = aligner.align(target1.getValue(), target2.getValue());
        result.setName1(target1.getKey());
        result.setName2(target2.getKey());

//        return new AlignmentResult(result, target1.getValue(), target2.getValue());
        return handler.apply(new AlignmentResult(result, target1.getValue(), target2.getValue()));
    }
}
