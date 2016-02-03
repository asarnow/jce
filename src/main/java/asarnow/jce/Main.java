package asarnow.jce; /**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 4/6/11
 * Time: 4:34 PM
 */

import asarnow.jce.io.OutputHandler;
import asarnow.jce.io.ProgressiveOutput;
import asarnow.jce.io.SummaryOutput;
import asarnow.jce.job.AlignmentResult;
import asarnow.jce.job.JobSeries;
import asarnow.jce.job.PairwiseAlignmentJobSeries;
import asarnow.jce.job.ProgressiveAlignmentJobSeries;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.biojava.nbio.structure.align.ce.CeMain;
import org.biojava.nbio.structure.align.ce.CeParameters;
import org.biojava.nbio.structure.align.ce.ConfigStrucAligParams;
import org.biojava.nbio.structure.align.fatcat.FatCatFlexible;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.fatcat.calc.FatCatParameters;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.align.util.UserConfiguration;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.LocalPDBDirectory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class);

	/**
	 * @param args Command line arguments
	 */
	public static void main(String[] args) throws IOException{
        OptionParser parser = new OptionParser();
        // Aligner selection
        OptionSpec fatcatArg = parser.acceptsAll(Arrays.asList("FATCAT-FLEX","fatcat-flex","fcf"), "Align with FATCAT_FLEX - flexible");
        OptionSpec fatcatRigidArg = parser.acceptsAll(Arrays.asList("FATCAT","fatcat","fc","fcr"), "Align with FATCAT - rigid");
        OptionSpec ceArg = parser.acceptsAll(Arrays.asList("CE","ce"), "Align with CE");
        OptionSpec daliArg = parser.acceptsAll(Arrays.asList("Dali","dali"),"Align using DaliLite external binary");
        // Aligner specific options
        // CE

        // FATCAT_FLEX


        // Structure selection
        OptionSpec<String> listArg = parser.acceptsAll(Arrays.asList("list", "l"), "Specify file of IDs for pairwise alignments").
                                            requiredIf(daliArg).
                                            withRequiredArg().
                                            ofType(String.class);

        // Multiple alignment
        OptionSpec<String> multipleArg = parser.acceptsAll(Arrays.asList("multiple", "m"), "Multiple alignment").
                                            withOptionalArg().
                                            ofType(String.class);
        OptionSpec<String> progressiveArg = parser.acceptsAll(Arrays.asList("root", "r"), "Specify root for progressive alignment").
                                            requiredIf(multipleArg).
                                            withRequiredArg().
                                            ofType(String.class);

        // PDB files
        OptionSpec<String> dirArg = parser.acceptsAll(Arrays.asList("pdb","p"), "Specify PDB directory").
                                            withOptionalArg().
                                            ofType(String.class);

        OptionSpec<Boolean> caOnlyArg = parser.acceptsAll(Arrays.asList("caonly"),"Parse CA atoms only")
                .withOptionalArg().ofType(Boolean.class).defaultsTo(true);

        OptionSpec<String> extractArg = parser.acceptsAll(Arrays.asList("extract","e"), "Directory for extracted structures").
                                            withOptionalArg().
                                            ofType(String.class);
        OptionSpec compressArg = parser.acceptsAll(Arrays.asList("compress","z"), "Compress generated structure files");

        OptionSpec skipPreprocessArg = parser.acceptsAll(Arrays.asList("skip-pre"), "Skip ID pre-processing");

        OptionSpec<String> chainArg = parser.acceptsAll(Arrays.asList("chain"), "Use first chain from ambiguous IDs").
                withRequiredArg().ofType(String.class).defaultsTo("first");

        // Alignment control
        OptionSpec<Integer> nprocArg = parser.acceptsAll(Arrays.asList("nproc","n"), "Number of threads").
                                            withRequiredArg().
                                            ofType(Integer.class).
                                            defaultsTo(Constants.NPROC_DEFAULT);
        // Output
        OptionSpec<String> logLevelArg = parser.acceptsAll(Arrays.asList("level"), "Log4J log level").
                withRequiredArg().ofType(String.class).defaultsTo("INFO");
        OptionSpec<Integer> infoArg = parser.acceptsAll(Arrays.asList("info","i"),"Print structure information with indicated verbosity").
                withOptionalArg().ofType(Integer.class).defaultsTo(0);
        OptionSpec<String> outfileArg = parser.acceptsAll(Arrays.asList("outfile","o"), "Output file").
                                            withRequiredArg().
                                            ofType(String.class);
        OptionSpec<Integer> distArg = parser.acceptsAll(Arrays.asList("distances","d"),"Generate distance matrix from list and alignment files").
                                            withRequiredArg().
                                            ofType(Integer.class);
        // Non-option arguments
        OptionSpec<String> nonOpts = parser.nonOptions("[<File>] [<Structure 1>] ... [<Structure N>]").ofType(String.class);
        // Help, parsing
        OptionSpec helpArg = parser.acceptsAll(Arrays.asList("h", "help", "?"), "Show this help and exit");
        OptionSet opts = parser.parse(args);

        if ( opts.has(helpArg) ){
            parser.printHelpOn( System.out );
            System.exit(0);
        }

        LogManager.getRootLogger().setLevel(Level.toLevel(opts.valueOf(logLevelArg)));

        List<String> nonOptArgs = new LinkedList<>(opts.valuesOf(nonOpts));
        List<String> fileArgs = new ArrayList<>();
        Iterator<String> it = nonOptArgs.iterator();

        // After this fileArgs has files, nonOptArgs has non-files (e.g. PDB IDs).
        while (it.hasNext()) {
            String noa = it.next();
            if (new File(noa).exists()) {
                it.remove();
                fileArgs.add(noa);
            }
        }

        int nStructArgs = fileArgs.size() + nonOptArgs.size();

        String algorithmName = null;
        ConfigStrucAligParams params = null;
        if ( opts.has(ceArg) ) {
            algorithmName = CeMain.algorithmName;
            params = new CeParameters();
        } else if ( opts.has(fatcatArg) ) {
            algorithmName = FatCatFlexible.algorithmName;
            params = new FatCatParameters();
        } else if ( opts.has(fatcatRigidArg) ) {
            algorithmName = FatCatRigid.algorithmName;
            params = new FatCatParameters();
        } else if ( opts.has(daliArg) ) {
            algorithmName = "Dali";
        }


        String pdbDir = System.getProperty("PDB_DIR");
        if (opts.has(dirArg)) {
            pdbDir = opts.valueOf(dirArg);
            System.setProperty("PDB_DIR", pdbDir);
        }

        UserConfiguration configuration = new UserConfiguration();
        configuration.setFileFormat(UserConfiguration.PDB_FORMAT);
        FileParsingParameters fileParams = new FileParsingParameters();
        fileParams.setHeaderOnly(false);
        fileParams.setParseCAOnly(opts.valueOf(caOnlyArg));
        fileParams.setAlignSeqRes(true);
        LocalPDBDirectory.ObsoleteBehavior obsoleteBehavior = LocalPDBDirectory.ObsoleteBehavior.FETCH_CURRENT;
        LocalPDBDirectory.FetchBehavior fetchBehavior = LocalPDBDirectory.FetchBehavior.FETCH_REMEDIATED;
        configuration.setFetchBehavior(fetchBehavior);
        configuration.setObsoleteBehavior(obsoleteBehavior);
        configuration.setPdbFilePath(pdbDir);
        configuration.setCacheFilePath(pdbDir);

        AtomCache cache = new AtomCache(configuration);
        cache.setFileParsingParams(fileParams);
        cache.setUseMmCif(false);

        Executor pool = Utility.createThreadPool(opts.valueOf(nprocArg));

        String extractDir = opts.hasArgument(extractArg) ? opts.valueOf(extractArg) : null;

        List<String> list2align = null;
        String root = null;
        if (nStructArgs > 0 || opts.has(listArg)) {
            List<String> inputList;
            if ( opts.has(listArg)) { // Using list
                inputList = Utility.listFromFile(opts.valueOf(listArg));
                inputList.addAll(nonOptArgs);
            } else {
                inputList = nonOptArgs;
            }
            logger.debug(inputList.size() + " items in input");

            list2align = Utility.standardizeIds(inputList);

            if (opts.has(infoArg)) { // Output info for structures
                for (String id : list2align) {
                    Utility.printStructureInfo(cache, id, opts.valueOf(infoArg));
                }
                System.exit(0);
            }

            if (opts.has(progressiveArg)) {
                root = Utility.standardizeId(opts.valueOf(progressiveArg));
                root = Utility.firstChainOnly(Arrays.asList(root), configuration.getPdbFilePath(), obsoleteBehavior).get(0);
                logger.debug("Root structure " + opts.valueOf(progressiveArg) + " parsed as " + root);
            }

            if (!opts.has(skipPreprocessArg)) {
                if (opts.valueOf(chainArg).toLowerCase().startsWith("f") || opts.valueOf(chainArg).equals("1")) {
                    logger.debug("Parsing first chain in input structures");
                    list2align = Utility.firstChainOnlyAsync(list2align, configuration.getPdbFilePath(), obsoleteBehavior, pool);
                } else {
                    logger.debug("Parsing all chains in input structures");
                    list2align = Utility.expandStructuresAsync(list2align, configuration.getPdbFilePath(), obsoleteBehavior, pool);
                }
            }

            if ( opts.has(extractArg) ) {
                extractDir = Utility.createExtractDir( extractDir );
                logger.debug("Extracting requested structures to " + extractDir);
                List<String> list2extract = new ArrayList<>(list2align);
                if (root != null) list2extract.add(root);
                Utility.extractStructures(list2extract, cache, extractDir, opts.has(compressArg));
                logger.debug("Done extracting");
            }

            Utility.listToFile("list.log", list2align);

            if (algorithmName != null) {
                JobSeries jobs;
                OutputHandler output;
                if (opts.has(progressiveArg)) {
                    list2align.remove(root);
                    jobs = new ProgressiveAlignmentJobSeries(list2align, root, cache, algorithmName, params);
                } else {
                    jobs = new PairwiseAlignmentJobSeries(list2align, cache, algorithmName, params);
                }
                if (opts.has(multipleArg)) {
                    logger.debug("Rooted alignment output file " + opts.valueOf(outfileArg));
                    output = new ProgressiveOutput(cache, root, opts.valueOf(outfileArg));
                } else {
                    logger.debug("Alignment summary output file " + opts.valueOf(outfileArg));
                    output = new SummaryOutput(opts.valueOf(outfileArg));
                }
                logger.debug("Running " + jobs.total() + " alignments with " + algorithmName);
                System.exit( Align.align(jobs, pool, output) );
            }

        }

        if (opts.has(distArg) && fileArgs.size()==1) { // Convert alignment output to distance matrix
//                double[] matrix = Data.distanceMatrix(list2align, fileArgs.get(0), opts.valueOf(distArg));
//                String[] strings = Data.doubles2strings(matrix);
//                if (opts.has(outfileArg)) {
//                    Utility.listToFile(opts.valueOf(outfileArg),Arrays.asList(strings));
//                } else {
//                    for (String s : strings) System.out.println(s);
//                }
            throw new NotImplementedException();
        }
        // Incorrect arguments if we made it here.
        System.exit(1);

	}
}
