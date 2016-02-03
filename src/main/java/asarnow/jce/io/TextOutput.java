package asarnow.jce.io;

import org.biojava.nbio.structure.align.util.AtomCache;

import java.io.*;

/**
 * (C) 1/30/16 Daniel Asarnow
 */
public class TextOutput implements OutputHandler<String> {


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
    public void handle(String result) {
        try {
            output.write(result);
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
