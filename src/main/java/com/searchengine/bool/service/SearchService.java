package com.searchengine.bool.service;

import com.searchengine.bool.anootation.LoggingAfter;
import com.searchengine.bool.anootation.LoggingBefore;
import com.searchengine.bool.config.SearchConfig;
import com.searchengine.bool.web.controller.CachedResult;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Service Layer for find satysfying
 * the search criteria
 *
 * Created by IntelliJ IDEA.
 * User: entrix
 * Date: 16.03.2012
 * Time: 23:13
 * To change this template use File | Settings | File Templates.
 */
public class SearchService {

    private static Logger logger = LogManager.getLogger(SearchService.class);

    private static IndexReader reader;
    private static IndexSearcher searcher;
    private static Analyzer analyzer;
    private static boolean isInit = false;
    
//    static {
//
//    }


    @LoggingBefore("initialize SearchService")
    @LoggingAfter("SearchService initialized")
    public static void initialize() throws IOException {
        reader = DirectoryReader.open(
                FSDirectory.open(new File(SearchConfig.getInstance().getProperty("search.index"))));
        searcher = new IndexSearcher(reader);
        analyzer = new StandardAnalyzer(Version.LUCENE_43);
        CachedResult.setIndexSearcher(searcher);
        isInit = true;
    }

    public boolean isInit() {
        return isInit;
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    public static ScoreDoc[] findDocuments(String queryStr) throws Exception {
        String field = SearchConfig.getInstance().getProperty("search.field");
        String queries = null;
        int repeat = 0;
        boolean raw = false;
        String queryString = null;
        int hitsPerPage = 10;


        BufferedReader in = null;

        in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        QueryParser parser = new QueryParser(Version.LUCENE_40, field, analyzer);


        String line = queryStr;


        line = line.trim();
        if (line.length() == 0) {
            return null;
        }

        Query query = parser.parse(line);
        CachedResult.queryL = query;
        System.out.println("Searching for: " + query.toString(field));

//        if (repeat > 0) {                           // repeat & time as benchmark
//            Date start = new Date();
//            for (int i = 0; i < repeat; i++) {
//                searcher.search(query, null, 100);
//            }
//            Date end = new Date();
//            System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
//        }


        TopDocs results = searcher.search(query, 5 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;

        System.out.println(numTotalHits + " total matching documents");
        if (numTotalHits == 0) {
            return new ScoreDoc[0];
        }

        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);
        hits = searcher.search(query, numTotalHits).scoreDocs;
        end = Math.min(hits.length, start + hitsPerPage);
        for (int i = start; i < end; i++) {
            if (raw) {                              // output raw format
                System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
                continue;
            }

            Document doc = searcher.doc(hits[i].doc);
            String path = doc.get("path");
            if (path != null) {
                System.out.println((i+1) + ". " + path);
                String title = doc.get("title");
                if (title != null) {
                    System.out.println("   Title: " + doc.get("title"));
                }
            } else {
                System.out.println((i+1) + ". " + "No path for this document");
            }

        }

//        CachedResult.setIndexSearcher(searcher);

        //reader.close();
        //TODO loosing searcher resourser cause it's needed for geting docs in cashed res

        return hits;
    }


    @LoggingBefore("Creating index")
    @LoggingAfter("Index created")
    public static void createIndex() throws Exception {

        logger.info("Creating index");
        System.out.println("Creating index");
        /** Index all text files under a directory. */
        String usage = "java org.apache.lucene.demo.IndexFiles"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles";
        String indexPath = "index";
        String docsPath = null;
        boolean create = true;

        indexPath = SearchConfig.getInstance().getProperty("search.index");

        docsPath = SearchConfig.getInstance().getProperty("document.dir");
        create = false;

        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            logger.error("Document directory '" + docDir.getAbsolutePath() +
                    "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        logger.info("Indexing to directory '" + indexPath + "'...");

        Directory dir = null;

        dir = FSDirectory.open(new File(indexPath));
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);

        if (create) {
            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            // Add new documents to an existing index:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        // Optional: for better indexing performance, if you
        // are indexing many documents, increase the RAM
        // buffer.  But if you do this, increase the max heap
        // size to the JVM (eg add -Xmx512m or -Xmx1g):
        //
        // iwc.setRAMBufferSizeMB(256.0);

        IndexWriter writer = null;

        writer = new IndexWriter(dir, iwc);
        indexDocs(writer, docDir);

        // NOTE: if you want to maximize search performance,
        // you can optionally call forceMerge here.  This can be
        // a terribly costly operation, so generally it's only
        // worth it when your index is relatively static (ie
        // you're done adding documents to it):
        //
        // writer.forceMerge(1);
        writer.close();

        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
        logger.info("Index created");
    }

    /**
     * Indexes the given file using the given writer, or if a directory is given,
     * recurses over files and directories found under the given directory.
     *
     * NOTE: This method indexes one document per input file.  This is slow.  For good
     * throughput, put multiple documents into your input file(s).  An example of this is
     * in the benchmark module, which can create "line doc" files, one document per line,
     * using the
     * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
     * >WriteLineDocTask</a>.
     *
     * @param writer Writer to the index where the given file/dir info will be stored
     * @param file The file to index, or the directory to recurse into to find files to index
     * @throws Exception If there is a low-level I/O error
     */
    private static void indexDocs(IndexWriter writer, File file)
            throws Exception {
        // do not try to index files that cannot be read
        if (file.canRead()) {
            if (file.isDirectory()) {
                String[] files = file.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(writer, new File(file, files[i]));
                    }
                }
            } else {

                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException fnfe) {
                    // at least on windows, some temporary files raise this exception with an "access denied" message
                    // checking if the file can be read doesn't help
                    return;
                }

                try {

                    // make a new, empty document
                    Document doc = new Document();

                    // Add the path of the file as a field named "path".  Use a
                    // field that is indexed (i.e. searchable), but don't tokenize
                    // the field into separate words and don't index term frequency
                    // or positional information:
                    Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
                    doc.add(pathField);

                    // Add the last modified date of the file a field named "modified".
                    // Use a LongField that is indexed (i.e. efficiently filterable with
                    // NumericRangeFilter).  This indexes to milli-second resolution, which
                    // is often too fine.  You could instead create a number based on
                    // year/month/day/hour/minutes/seconds, down the resolution you require.
                    // For example the long value 2011021714 would mean
                    // February 17, 2011, 2-3 PM.
                    doc.add(new LongField("modified", file.lastModified(), Field.Store.NO));

                    // Add the contents of the file to a field named "contents".  Specify a Reader,
                    // so that the text of the file is tokenized and indexed, but not stored.
                    // Note that FileReader expects the file to be in UTF-8 encoding.
                    // If that's not the case searching for special characters will fail.
                    doc.add(new Field("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8")), Field.TermVector.WITH_POSITIONS_OFFSETS));

                    if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                        // New index, so we just add the document (no old document can be there):
                        System.out.println("adding " + file);
                        writer.addDocument(doc);
                    } else {
                        // Existing index (an old copy of this document may have been indexed) so
                        // we use updateDocument instead to replace the old one matching the exact
                        // path, if present:
                        logger.info("updating " + file);
                        writer.updateDocument(new Term("path", file.getPath()), doc);
                    }

                } finally {
                    fis.close();
                }
            }
        }
    }

    @LoggingBefore("start updateIndex")
    synchronized public static void updateIndex() {

    }
}
