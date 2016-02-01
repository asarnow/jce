# jce
Structure alignment and analysis using BioJava
<pre>
java -jar jce.jar --help
Non-option arguments:
[<File>] [<Structure 1>] ... [<Structure N>]

Option                               Description
------                               -----------
-?, -h, --help                       Show this help and exit
--CE, --ce                           Align with CE (default)
--Dali, --dali                       Align using DaliLite external binary
--FATCAT, --fatcat, --fc, --fcr      Align with FATCAT - rigid
--FATCAT-FLEX, --fatcat-flex, --fcf  Align with FATCAT_FLEX - flexible
--caonly [Boolean]                   Parse CA atoms only (default: true)
--chain                              Use first chain from ambiguous IDs
                                       (default: first)
-d, --distances <Integer>            Generate distance matrix from list and
                                       alignment files
-e, --extract                        Directory for extracted structures
-i, --info [Integer]                 Print structure information with
                                       indicated verbosity (default: 0)
-l, --list                           Specify file of IDs for pairwise
                                       alignments
--level                              Log4J log level (default: INFO)
-m, --multiple                       Multiple alignment
-n, --nproc <Integer>                Number of threads (default: 1)
-o, --outfile                        Output file
-p, --pdb                            Specify PDB directory
-r, --root                           Specify root for progressive alignment
--skip-pre                           Skip ID pre-processing
-z, --compress                       Compress generated structure files
</pre>
