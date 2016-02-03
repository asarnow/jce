package asarnow.jce.io;

import asarnow.jce.Utility;
import asarnow.jce.job.AlignmentResult;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.gui.DisplayAFP;
import org.biojava.nbio.structure.align.util.AtomCache;

import java.io.*;

/**
 * @author Daniel Asarnow
 */
public class ProgressiveOutput implements OutputHandler<Structure> {

    File file;
    AtomCache cache;
    String rootId;
    boolean opened;
    Structure progressive;


    public ProgressiveOutput(AtomCache cache, String rootId, File file) {
        this.file = file;
        this.cache = cache;
        this.rootId = rootId;
        open();
    }

    public ProgressiveOutput(AtomCache cache, String rootId, String filePath) {
        this(cache, rootId, new File(filePath));
    }

    private void open() {
        opened = true;
    }

    @Override
    public boolean isOpened() {
        return opened;
    }

    @Override
    public void close() {
        try (Writer output = new BufferedWriter(new FileWriter(this.file))) {
            output.write(progressive.toPDB());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(Structure result) {
        if (progressive == null) {
            progressive = result;
        } else {
            progressive.addModel(result.getModel(1));
        }
    }

}
