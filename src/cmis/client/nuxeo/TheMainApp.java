/*
 * IMPORTANT: This Java application is put on GitHub mainly for backup purpose :->
 *
 * The goal is to check the speed of some CMIS requests to Nuxeo, because we had
 * a question (on answers.nuxeo.com) on this topic, with somebody having performance
 * troubles for simple requests.
 *
 * For testing, I built a localhost:8080 nuxeo application with 31,000 "File" document
 * which is not a huge number, but the question was about having "some thousands of
 * documents". I used nuxeo-bulk-importer and its randomImporter to create random
 * documents. Note that for the "getObjectByPath()" query, I had to move some
 * of the existing documents so they were all in the "other" folder
 *      => You sure need to adapt the hard-coded names/paths if you want to use
 *         this app for your own testing
 *
 * This is a quick and dirty sample java application. Just open it from Eclipse and:
 *      * Update the hard-coded doc names and paths if needed
 *      * Setup the test parameters (kSAME_DOC_FOR_ALL, ...)
 *      * Run
 *      * Check the output to get the results
 */


/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Author:
 *     Thibaud Arguillere (Nuxeo), 2014-02-09
 */

package cmis.client.nuxeo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class TheMainApp {

    private static String [] _docNames = null;
    private static int _docNamesCount = -1;
    private static int _docNamesMaxForRandom = -1;

    private static String [] _docNamesRenamed = null;
    private static int _docNamesRenamedCount = -1;

    private static String [] _docNamesToUse = null;
    private static int _docNamesCountToUse = 0;

    private static String _docPathToUse = "";

    /*  Simple example for parsing the result set
    ItemIterable<QueryResult> results = session.query("SELECT dc:title FROM cmis:document where dc:title <> 'file-0-0", true);
    for(QueryResult hit: results) {
        for(PropertyData<?> property: hit.getProperties()) {
            String queryName = property.getQueryName();
            Object value = property.getFirstValue();
            System.out.println(queryName + ": " + value);
        }
        System.out.println("--------------------------------------");
    }
     */
    public static final int kSESSION_FOR_LOCALHOST = 1;
    public static final int kSESSION_FOR_CMISTEST_REMOTEAWS = 2;
    public static final int kSESSION_FOR_DEMO_NUXEO_COM = 3;

    public static final boolean kSAME_DOC_FOR_ALL = false;
    public static final boolean kONE_SESSION_FOR_ALL = true;
    public static final boolean kENABLE_CLIENT_CACHE = false; // ignored if kONE_SESSION_FOR_ALL is false
    public static final boolean kUSE_WITH_TITLE_DIFF_NAME = false; // dc:title == or != name

    public static final boolean kDO_LOCALHOST = false;
    public static final int kLOCALHOST_COUNT_OF_TESTS = 0;

    public static final boolean kDO_CMISTEST_REMOTEAWS = true;
    public static final int kCMISTEST_REMOTEAWS_COUNT_OF_TESTS = 0;

    public static final boolean kDO_DEMO_NUXEO_COM = false;
    public static final int kDEMO_NUXEO_COM_COUNT_OF_TESTS = 2;//50;

    public static final boolean kLOCAL_ONETHREAD_N_MINUTES = false;
    public static final int k_ONETHREAD_N_MINUTES_DURATION = 30; //Minutes
    public static final int k_ONETHREAD_N_MINUTES_LOG_EVERY = 5; //seconds
    public static final int k_ONETHREAD_N_MINUTES_SLEEP_N_MS = 250;// every k_ONETHREAD_N_MINUTES_LOG_EVERY. To let the network have a pause

    public static int _RandomInt(int inMin, int inMax) {
        // No error check here
        return inMin + (int)(Math.random() * ((inMax - inMin) + 1));
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        do_buildDocNames();


/*
        CmisObject doc= null;
        Session session = do_GetSession(false);
        long start = System.currentTimeMillis();
        ItemIterable<QueryResult> results = session.query(
                "SELECT cmis:objectId FROM cmis:document WHERE cmis:name='file-0-3'",
                true);
        Iterator<QueryResult> it = results.iterator();
        if(it.hasNext()) {
            QueryResult r = it.next();
            PropertyData<?> idProp = r.getPropertyByQueryName("cmis:objectId");
            String id = (String) idProp.getFirstValue();
            doc = session.getObject( id );
        } else {
            System.out.println("_queryForName: Doc <file-0-3> not found.");
        }
        System.out.println("alors?..." + (System.currentTimeMillis() - start));
        if(true) {
            return;
        }
*/

        if(!kDO_LOCALHOST && !kDO_DEMO_NUXEO_COM && !kLOCAL_ONETHREAD_N_MINUTES && !kDO_CMISTEST_REMOTEAWS) {
            System.out.println("Hmmmm. Basically, you are testing nothing, all main test are set to false");
        }


        if(kLOCAL_ONETHREAD_N_MINUTES) {
            testOnLocalHost1ThreadNMinutes(k_ONETHREAD_N_MINUTES_DURATION, k_ONETHREAD_N_MINUTES_LOG_EVERY);
            return;
        }

        if(kDO_LOCALHOST) {
            System.out.println("START OF TEST ON LOCAL HOST");
            basicTest(kSESSION_FOR_LOCALHOST, kLOCALHOST_COUNT_OF_TESTS);
            System.out.println("END OF TEST ON LOCAL HOST");
        }

        if(kDO_CMISTEST_REMOTEAWS) {
            System.out.println("START OF TEST ON REMOTE AWS");
            basicTest(kSESSION_FOR_CMISTEST_REMOTEAWS, kCMISTEST_REMOTEAWS_COUNT_OF_TESTS);
            System.out.println("END OF TEST ON REMOTE AWS");
        }

        if(kDO_DEMO_NUXEO_COM) {
            System.out.println("START OF TEST ON demo.nuxeo.com");
            testOnDemoNuxeoCom();
            System.out.println("END OF TEST ON demo.nuxeo.com");
        }
    }

    private static long do_queryForName(Session inSession, String inDocName, boolean inDoLog, int inSessionKind) {
        long start, result;
        CmisObject doc= null;
        boolean needCloseSession = false;

        start = System.currentTimeMillis();

        if(inSession == null) {
            inSession = do_GetSession(inSessionKind);
            needCloseSession = true;
            System.out.println("Session created");
        }

        ItemIterable<QueryResult> results = inSession.query(
                "SELECT cmis:objectId FROM cmis:document WHERE cmis:name='" + inDocName + "'",
                true);
        // Get only the first item for this test, even if the query returned more than one
        Iterator<QueryResult> it = results.iterator();
        if(it.hasNext()) {
            QueryResult r = it.next();
            PropertyData<?> idProp = r.getPropertyByQueryName("cmis:objectId");
            String id = (String) idProp.getFirstValue();
            doc = inSession.getObject( id );
        } else {
            System.out.println("_queryForName: Doc <" + inDocName + "> not found.");
        }

        if(needCloseSession) {
            inSession.clear();
            inSession = null;
        }

        result = System.currentTimeMillis() - start;

        if(inDoLog) {
            // Not in the duration of this call, but for debug
            System.out.println("_queryForName, doc: " + (doc == null ? "(not found)" : "id=" +doc.getId() + ", name=" + doc.getName()));
        }

        return result;
    }

    private static long do_queryUsing_getObjectByPath(Session inSession, String inPath, boolean inDoLog, int inSessionKind) {
        long start, result;
        CmisObject doc= null;
        boolean needCloseSession = false;

        start = System.currentTimeMillis();
        if(inSession == null) {
            inSession = do_GetSession(inSessionKind);
            needCloseSession = true;
        }
        doc =inSession.getObjectByPath(inPath);
        if(needCloseSession) {
            inSession.clear();
            inSession = null;
        }
        result = System.currentTimeMillis() - start;
        if(inDoLog) {
            // Not in the duration of this call, but for debug
            System.out.println("getObjectByPath, doc: " + (doc == null ? "NULL" : "id=" +doc.getId() + ", name=" + doc.getName()));
        }

        return result;
    }

    private static Session do_GetSession(int inKind) {
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put(SessionParameter.USER, "Administrator");
        parameter.put(SessionParameter.PASSWORD, "Administrator");
        switch (inKind) {
        case kSESSION_FOR_LOCALHOST:
            parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/nuxeo/atom/cmis");
            break;

        case kSESSION_FOR_CMISTEST_REMOTEAWS:
            parameter.put(SessionParameter.ATOMPUB_URL, "http://cmistest.cloud.nuxeo.com/nuxeo/atom/cmis");
            break;

        case kSESSION_FOR_DEMO_NUXEO_COM:
            parameter.put(SessionParameter.ATOMPUB_URL, "http://cmis.demo.nuxeo.org/nuxeo/atom/cmis");
            break;

        default:
            break;
        }
        parameter.put(SessionParameter.BINDING_TYPE,
                BindingType.ATOMPUB.value());
        // Use the first repository
        List<Repository> repositories = factory.getRepositories(parameter);
        Session session = repositories.get(0).createSession();
        session.getRootFolder();

        return session;
    }

    public static void do_buildDocNames() {
        if(_docNames == null) {
            _docNamesCount = 7 * 16;
            _docNamesMaxForRandom = _docNamesCount - 1;
            _docNames = new String[ _docNamesCount ];

            _docNamesRenamedCount = _docNamesCount;
            _docNamesRenamed = new String[_docNamesRenamedCount];

            int i = 0, j = 0, idx = 0, idx2 = 0;

            for(i = 0; i <= 6; i++) {
                for(j = 0; j <= 15; j++) {
                    _docNames[idx++] = "file-" + i + "-" + j + ".pdf";
                    _docNamesRenamed[idx2++] = "file-" + i + "-" + j + "-r.pdf";
                }
            }
        }
    }

    private static void do_updateDocNamesToUse() {
        if(kUSE_WITH_TITLE_DIFF_NAME) {
            _docNamesToUse = _docNamesRenamed;
            _docNamesCountToUse = _docNamesRenamedCount;
            _docPathToUse = "/default-domain/workspaces/ws/random-import/renamed/";

        } else {
            _docNamesToUse = _docNames;
            _docNamesCountToUse = _docNamesCount;
            //_docPathToUse = "/default-domain/workspaces/ws/CMIS-Test-ToImport/aThe102Docs/";
            _docPathToUse = "/default-domain/workspaces/ws/a/";
        }
    }

    private static void basicTest(int inSessionKind, int inCountOfTests) throws InterruptedException, IOException {
        Session session = null;

        do_updateDocNamesToUse();

        if(kONE_SESSION_FOR_ALL) {
            session = do_GetSession(inSessionKind);
            session.getDefaultContext().setCacheEnabled(kENABLE_CLIENT_CACHE);
        }

        long totalQueryName = 0, totalQueryPath = 0;
        int i;
        int nbTests = inCountOfTests;

        if(nbTests < 1 || nbTests > _docNamesCountToUse) {
            nbTests = _docNamesCountToUse;
        }

        System.out.println("<QUERY_FOR_NAME>");
        for(i = 0; i < nbTests; i++) {
            if(kSAME_DOC_FOR_ALL) {
                totalQueryName += do_queryForName(session, _docNamesToUse[0], false, inSessionKind);
            } else {
                totalQueryName += do_queryForName(session, _docNamesToUse[i], false, inSessionKind);
            }
            Thread.sleep(1);
        }
        System.out.println("</QUERY_FOR_NAME>");

        System.out.println("<QUERY_FOR_PATH>");
        for(i = 0; i < nbTests; i++) {
            if(kSAME_DOC_FOR_ALL) {
                totalQueryPath += do_queryUsing_getObjectByPath(session,
                        _docPathToUse + _docNamesToUse[0], false, inSessionKind);
            } else {
                totalQueryPath += do_queryUsing_getObjectByPath(session,
                        _docPathToUse + _docNames[i], false, inSessionKind);
            }
            //Thread.sleep(1);
        }
        System.out.println("</QUERY_FOR_PATH>");


        // ==================================================
        String cacheInfo;
        if(kONE_SESSION_FOR_ALL) {
            cacheInfo = session.getDefaultContext().isCacheEnabled() ? "ENABLED" : "NOT ENABLED";
        } else {
            cacheInfo = "NOT ENABLED (one session/query)";
        }
        System.out.println( String.format("Test on localhost\nAlways the same name/path: %s\n"
                + "Using one session for: %s\n"
                + "CMIS Client Cache: %s\n"
                + "Using dc:title != name: %s\n"
                + "Query for name: %d iterations, duration=%dms, average=%dms\n"
                + "Query for path: %d iterations, duration=%dms, average=%dms\n",
                                kSAME_DOC_FOR_ALL ? "YES" : "NO",
                                kONE_SESSION_FOR_ALL ? "ALL" : "EACH REQUEST",
                                cacheInfo,
                                kUSE_WITH_TITLE_DIFF_NAME ? "YES" : "NO",
                                        nbTests, totalQueryName, totalQueryName/nbTests,
                                        nbTests, totalQueryPath, totalQueryPath/nbTests ) );
    }

    private static void testOnLocalHost() throws InterruptedException, IOException {

        basicTest(kSESSION_FOR_LOCALHOST, kLOCALHOST_COUNT_OF_TESTS);

        Session session = null;

        do_updateDocNamesToUse();

        if(kONE_SESSION_FOR_ALL) {
            session = do_GetSession(kSESSION_FOR_LOCALHOST);
            session.getDefaultContext().setCacheEnabled(kENABLE_CLIENT_CACHE);
        }

        long totalQueryName = 0, totalQueryPath = 0;
        int i;
        int nbTests = kLOCALHOST_COUNT_OF_TESTS;

        if(nbTests < 1 || nbTests > _docNamesCountToUse) {
            nbTests = _docNamesCountToUse;
        }

        System.out.println("<QUERY_FOR_NAME>");
        for(i = 0; i < nbTests; i++) {
            if(kSAME_DOC_FOR_ALL) {
                totalQueryName += do_queryForName(session, _docNamesToUse[0], false, kSESSION_FOR_LOCALHOST);
            } else {
                totalQueryName += do_queryForName(session, _docNamesToUse[i], false, kSESSION_FOR_LOCALHOST);
            }
            Thread.sleep(1);
        }
        System.out.println("</QUERY_FOR_NAME>");

        System.out.println("<QUERY_FOR_PATH>");
        for(i = 0; i < nbTests; i++) {
            if(kSAME_DOC_FOR_ALL) {
                totalQueryPath += do_queryUsing_getObjectByPath(session,
                        _docPathToUse + _docNamesToUse[0], false, kSESSION_FOR_LOCALHOST);
            } else {
                totalQueryPath += do_queryUsing_getObjectByPath(session,
                        _docPathToUse + _docNames[i], false, kSESSION_FOR_LOCALHOST);
            }
            //Thread.sleep(1);
        }
        System.out.println("</QUERY_FOR_PATH>");


        // ==================================================
        String cacheInfo;
        if(kONE_SESSION_FOR_ALL) {
            cacheInfo = session.getDefaultContext().isCacheEnabled() ? "ENABLED" : "NOT ENABLED";
        } else {
            cacheInfo = "NOT ENABLED (one session/query)";
        }
        System.out.println( String.format("Test on localhost\nAlways the same name/path: %s\n"
                + "Using one session for: %s\n"
                + "CMIS Client Cache: %s\n"
                + "Using dc:title != name: %s\n"
                + "Query for name: %d iterations, duration=%dms, average=%dms\n"
                + "Query for path: %d iterations, duration=%dms, average=%dms\n",
                                kSAME_DOC_FOR_ALL ? "YES" : "NO",
                                kONE_SESSION_FOR_ALL ? "ALL" : "EACH REQUEST",
                                cacheInfo,
                                kUSE_WITH_TITLE_DIFF_NAME ? "YES" : "NO",
                                        nbTests, totalQueryName, totalQueryName/nbTests,
                                        nbTests, totalQueryPath, totalQueryPath/nbTests ) );
    }

    private static void testOnDemoNuxeoCom() {
        long totalQueryName = 0, totalQueryPath = 0;
        int i;

        do_updateDocNamesToUse();

        Session session = null;

        if(kONE_SESSION_FOR_ALL) {
            session = do_GetSession(kSESSION_FOR_DEMO_NUXEO_COM);
            session.getDefaultContext().setCacheEnabled(kENABLE_CLIENT_CACHE);
        }

        System.out.println("<QUERY_FOR_NAME>");
        for(i = 0; i < kDEMO_NUXEO_COM_COUNT_OF_TESTS; i++) {
            totalQueryName += do_queryForName(session, "project_report.xls", false, kSESSION_FOR_DEMO_NUXEO_COM);
        }
        System.out.println("</QUERY_FOR_NAME>");

        System.out.println("<QUERY_FOR_PATH>");
        for(i = 0; i < kDEMO_NUXEO_COM_COUNT_OF_TESTS; i++) {
            totalQueryPath += do_queryUsing_getObjectByPath(session,
                    "/default-domain/workspaces/IT Department/Projects/Database Creation/Documentation/project_report.xls",
                    false, kSESSION_FOR_DEMO_NUXEO_COM);
        }
        System.out.println("</QUERY_FOR_PATH>");

        String cacheInfo;
        if(kONE_SESSION_FOR_ALL) {
            cacheInfo = session.getDefaultContext().isCacheEnabled() ? "ENABLED" : "NOT ENABLED";
        } else {
            cacheInfo = "NOT ENABLED (one session/query)";
        }
        System.out.println( String.format("Test on demo.nuxeo.com\nAlways the same name/path: YES (project_report.xls)\n"
                + "Using one session for: %s\n"
                + "CMIS Client Cache: %s\n"
                + "Query for name: %d iterations, duration=%dms, average=%dms\n"
                + "Query for path: %d iterations, duration=%dms, average=%dms\n",
                                kONE_SESSION_FOR_ALL ? "ALL" : "EACH REQUEST",
                                cacheInfo,
                                kDEMO_NUXEO_COM_COUNT_OF_TESTS, totalQueryName, totalQueryName/kDEMO_NUXEO_COM_COUNT_OF_TESTS,
                                kDEMO_NUXEO_COM_COUNT_OF_TESTS, totalQueryPath, totalQueryPath/kDEMO_NUXEO_COM_COUNT_OF_TESTS ) );
    }


    private static void testOnLocalHost1ThreadNMinutes(int inDurationMinutes,
                                                        int logInfosEveryNSeconds) throws InterruptedException {
        Session session = null;
        long msStart, msEnd,
             totalLimit,
             countOfIterations = 0,
             numberOfQueries = 0, totalDurationQueryName = 0, totalDurationQueryPath = 0;

        if(inDurationMinutes <= 0) {
            inDurationMinutes = 30;
        }
        if(logInfosEveryNSeconds <= 0) {
            logInfosEveryNSeconds = 30;
        }

        do_updateDocNamesToUse();

        session = do_GetSession(kSESSION_FOR_LOCALHOST);
        session.getDefaultContext().setCacheEnabled(kENABLE_CLIENT_CACHE);

        System.out.println( String.format("START\nDuration (minutes)\t%d\nLog every (seconds)\t%d\nSession shared\tYES\nClient Cache\t%s",
                inDurationMinutes,
                logInfosEveryNSeconds,
                session.getDefaultContext().isCacheEnabled() ? "ENABLED" : "NOT ENABLED") );

        System.out.println("With pause for " + k_ONETHREAD_N_MINUTES_SLEEP_N_MS + "ms");
        System.out.println( "Iteration\tElapsed time (s)\tCount\tName\tPath");
        msStart = System.currentTimeMillis();
        totalLimit = System.currentTimeMillis() + (inDurationMinutes * 60 * 1000);
        do {
            long durationMax = System.currentTimeMillis() + (logInfosEveryNSeconds * 1000);
            int subTotalQueryName = 0, subTotalQueryPath = 0, subCount = 0;
            do {
                subCount += 1;

                subTotalQueryName += do_queryForName(session, _docNamesToUse[ _RandomInt(0, _docNamesMaxForRandom) ], false, kSESSION_FOR_LOCALHOST);
                subTotalQueryPath += do_queryUsing_getObjectByPath(session,
                                                _docPathToUse + _docNamesToUse[ _RandomInt(0, _docNamesMaxForRandom) ],
                                                false, kSESSION_FOR_LOCALHOST);

                if(k_ONETHREAD_N_MINUTES_SLEEP_N_MS > 0) {
                    Thread.sleep(k_ONETHREAD_N_MINUTES_SLEEP_N_MS);
                }

            } while(System.currentTimeMillis() < durationMax);

            countOfIterations += 1;
            System.out.println( String.format("%d\t%d\t%d\t%d\t%d",
                                                countOfIterations,
                                                (int) ((System.currentTimeMillis() - msStart) / 1000),
                                                subCount,
                                                subTotalQueryName / subCount,
                                                subTotalQueryPath / subCount));

            numberOfQueries += subCount;
            totalDurationQueryName += subTotalQueryName;
            totalDurationQueryPath += subTotalQueryPath;

        } while(System.currentTimeMillis() < totalLimit);

        msEnd = System.currentTimeMillis() - msStart;


        // ==================================================
        System.out.println( String.format("Test on localhost\n"
                + "Duration\t%ds\n"
                + "Number of queries (each test)\t%s\n"
                + "Query for name AVG\t%dms\n"
                + "Query for path AVG\t%dms\n",
                            (int) (msEnd/1000),
                            numberOfQueries,
                            (int) (totalDurationQueryName / numberOfQueries),
                            (int) (totalDurationQueryPath / numberOfQueries) ));
    }
}
