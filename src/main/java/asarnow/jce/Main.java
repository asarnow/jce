package asarnow.jce; /**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 4/6/11
 * Time: 4:34 PM
 */

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Main {

	/**
	 * @param args Command line arguments
	 */
	public static void main(String[] args) throws IOException{
        OptionParser parser = new OptionParser();
        // Aligner selection
        OptionSpec fatcatArg = parser.acceptsAll(Arrays.asList("FATCAT","fatcat","fc"), "Align with FATCAT - flexible");
        OptionSpec fatcatRigidArg = parser.acceptsAll(Arrays.asList("FATCAT-rigid","fatcat-rigid","fcr"), "Align with FATCAT - rigid");
        OptionSpec ceArg = parser.acceptsAll(Arrays.asList("CE","ce"), "Align with CE (default)");
        OptionSpec daliArg = parser.acceptsAll(Arrays.asList("Dali","dali"),"Align using DaliLite external binary");
        // Aligner specific options
        // CE

        // FATCAT


        // Structure selection
        OptionSpec<String> listArg = parser.acceptsAll(Arrays.asList("list", "l"), "Specify file of IDs for pairwise alignments").
                                            requiredIf(daliArg).
                                            withRequiredArg().
                                            ofType(String.class);
        // PDB files
        OptionSpec<String> dirArg = parser.acceptsAll(Arrays.asList("pdb","p"), "Specify PDB directory").
                                            withRequiredArg().
                                            ofType(String.class).
                                            defaultsTo(Constants.CWD);
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

        List<String> noas = new LinkedList<>(opts.valuesOf(nonOpts));
        List<String> noafiles = new ArrayList<>();
        Iterator<String> it = noas.iterator();
        while (it.hasNext()) {
            String noa = it.next();
            if (new File(noa).exists()) {
                it.remove();
                noafiles.add(noa);
            }
        }

        int alignerFlag = Constants.NOALIGN;
        if ( opts.has(ceArg) ) {
            alignerFlag = Constants.CE;
        } else if ( opts.has(fatcatArg) ) {
            alignerFlag = Constants.FATCAT;
        } else if ( opts.has(fatcatRigidArg) ) {
            alignerFlag = Constants.FATCAT_RIGID;
        } else if ( opts.has(daliArg) ) {
            alignerFlag = Constants.DALI;
        }

        String extractDir = null;
        List<String> list2align = null;
        if ( opts.has(listArg)) { // Using list
            if ( opts.has(extractArg) ) {
                extractDir = Data.createExtractDir( opts.hasArgument(extractArg) ? opts.valueOf(extractArg) : null );
                List<String> list = Utility.listFromFile(opts.valueOf(listArg));
                list2align = Data.extractStructures(list,
                        opts.valueOf(dirArg),
                        opts.has(unifiedPDB),
                        Utility.createFileParsingParameters(!opts.has(parseAllAtomsArg)),
                        extractDir,
                        opts.has(compressArg));
            } else {
                list2align = Utility.listFromFile(opts.valueOf(listArg));
            }
        }

        String pdbDir = opts.has(extractArg) ? extractDir : opts.valueOf(dirArg);

        if ( alignerFlag != Constants.NOALIGN ) {
            if ( opts.has(listArg) && noas.size()==0 ) {
                System.exit( Align.alignList( list2align, pdbDir, opts.valueOf(outfileArg),
                                !(opts.has(extractArg) || opts.has(unifiedPDB)),
                                opts.valueOf(nprocArg), alignerFlag ) );
//            } else if ( opts.has(listArg) && noas.size()>0 ) {
                //TODO alignLists(noas, list2align, ...);
            } else if (noas.size()==2) {
                System.exit( Align.alignPair(noas.get(0),noas.get(1), pdbDir, !(opts.has(unifiedPDB) || pdbDir==null), alignerFlag) );
            } else if (noas.size()>2) {
                System.exit( Align.alignList( noas, pdbDir, opts.valueOf(outfileArg),
                        false, opts.valueOf(nprocArg), alignerFlag ) );
            }
        } else { // No alignment tasks
            if (opts.has(distArg) && noafiles.size()==1) { // Convert alignment output to distance matrix
                double[] matrix = Data.distanceMatrix(list2align, noafiles.get(0), opts.valueOf(distArg));
                String[] strings = Data.doubles2strings(matrix);
                if (opts.has(outfileArg)) {
                    Utility.listToFile(opts.valueOf(outfileArg),Arrays.asList(strings));
                } else {
                    for (String s : strings) System.out.println(s);
                }
                System.exit(0);
            } else if (opts.has(infoArg)) { // Output info for structures
                Data.printStructureInfo(noas, pdbDir, opts.has(unifiedPDB), Utility.createFileParsingParameters());
                System.exit(0);
            }
        }
        // Incorrect arguments if we made it here.
        System.exit(1);

	}
}
