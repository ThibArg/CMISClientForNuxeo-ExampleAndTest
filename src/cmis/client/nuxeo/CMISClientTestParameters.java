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
 * Contributors:
 *     thibaud
 */
package cmis.client.nuxeo;

/**
 * @author Thibaud Arguillere
 *
 * @since 5.9.2
 */
public class CMISClientTestParameters {

    private boolean _withLocalHostTest;
    private boolean _withDemoNuxeoComTest;

    private boolean _reuseSession;
    private boolean _useClientCache;

    private boolean _withOneThreadNMinutes;
    private int _OTNMDurationInMinutes;// OTNM = "One Thread N Minutes"
    private int _OTNMDurationLogEveryNSeconds;

    // This one uses local host
    public CMISClientTestParameters createOneThreadNMinutesTest(
                                                    int durationInMinutes,
                                                    int logEveryNSeconds,
                                                    boolean inReuseSession,
                                                    boolean inUseClientCache) {
        return new CMISClientTestParameters(false, false,
                                            inReuseSession, inUseClientCache,
                                            true, durationInMinutes, logEveryNSeconds);

    }

    public CMISClientTestParameters(boolean withLocalHostTest,
                                    boolean withDemoNuxeoComTest,
                                    boolean reuseSession,
                                    boolean useClientCache,
                                    boolean withOneThreadNMinutes,
                                    int OTNMDurationInMinutes,
                                    int OTNMDurationLogEveryNSeconds) {
        _withLocalHostTest = withLocalHostTest;
        _withDemoNuxeoComTest = withDemoNuxeoComTest;

        _reuseSession = reuseSession;
        _useClientCache = useClientCache;

        _withOneThreadNMinutes = withOneThreadNMinutes;
        _OTNMDurationInMinutes = OTNMDurationInMinutes;
        _OTNMDurationLogEveryNSeconds = OTNMDurationLogEveryNSeconds;
    }

    public boolean testLocalHost() {
        return _withLocalHostTest;
    }

    public boolean testDemoNuxeoCom() {
        return _withDemoNuxeoComTest;
    }

    public boolean testOneThreadNMinutes() {
        return _withOneThreadNMinutes;
    }
}
