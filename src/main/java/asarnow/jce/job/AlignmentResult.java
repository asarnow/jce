package asarnow.jce.job;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.align.model.AFPChain;

/**
 * (C) 1/30/16 Daniel Asarnow
 */
public class AlignmentResult {

    private final AFPChain afpChain;
    private final Atom[] ca1;
    private final Atom[] ca2;

    public AlignmentResult(AFPChain afpChain, Atom[] ca1, Atom[] ca2) {
        this.afpChain = afpChain;
        this.ca1 = ca1;
        this.ca2 = ca2;
    }

    public AFPChain getAfpChain() {
        return afpChain;
    }

    public Atom[] getCa1() {
        return ca1;
    }

    public Atom[] getCa2() {
        return ca2;
    }
}
