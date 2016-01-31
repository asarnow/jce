import asarnow.jce.Align;
import asarnow.jce.Constants;
import asarnow.jce.Utility;
import asarnow.jce.io.OutputHandler;
import asarnow.jce.io.ProgressiveOutput;
import asarnow.jce.job.AlignmentJob;
import asarnow.jce.job.JobSeries;
import asarnow.jce.job.ProgressiveAlignmentJobSeries;
import org.biojava.nbio.structure.align.ce.CeMain;
import org.biojava.nbio.structure.align.ce.CeParameters;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.jmol.minimize.Util;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * (C) 1/29/16 Daniel Asarnow
 */
public class ProgressiveAlignmentTest {

    @Test
    public void progressive() {
        ClassLoader classLoader = getClass().getClassLoader();
        File listFile = new File(classLoader.getResource("pdblist.txt").getFile());
        String pdbDir = new File(classLoader.getResource("pdb").getFile()).getAbsolutePath();
        File outputFile = null;
        try {
            outputFile = File.createTempFile( "jce-alig", ".pdb");
        } catch (IOException e) {
            e.printStackTrace();
        }
        AtomCache cache = Utility.initAtomCache(pdbDir);
        String root = "1a0aA";
        root = Utility.standardizeId(root);
        List<String> list2align = Utility.listFromFile(listFile);
        Utility.standardizeIds(list2align);
        if (list2align.contains(root)) list2align.remove(root);
        JobSeries<AFPChain> jobs = new ProgressiveAlignmentJobSeries(list2align, root, cache, CeMain.algorithmName, new CeParameters());
        OutputHandler output = new ProgressiveOutput(cache, root, outputFile);
        Align.align(jobs, Utility.createThreadPool(4), output);
    }

}
