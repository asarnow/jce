package asarnow.jce.job;

import java.util.concurrent.Callable;

/**
 * @author Daniel Asarnow
 */
public interface JobSeries<T> {

    public boolean hasNext();

    public Callable<T> next();

    public int remaining();

    public int completed();

    public int total();

//    public void start();

//    public void add(T job);
}
