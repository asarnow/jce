package asarnow.jce.job;

/**
 * @author Daniel Asarnow
 */
public interface JobSeries {

    public boolean hasNext();

    public Runnable next();

    public int remaining();

    public int completed();

    public int total();
}
