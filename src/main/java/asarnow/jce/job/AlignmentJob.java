package asarnow.jce.job;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.model.AFPChain;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/8/11
 * Time: 10:42 PM
 */
public class AlignmentJob implements Callable<AFPChain> {

    private SimpleEntry<String,Atom[]> target1;
    private SimpleEntry<String,Atom[]> target2;
    StructureAlignment aligner;

    public AlignmentJob(SimpleEntry<String,Atom[]> target1, SimpleEntry<String,Atom[]> target2, StructureAlignment aligner) {
        this.target1 = target1;
        this.target2 = target2;
        this.aligner = aligner;
    }

    public AFPChain call() throws IOException, StructureException {
        AFPChain result = aligner.align(target1.getValue(), target2.getValue());
        result.setName1(target1.getKey());
        result.setName1(target2.getKey());
        return result;
    }
}
