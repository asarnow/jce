/**
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
import org.biojava.bio.structure.io.FileParsingParameters;
import org.biojava.bio.structure.io.PDBFileReader;

import java.io.IOException;
import java.util.Arrays;

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
        OptionSpec helpArg = parser.acceptsAll( Arrays.asList("h","help","?"), "Show this help" );
        OptionSet opts = parser.parse(args);

        if ( opts.has(helpArg) ){
            parser.printHelpOn( System.out );
            System.exit(0);
        }

		String pdb1 = opts.nonOptionArguments().get(1);
		String pdb2 = opts.nonOptionArguments().get(3);
		String chainId1 = opts.nonOptionArguments().get(2);
		String chainId2 = opts.nonOptionArguments().get(4);

		FileParsingParameters fparams = new FileParsingParameters();
		fparams.setParseCAOnly(true);

//		AtomCache cache = new AtomCache(pdbFilePath, false);
//		cache.setFileParsingParameters(fparams);

		PDBFileReader reader = new PDBFileReader();
		reader.setFileParsingParameters(fparams);

		Structure structure1;
		Structure structure2;

		try {
//			structure1 = cache.getStructure(pdb1);
//			structure2 = cache.getStructure(pdb2);

			structure1 = reader.getStructure(pdb1);
			structure2 = reader.getStructure(pdb2);

			pdb1 = structure1.getPDBCode();
			pdb2 = structure2.getPDBCode();

			structure1 = new StructureImpl(structure1.getChainByPDB(chainId1));
			structure2 = new StructureImpl(structure2.getChainByPDB(chainId2));

			Atom[] ca1 = StructureTools.getAtomCAArray(structure1);
			Atom[] ca2 = StructureTools.getAtomCAArray(structure2);

            AFPChain afpChain = null;

            if ( opts.has(fatcatArg) ) {
                afpChain = useFatcat(ca1, ca2);
            } else if ( opts.has(ceArg) ) {
                afpChain = useCe(ca1, ca2);
            } else if ( opts.has(fatcatRigidArg) ) {
                afpChain = useFatcatRigid(ca1, ca2);
            } else {
                afpChain = useCe(ca1, ca2);
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
			return;
		}


	}

    private static AFPChain useFatcat(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(FatCatFlexible.algorithmName);
        FatCatParameters params = new FatCatParameters();
//	    params.setShowAFPRanges(true);
        return aligner.align(ca1, ca2, params);
    }

    private static AFPChain useCe(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(CeMain.algorithmName);
        CeParameters params = new CeParameters();
        return aligner.align(ca1, ca2, params);
    }

    private static AFPChain useFatcatRigid(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);
        FatCatParameters params = new FatCatParameters();
//	    params.setShowAFPRanges(true);
        return aligner.align(ca1, ca2, params);
    }

}
