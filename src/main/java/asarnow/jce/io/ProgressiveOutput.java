package asarnow.jce.io;

import asarnow.jce.Utility;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.align.gui.DisplayAFP;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AtomCache;

import java.io.*;

/**
 * @author Daniel Asarnow
 */
public class ProgressiveOutput implements OutputHandler {

    File file;
    AtomCache cache;
    String rootId;
    boolean opened;
    Structure progressive;
    Atom[] rootCa;


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
    public void handle(AFPChain afpChain) {
        try {
            if (progressive == null) {
                this.progressive = new StructureImpl();
                Structure root = cache.getStructure(rootId);
                rootCa = StructureTools.getAtomCAArray(root);
                Structure progressive = new StructureImpl();
                progressive.addModel(root.getChains());
            }
            System.out.print(Utility.summarizeAfpChain(afpChain));
            Atom[] ca = StructureTools.getAtomCAArray(cache.getStructure(afpChain.getName2()));
            Structure artificial = DisplayAFP.createArtificalStructure(afpChain, rootCa, ca);
            progressive.addModel(artificial.getModel(1));
        } catch (IOException | StructureException e) {
            e.printStackTrace();
        }
    }

}
