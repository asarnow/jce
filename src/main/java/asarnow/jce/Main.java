package asarnow.jce; /**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 4/6/11
 * Time: 4:34 PM
 */

import asarnow.jce.io.OutputHandler;
import asarnow.jce.io.ProgressiveOutput;
import asarnow.jce.io.SummaryOutput;
import asarnow.jce.job.AlignmentJob;
import asarnow.jce.job.JobSeries;
import asarnow.jce.job.PairwiseAlignmentJobSeries;
import asarnow.jce.job.ProgressiveAlignmentJobSeries;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.biojava.nbio.structure.align.ce.CeMain;
import org.biojava.nbio.structure.align.ce.CeParameters;
import org.biojava.nbio.structure.align.ce.ConfigStrucAligParams;
import org.biojava.nbio.structure.align.fatcat.FatCatFlexible;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.fatcat.calc.FatCatParameters;
import org.biojava.nbio.structure.align.util.AtomCache;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

	/**
	 * @param args Command line arguments
	 */
	public static void main(String[] args) throws IOException{
        OptionParser parser = new OptionParser();
        // Aligner selection
        OptionSpec fatcatArg = parser.acceptsAll(Arrays.asList("FATCAT-FLEX","fatcat-flex","fcf"), "Align with FATCAT_FLEX - flexible");
        OptionSpec fatcatRigidArg = parser.acceptsAll(Arrays.asList("FATCAT","fatcat","fc","fcr"), "Align with FATCAT - rigid");
        OptionSpec ceArg = parser.acceptsAll(Arrays.asList("CE","ce"), "Align with CE (default)");
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
        OptionSpec unifiedPDB = parser.acceptsAll(Arrays.asList("unified","u"), "Use unified PDB directory");
        OptionSpec parseAllAtomsArg = parser.acceptsAll(Arrays.asList("parseall"),"Parse all atoms (default: parse CA only)");

        OptionSpec<String> extractArg = parser.acceptsAll(Arrays.asList("extract","e"), "Directory for extracted structures").
                                            withOptionalArg().
                                            ofType(String.class);
        OptionSpec compressArg = parser.acceptsAll(Arrays.asList("compress","c"), "Compress extracted structures");

        // Alignment control
        OptionSpec<Integer> nprocArg = parser.acceptsAll(Arrays.asList("nproc","n"), "Number of threads").
                                            withRequiredArg().
                                            ofType(Integer.class).
                                            defaultsTo(Constants.NPROC_DEFAULT);
        // Output
        OptionSpec infoArg = parser.acceptsAll(Arrays.asList("info","i"),"Print information about structures listed on command line");
        OptionSpec<String> outfileArg = parser.acceptsAll(Arrays.asList("outfile","o"), "output file").
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

//        String extractDir = null;
//        List<String> list2align = null;
//        if ( opts.has(listArg)) { // Using list
//            if ( opts.has(extractArg) ) {
//                extractDir = Data.createExtractDir( opts.hasArgument(extractArg) ? opts.valueOf(extractArg) : null );
//                List<String> list = Utility.listFromFile(opts.valueOf(listArg));
//                list2align = Data.extractStructures(list,
//                        opts.valueOf(dirArg),
//                        opts.has(unifiedPDB),
//                        Utility.createFileParsingParameters(!opts.has(parseAllAtomsArg)),
//                        extractDir,
//                        opts.has(compressArg));
//            } else {
//                list2align = Utility.listFromFile(opts.valueOf(listArg));
//            }
//        }

//        String pdbDir = opts.has(extractArg) ? extractDir : opts.valueOf(dirArg);
        AtomCache cache;
        if (opts.has(dirArg)) {
            cache = Utility.initAtomCache(opts.valueOf(dirArg));
        } else {
            cache = Utility.initAtomCache(Constants.CWD);
        }

        JobSeries<AlignmentJob> jobs;
        OutputHandler output;

        if ( algorithmName != null ) {
            if ( opts.has(listArg) && nonOptArgs.size() == 0 ) { // Just a list.
                List<String> list2align = Utility.listFromFile(opts.valueOf(listArg));
                Utility.standardizeIds(list2align);
                if (opts.has(multipleArg)) {
                    String root = Utility.standardizeId(opts.valueOf(progressiveArg));
                    if (list2align.contains(root)) list2align.remove(root);
                    jobs = new ProgressiveAlignmentJobSeries(list2align, root, cache, algorithmName, params);
                    output = new ProgressiveOutput(cache, root, opts.valueOf(outfileArg));
                } else {
                    jobs = new PairwiseAlignmentJobSeries(list2align, cache, algorithmName, params);
                    output = new SummaryOutput(opts.valueOf(outfileArg));
                }
                System.exit( Align.align(jobs, Utility.createThreadPool(opts.valueOf(nprocArg)), output) );
            } else if (nStructArgs == 2) { // Just a pair.
                throw new NotImplementedException();
            } else if (nStructArgs > 2) { // Multiple ID/file arguments.
                throw new NotImplementedException();
            }
        } else { // No alignment tasks
            if (opts.has(distArg) && fileArgs.size()==1) { // Convert alignment output to distance matrix
//                double[] matrix = Data.distanceMatrix(list2align, fileArgs.get(0), opts.valueOf(distArg));
//                String[] strings = Data.doubles2strings(matrix);
//                if (opts.has(outfileArg)) {
//                    Utility.listToFile(opts.valueOf(outfileArg),Arrays.asList(strings));
//                } else {
//                    for (String s : strings) System.out.println(s);
//                }
                throw new NotImplementedException();
            } else if (opts.has(infoArg)) { // Output info for structures
                throw new NotImplementedException();
            }
        }
        // Incorrect arguments if we made it here.
        System.exit(1);

	}
}
