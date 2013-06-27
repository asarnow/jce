package asarnow.jce; /**
 * Created by IntelliJ IDEA.
 * User: da
 * Date: 4/6/11
 * Time: 4:34 PM
 */

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.biojava.bio.structure.*;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.StructureAlignmentFactory;
import org.biojava.bio.structure.align.ce.CeMain;
import org.biojava.bio.structure.align.ce.CeParameters;
import org.biojava.bio.structure.align.fatcat.FatCatFlexible;
import org.biojava.bio.structure.align.fatcat.FatCatRigid;
import org.biojava.bio.structure.align.fatcat.calc.FatCatParameters;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.io.FileParsingParameters;
import org.biojava.bio.structure.io.PDBFileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

//import org.biojava.bio.structure.align.model.AfpChainWriter;
//import org.biojava.bio.structure.align.util.AtomCache;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
//		String pdbFilePath = args[0];
//		String pdb1 = args[1];
//		String pdb2 = args[2];

        OptionParser parser = new OptionParser();
        OptionSpec fatcatArg = parser.acceptsAll(Arrays.asList("FATCAT","fatcat","fc"), "Align with FATCAT - flexible");
        OptionSpec fatcatRigidArg = parser.acceptsAll(Arrays.asList("FATCAT-rigid","fatcat-rigid","fcr"), "Align with FATCAT - rigid");
        OptionSpec ceArg = parser.acceptsAll(Arrays.asList("CE","ce"), "Align with CE (default)");
        OptionSpec<String> listArg = parser.acceptsAll(Arrays.asList("list","l"), "Specify file of IDs for pairwise alignments").withRequiredArg().ofType(String.class);
        OptionSpec<String> dirArg = parser.acceptsAll(Arrays.asList("pdb","p"), "Specify PDB directory").withRequiredArg().ofType(String.class);
        OptionSpec<String> outfileArg = parser.acceptsAll(Arrays.asList("outfile","o"), "Output file for pairwise alignments").withRequiredArg().ofType(String.class);
        OptionSpec<Integer> nprocArg = parser.acceptsAll(Arrays.asList("nproc","n"), "Number of threads").withRequiredArg().ofType(Integer.class);
        OptionSpec<Boolean> pdbAllArg = parser.acceptsAll(Arrays.asList("divided"), "Use divided PDB directory (boolean, default true)").withRequiredArg().ofType(Boolean.class).defaultsTo(true);
        OptionSpec helpArg = parser.acceptsAll( Arrays.asList("h","help","?"), "Show this help" );
        OptionSet opts = parser.parse(args);

        if ( opts.has(helpArg) ){
            parser.printHelpOn( System.out );
            System.exit(0);
        }

        int alignerFlag = Constants.CE; // use ce
        if ( opts.has(fatcatArg) ) {
            alignerFlag = Constants.FATCAT; // use fatcat
        } else if ( opts.has(fatcatRigidArg) ) {
            alignerFlag = Constants.FATCAT_RIGID; // use fatcat_rigid
        }

        if ( opts.has(listArg) && opts.has(dirArg) ) {
            System.exit(
                    alignList(
                            opts.valueOf(listArg),
                            opts.valueOf(dirArg),
                            opts.valueOf(outfileArg),
                            opts.valueOf(pdbAllArg),
                            opts.valueOf(nprocArg),
                            alignerFlag)
                        );
        } else if ( opts.has(listArg) ) {
            // must have dir if using a list
            System.exit(1);
        }

        if ( opts.has(dirArg) ) {
            System.exit( alignPair(opts, opts.valueOf(dirArg), opts.valueOf(pdbAllArg), alignerFlag) );
        } else {
            System.exit( alignPair(opts, null, false, alignerFlag) );
        }
	}

    private static int alignPair(OptionSet opts, String pdbDir, boolean divided, int alignerFlag){
        String pdb1 = opts.nonOptionArguments().get(0);
        String pdb2 = opts.nonOptionArguments().get(2);
        String chainId1 = opts.nonOptionArguments().get(1);
        String chainId2 = opts.nonOptionArguments().get(3);

        FileParsingParameters fparams = new FileParsingParameters();
        fparams.setParseCAOnly(true);

//		AtomCache cache = new AtomCache(pdbFilePath, false);
//		cache.setFileParsingParameters(fparams);

        PDBFileReader reader = new PDBFileReader();
        if (pdbDir != null) reader.setPath(pdbDir);
        reader.setPdbDirectorySplit(divided);
        reader.setFileParsingParameters(fparams);

        Structure structure1;
        Structure structure2;

        try {
//			structure1 = cache.getStructure(pdb1);
//			structure2 = cache.getStructure(pdb2);

            if (pdbDir != null) {
                structure1 = reader.getStructureById(pdb1);
                structure2 = reader.getStructureById(pdb2);
            } else {
                structure1 = reader.getStructure(pdb1);
                structure2 = reader.getStructure(pdb2);
            }

            pdb1 = structure1.getPDBCode();
            pdb2 = structure2.getPDBCode();

            structure1 = new StructureImpl(structure1.getChainByPDB(chainId1));
            structure2 = new StructureImpl(structure2.getChainByPDB(chainId2));

            Atom[] ca1 = StructureTools.getAtomCAArray(structure1);
            Atom[] ca2 = StructureTools.getAtomCAArray(structure2);

            AFPChain afpChain = null;

            switch ( alignerFlag ) {
//                case Constants.CE: afpChain = useCe(ca1, ca2); break;
                case Constants.FATCAT: afpChain = useFatcat(ca1, ca2); break;
                case Constants.FATCAT_RIGID: afpChain = useFatcatRigid(ca1, ca2); break;
                default: afpChain = useCe(ca1, ca2); break;
            }

//			afpChain.setName1(pdb1);
//			afpChain.setName2(pdb2);

            System.out.println( pdb1 + chainId1 + '\t' +
                    pdb2 + chainId2 + '\t' +
                    Double.toString(afpChain.getTotalRmsdOpt()) + '\t' +
                    Double.toString(afpChain.getAlignScore()) + '\t' +
                    Double.toString(afpChain.getProbability()) + '\t' +  // for fatcat, not a z-score!
                    Integer.toString(afpChain.getNrEQR()) + '\t' +
                    Double.toString(afpChain.getSimilarity())
            );

//			System.out.println(AfpChainWriter.toWebSiteDisplay(afpChain, ca1, ca2));
//			System.out.println(AfpChainWriter.toScoresList(afpChain));
//			System.out.println(afpChain.toRotMat());

        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    private static int alignList(String idsFile, String pdbDir, String outFile, Boolean divided, Integer nproc, int alignerFlag) /*throws IOException, InterruptedException*/ {
        List<String> idsRaw = Utility.listFromFile(idsFile);
        List<String> ids = new ArrayList<String>();

        int l = 0;
        for (String id : idsRaw){
            if ( Utility.pdbFileExists(id, pdbDir, divided) ) {
                ids.add(id);
                l++;
    }
}

        System.out.println(String.valueOf(idsRaw.size() - l) + " ids removed due to missing file");

        AtomCache cache = Utility.initAtomCache(pdbDir, divided);

        BlockingQueue<String> outputQueue = new ArrayBlockingQueue<String>(128);

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                nproc, // core size
                nproc, // max size
                60, // idle timeout
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(4096),
                new ThreadPoolExecutor.CallerRunsPolicy() ); // If we have to reject a task, run it in the calling thread.

        Output outputRunnable = new Output(outputQueue, outFile);
        Thread outputThread = new Thread( outputRunnable );
        outputThread.start();

        int k = 0;
        for (int i=0; i<ids.size(); i++){
            for (int j=i+1; j<ids.size(); j++){
                threadPool.execute(new Align(cache, ids.get(i), ids.get(j), alignerFlag, outputQueue));
//                System.out.println(++k);
                k++;
            }
        }

        System.out.println(String.valueOf(k) + " pairs to align");

        threadPool.shutdown();
        try {
        threadPool.awaitTermination(21, TimeUnit.DAYS);
        outputQueue.put("STOP");
        outputThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace(); //TODO Auto-generated catch block
        }

        return 0;
    }

    protected static AFPChain useFatcat(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(FatCatFlexible.algorithmName);
        FatCatParameters params = new FatCatParameters();
//	    params.setShowAFPRanges(true);
        return aligner.align(ca1, ca2, params);
    }

    protected static AFPChain useCe(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(CeMain.algorithmName);
        CeParameters params = new CeParameters();
        return aligner.align(ca1, ca2, params);
    }

    protected static AFPChain useFatcatRigid(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);
        FatCatParameters params = new FatCatParameters();
//	    params.setShowAFPRanges(true);
        return aligner.align(ca1, ca2, params);
    }

}
