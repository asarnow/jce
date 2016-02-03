package asarnow.jce.io;

import asarnow.jce.job.AlignmentResult;

/**
 * (C) 1/29/16 Daniel Asarnow
 */
public interface OutputHandler<T> {

    public void handle(T result);

    public void close();

    public boolean isOpened();
}
