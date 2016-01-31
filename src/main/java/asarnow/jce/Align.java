package asarnow.jce;

import asarnow.jce.io.OutputHandler;
import asarnow.jce.job.AlignmentJob;
import asarnow.jce.job.JobSeries;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.ce.CeMain;
import org.biojava.nbio.structure.align.ce.CeParameters;
import org.biojava.nbio.structure.align.ce.ConfigStrucAligParams;
import org.biojava.nbio.structure.align.fatcat.FatCatFlexible;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.fatcat.calc.FatCatParameters;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileReader;

import java.util.concurrent.*;

/**
 * @author Daniel Asarnow
 */
public class Align {

//    static final AFPChain POISON_PILL = new AFPChain() {{ setId(-1); }};

    static int alignPair(String pdb1, String pdb2, String pdbDir, boolean divided, int alignerFlag){
        //TODO use AtomCache and getStructure
        String chainId1 = pdb1.substring(4);
        String chainId2 = pdb2.substring(4);

        FileParsingParameters fparams = new FileParsingParameters();
        fparams.setParseCAOnly(true);

//		AtomCache cache = new AtomCache(pdbFilePath, false);
//		cache.setFileParsingParameters(fparams);

        PDBFileReader reader = new PDBFileReader();
        if (pdbDir != null) reader.setPath(pdbDir);
//        reader.setPdbDirectorySplit(divided);
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

            AFPChain afpChain;

            switch ( alignerFlag ) {
//                case Constants.CE: afpChain = useCe(ca1, ca2); break;
                case Constants.FATCAT_FLEX: afpChain = useFatcat(ca1, ca2); break;
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

    public static int align(JobSeries<AlignmentJob> jobs, Executor pool, OutputHandler output) {
        CompletionService<AlignmentJob> exec = new ExecutorCompletionService<>(Executors.newSingleThreadExecutor());
        CompletionService<AFPChain> alignmentService = new ExecutorCompletionService<>(pool);
        int queued = 0;
        while (jobs.hasNext()) {
            exec.submit(jobs.next());
            queued++;
        }
        int aligned = 0;
        int parsed = 0;
        Future<AFPChain> futureAlignment = null;
        Future<AlignmentJob> futureJob = null;
        while (aligned + parsed < queued) {
            try {
                futureJob = exec.poll(250, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (futureJob != null) {
                try {
                    alignmentService.submit(futureJob.get());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    parsed++;
                }
            }
            try {
                futureAlignment = alignmentService.poll(250, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (futureAlignment != null) {
                try {
                    AFPChain afpChain = futureAlignment.get();
                    // queue.put(afpChain)
                    output.handle(afpChain);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    aligned++;
                }
            }
        }
        output.close();
        return 0;
    }

//    static int alignList(List<String> ids, String pdbDir, String outFile, Boolean divided, Integer nproc, int alignerFlag) /*throws IOException, InterruptedException*/ {
//        BlockingQueue<AFPChain> outputQueue = new ArrayBlockingQueue<>(128);
//
//        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
//                nproc, // core size
//                nproc, // max size
//                60, // idle timeout
//                TimeUnit.SECONDS,
//                new ArrayBlockingQueue<Runnable>(4096, true), // Fairness = true for FIFO
//                new ThreadPoolExecutor.CallerRunsPolicy() ); // If we have to reject a task, run it in the calling thread.
//
//        JobSeries jobSeries;
//        Thread outputThread;
//        switch (alignerFlag) {
//            case Constants.DALI:
////                DaliImportDCCP daliImportDCCPJob = new DaliImportDCCP(outputQueue, outFile);
////                outputThread = new Thread(daliImportDCCPJob);
////                jobSeries = new DaliListAlignmentJobSeries(ids, outputQueue);
////                break;
//                throw new NotImplementedException();
//            default:
//                AtomCache cache = Utility.initAtomCache(pdbDir, divided);
//                SummaryOutput summaryOutputJobRunnable = new SummaryOutput(outputQueue, outFile);
//                outputThread = new Thread(summaryOutputJobRunnable);
//                jobSeries = new PairwiseAlignmentJobSeries(ids, alignerFlag, cache, outputQueue);
//                break;
//        }
//
//        runJobs(jobSeries, threadPool, outputThread, outputQueue);
//
//        return 0;
//    }
//
//    static int alignProgressive(List<String> ids, String root, String pdbDir, String outFile, Integer nproc, int alignerFlag) {
//        BlockingQueue<AFPChain> outputQueue = new ArrayBlockingQueue<>(128);
//
//        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
//                nproc, // core size
//                nproc, // max size
//                60, // idle timeout
//                TimeUnit.SECONDS,
//                new ArrayBlockingQueue<Runnable>(4096, true), // Fairness = true for FIFO
//                new ThreadPoolExecutor.CallerRunsPolicy() ); // If we have to reject a task, run it in the calling thread.
//
//        JobSeries jobSeries;
//        Thread outputThread;
//        switch (alignerFlag) {
//            case Constants.DALI:
//                throw new NotImplementedException();
//            default:
//                AtomCache cache = Utility.initAtomCache(pdbDir, true);
//                ProgressiveOutput outputJob = new ProgressiveOutput(cache, root, outputQueue, outFile);
//                outputThread = new Thread(outputJob);
//                jobSeries = new ProgressiveAlignmentJobSeries(ids, root, alignerFlag, cache, outputQueue);
//                break;
//        }
//
//        runJobs(jobSeries, threadPool, outputThread, outputQueue);
//
//        return 0;
//    }
//
//    static void runJobs(JobSeries jobSeries, ThreadPoolExecutor threadPool, Thread outputThread, BlockingQueue<AFPChain> outputQueue) {
//        outputThread.start();
//        // threadPool.execute( outputJob );
//
//        while (jobSeries.hasNext()) {
//            Runnable job = jobSeries.next();
//            if (job!=null) threadPool.execute(job);
//        }
//
//        // threadPool.execute( new PoisonPill() );
//
//        threadPool.shutdown();
//        try {
//            threadPool.awaitTermination(21, TimeUnit.DAYS);
//            outputQueue.put(POISON_PILL);
//            outputThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    public static AFPChain useFatcat(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(FatCatFlexible.algorithmName);
        FatCatParameters params = new FatCatParameters();
//	    params.setShowAFPRanges(true);
        return aligner.align(ca1, ca2, params);
    }

    public static AFPChain useFatcat(Atom[] ca1, Atom[] ca2, ConfigStrucAligParams params) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(FatCatFlexible.algorithmName);
        return aligner.align(ca1, ca2, params);
    }

    public static AFPChain useCe(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(CeMain.algorithmName);
        CeParameters params = new CeParameters();
        return aligner.align(ca1, ca2, params);
    }

    public static AFPChain useCe(Atom[] ca1, Atom[] ca2, ConfigStrucAligParams params) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(CeMain.algorithmName);
        return aligner.align(ca1, ca2, params);
    }

    public static AFPChain useFatcatRigid(Atom[] ca1, Atom[] ca2) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);
        FatCatParameters params = new FatCatParameters();
        return aligner.align(ca1, ca2, params);
    }

    public static AFPChain useFatcatRigid(Atom[] ca1, Atom[] ca2, ConfigStrucAligParams params) throws StructureException {
        StructureAlignment aligner = StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);
        return aligner.align(ca1, ca2, params);
    }
}
