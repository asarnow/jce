package asarnow.jce.io;

import asarnow.jce.job.AlignmentResult;

/**
 * (C) 1/29/16 Daniel Asarnow
 */
public interface OutputHandler {

    public void handle(AlignmentResult result);

    public void close();

    public boolean isOpened();
}
