package asarnow.jce.io;

import org.biojava.nbio.structure.align.model.AFPChain;

/**
 * (C) 1/29/16 Daniel Asarnow
 */
public interface OutputHandler {

    public void handle(AFPChain afpChain);

    public void close();

    public boolean isOpened();
}
