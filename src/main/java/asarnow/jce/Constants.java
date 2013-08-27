package asarnow.jce;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 7/8/11
 * Time: 10:54 PM
 */
public interface Constants {
    public static final int NOALIGN = -1;
    public static final int CE = 0;
    public static final int FATCAT = 1;
    public static final int FATCAT_RIGID = 2;
    public static final int DALI = 3;
//    public static final int MATT = 4;

    public static final List<String> CE_ALIASES = Arrays.asList("ce");
    public static final List<String> FATCAT_ALIASES = Arrays.asList("fatcat","fatcat_flexible","fatcat-flexible");
    public static final List<String> FATCAT_RIGID_ALIASES = Arrays.asList("fatcat_rigid","fatcat-rigid","fatcatr");

    public static final String TEMP_DIR_PREFIX = "jce";
    public static final String CWD = System.getProperty("user.dir");

    public static final String TARGET_LIST_FILE_NAME = "alignmentTargets.txt";

    public static final String DALI_BINARY_PATH = "DaliLite";
    public static final String DALI_LIST_ARG = "-list";
    public static final String DALI_LOG_FILE_NAME = "dali.log";

    public static final int NPROC_DEFAULT = 1;

}
