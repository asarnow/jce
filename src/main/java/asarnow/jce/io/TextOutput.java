package asarnow.jce.io;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.model.AfpChainWriter;
import org.biojava.nbio.structure.align.util.AtomCache;

import java.io.*;

/**
 * (C) 1/30/16 Daniel Asarnow
 */
public class TextOutput implements OutputHandler {


    File file;
    AtomCache cache;
    private Writer output;
    private boolean opened;

    public TextOutput(AtomCache cache, File file) {
        this.cache = cache;
        this.file = file;
        open();
    }

    public TextOutput(AtomCache cache, String filePath) {
        this(cache, new File(filePath));
    }

    private void open() {
        try {
            output = new BufferedWriter(new FileWriter(this.file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        opened = true;
    }

    @Override
    public void handle(AFPChain afpChain) {
        try {
            Atom[] ca1 = new Atom[0];
            Atom[] ca2 = new Atom[0];
            try {
                ca1 = StructureTools.getAtomCAArray(cache.getStructure(afpChain.getName1()));
                ca2 = StructureTools.getAtomCAArray(cache.getStructure(afpChain.getName2()));
            } catch (StructureException e) {
                e.printStackTrace();
            }
            output.write(AfpChainWriter.toFatCat(afpChain, ca1, ca2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        opened = false;
    }

    public boolean isOpened() {
        return opened;
    }
}
