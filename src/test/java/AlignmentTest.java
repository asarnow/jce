import asarnow.jce.Utility;
import asarnow.jce.io.OutputHandler;
import asarnow.jce.io.TextOutput;
import asarnow.jce.job.AlignmentJob;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.ce.CeMain;
import org.biojava.nbio.structure.align.ce.CeParameters;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * (C) 1/30/16 Daniel Asarnow
 */
public class AlignmentTest {
    @Test
    public void alignment() {
        ClassLoader classLoader = getClass().getClassLoader();
        String pdbDir = new File(classLoader.getResource("pdb").getFile()).getAbsolutePath();
        File outputFile = null;
        try {
            outputFile = File.createTempFile( "jce-alig", ".pdb");
        } catch (IOException e) {
            e.printStackTrace();
        }
        AtomCache cache = Utility.initAtomCache(pdbDir);
        String id1 = "1a0a.A";
        String id2 = "1a0r.B";
        AlignmentJob alignJob = new AlignmentJob(cache, id1, id2, CeMain.algorithmName, new CeParameters());
        OutputHandler output = new TextOutput(cache, outputFile);
        try {
            output.handle(alignJob.call());
        } catch (IOException | StructureException e) {
            e.printStackTrace();
        }
        output.close();
    }
}
