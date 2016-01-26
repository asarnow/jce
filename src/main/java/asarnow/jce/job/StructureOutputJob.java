package asarnow.jce.job;

import asarnow.jce.Utility;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.util.AtomCache;

import java.io.IOException;

/**
 * @author Daniel Asarnow
 */
public class StructureOutputJob implements Runnable {

    private AtomCache cache;
    private String id;
    private String extractDir;
    private boolean compressed;

    public StructureOutputJob(AtomCache cache, String id, String extractDir) {
        this(cache, id, extractDir, false);
    }

    public StructureOutputJob(AtomCache cache, String id, String extractDir, boolean compressed) {
        this.cache = cache;
        this.id = id;
        this.extractDir = extractDir;
        this.compressed = compressed;
    }

    public void run() {
        try {
            Structure structure = cache.getStructure(id);
            Utility.writePDB(structure, extractDir, compressed);
        } catch (IOException | StructureException e) {
            e.printStackTrace();
        }
    }

}
