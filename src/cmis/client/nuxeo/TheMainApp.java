/*
 * IMPORTANT: This Java application is put on GitHib mainly for backup purpose :->
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

    public static final boolean kSAME_DOC_FOR_ALL = false;
    public static final boolean kONE_SESSION_FOR_ALL = true;
    public static final boolean kENABLE_CLIENT_CACHE = true; // ignored if kONE_SESSION_FOR_ALL is false

    public static final boolean kDO_LOCALHOST = false;
    public static final boolean kDO_DEMO_NUXEO_COM = true;
    public static final int kDEMO_NUXEO_COM_COUNT_OF_TESTS = 50;

    public static void main(String[] args) throws InterruptedException {

        do_buildDocNames();

        if(kDO_LOCALHOST) {
            System.out.println("<test_on_localhost>");
            testOnLocalHost();
            System.out.println("</test_on_localhost>");
        }

        if(kDO_DEMO_NUXEO_COM) {
            System.out.println("<test_on_nuxeo_demo_com>");
            testOnDemoNuxeoCom();
            System.out.println("</test_on_nuxeo_demo_com>");
        }
    }

    private static long do_queryForName(Session inSession, String inDocName, boolean inDoLog, boolean inIsDemoNuxeoCom) {
        long start, result;
        CmisObject doc= null;
        boolean needCloseSession = false;

        start = System.currentTimeMillis();

        if(inSession == null) {
            inSession = do_GetSession(inIsDemoNuxeoCom);
            needCloseSession = true;
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

    private static long do_queryUsing_getObjectByPath(Session inSession, String inPath, boolean inDoLog, boolean inIsDemoNuxeoCom) {
        long start, result;
        CmisObject doc= null;
        boolean needCloseSession = false;

        start = System.currentTimeMillis();
        if(inSession == null) {
            inSession = do_GetSession(inIsDemoNuxeoCom);
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

    private static Session do_GetSession(boolean inDemoNuxeoCom) {
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put(SessionParameter.USER, "Administrator");
        parameter.put(SessionParameter.PASSWORD, "Administrator");
        if(inDemoNuxeoCom) {
            parameter.put(SessionParameter.ATOMPUB_URL, "http://cmis.demo.nuxeo.org/nuxeo/atom/cmis");
        } else {
            parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/nuxeo/atom/cmis");
        }
        parameter.put(SessionParameter.BINDING_TYPE,
                BindingType.ATOMPUB.value());
        // Use the first repository
        List<Repository> repositories = factory.getRepositories(parameter);
        Session session = repositories.get(0).createSession();
        session.getRootFolder();

        return session;
    }

    private static String [] _docNames = null;
    private static int _docNamesCount = -1;
    public static void do_buildDocNames() {
        if(_docNames == null) {
            _docNamesCount = 7 * 16;
            _docNames = new String[ _docNamesCount ];
            int i = 0, j = 0, idx = 0;

            for(i = 0; i <= 6; i++) {
                for(j = 0; j <= 15; j++) {
                    _docNames[idx++] = "file-" + i + "-" + j;
                }
            }
        }
    }

    private static void testOnLocalHost() throws InterruptedException {
        Session session = null;

        if(kONE_SESSION_FOR_ALL) {
            session = do_GetSession(false);
            session.getDefaultContext().setCacheEnabled(kENABLE_CLIENT_CACHE);
        }

        long totalQueryName = 0, totalQueryPath = 0;
        int i;
        int kMAX_TESTS = _docNamesCount;

        if(kMAX_TESTS > _docNamesCount) {
            kMAX_TESTS = _docNamesCount;
        }

        System.out.println("<QUERY_FOR_NAME>");
        for(i = 0; i < kMAX_TESTS; i++) {
            if(kSAME_DOC_FOR_ALL) {
                totalQueryName += do_queryForName(session, _docNames[0], false, false);
            } else {
                totalQueryName += do_queryForName(session, _docNames[i], false, false);
            }
            Thread.sleep(1);
        }
        System.out.println("</QUERY_FOR_NAME>");

        System.out.println("<QUERY_FOR_PATH>");
        for(i = 0; i < kMAX_TESTS; i++) {
            if(kSAME_DOC_FOR_ALL) {
                totalQueryPath += do_queryUsing_getObjectByPath(session,
                        "/default-domain/workspaces/ws/random-import/other/" + _docNames[0], false, false);
            } else {
                totalQueryPath += do_queryUsing_getObjectByPath(session,
                        "/default-domain/workspaces/ws/random-import/other/" + _docNames[i], false, false);
            }
            Thread.sleep(1);
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
                + "Query for name: %d iterations, duration=%dms, average=%dms\n"
                + "Query for path: %d iterations, duration=%dms, average=%dms\n",
                                kSAME_DOC_FOR_ALL ? "YES" : "NO",
                                kONE_SESSION_FOR_ALL ? "ALL" : "EACH REQUEST",
                                cacheInfo,
                                kMAX_TESTS, totalQueryName, totalQueryName/kMAX_TESTS,
                                kMAX_TESTS, totalQueryPath, totalQueryPath/kMAX_TESTS ) );
    }

    private static void testOnDemoNuxeoCom() {
        long totalQueryName = 0, totalQueryPath = 0;
        int i;

        Session session = null;

        if(kONE_SESSION_FOR_ALL) {
            session = do_GetSession(true);
            session.getDefaultContext().setCacheEnabled(kENABLE_CLIENT_CACHE);
        }

        System.out.println("<QUERY_FOR_NAME>");
        for(i = 0; i < kDEMO_NUXEO_COM_COUNT_OF_TESTS; i++) {
            totalQueryName += do_queryForName(session, "project_report.xls", false, true);
        }
        System.out.println("</QUERY_FOR_NAME>");

        System.out.println("<QUERY_FOR_PATH>");
        for(i = 0; i < kDEMO_NUXEO_COM_COUNT_OF_TESTS; i++) {
            totalQueryPath += do_queryUsing_getObjectByPath(session,
                    "/default-domain/workspaces/IT Department/Projects/Database Creation/Documentation/project_report.xls",
                    false, true);
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
}
