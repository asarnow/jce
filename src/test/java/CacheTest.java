import asarnow.jce.Utility;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.junit.Test;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;

/**
 * (C) 1/29/16 Daniel Asarnow
 */
public class CacheTest {

    @Test
    public void cache() {
        ClassLoader classLoader = getClass().getClassLoader();
        String pdbDir = new File(classLoader.getResource("pdb").getFile()).getAbsolutePath();
        System.out.println(pdbDir);
        AtomCache cache = Utility.initAtomCache(pdbDir);
        Structure pdb1a0aA = null;
        try {
            pdb1a0aA = cache.getStructure("1a0aA");
        } catch (IOException | StructureException e) {
            e.printStackTrace();
        }
        assert(pdb1a0aA != null);
        assert(pdb1a0aA.hasChain("A"));
    }
}
