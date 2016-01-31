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
    // Note all interface fields are public, static and final.
    int NOALIGN = -1;
    int CE = 0;
    int FATCAT_FLEX = 1;
    int FATCAT_RIGID = 2;
    int DALI = 3;
//  l int MATT = 4;

    List<String> CE_ALIASES = Arrays.asList("ce");
    List<String> FATCAT_ALIASES = Arrays.asList("fatcat","fatcat_flexible","fatcat-flexible");
    List<String> FATCAT_RIGID_ALIASES = Arrays.asList("fatcat_rigid","fatcat-rigid","fatcatr");

    String TEMP_DIR_PREFIX = "jce";
    String CWD = System.getProperty("user.dir");

    String TARGET_LIST_FILE_NAME = "alignmentTargets.txt";

    String DALI_BINARY_PATH = "DaliLite";
    String DALI_LIST_ARG = "-list";
    String DALI_LOG_FILE_NAME = "dali.log";

    int NPROC_DEFAULT = 1;

}
