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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

/**
 * @author Thibaud Arguillere
 *
 */
public class CMISSession {

    private boolean _reuseSession = true;
    private String _user, _pwd, _url;
    private boolean _useCache = true;

    Session _session = null;

    public static CMISSession createLocalhostSession(boolean inReuse) {
        return new CMISSession(CMISClientConstants.kDEFAULT_USERNAME,
                                CMISClientConstants.kDEFAULT_PWD,
                                CMISClientConstants.kLOCALHOST_URL,
                                inReuse);
    }

    public static CMISSession createDemoNuxeoComSession(boolean inReuse) {
        return new CMISSession(CMISClientConstants.kDEFAULT_USERNAME,
                                CMISClientConstants.kDEFAULT_PWD,
                                CMISClientConstants.KDEMO_NUXEO_COM_URL,
                                inReuse);
    }

    public CMISSession(String inUser, String inPwd, String inUrl, boolean inReuse) {
        _reuseSession = inReuse;
        _user = inUser;
        _pwd = inPwd;
        _url = inUrl;

        _session = null;
    }

    private Session createSession() {
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put(SessionParameter.USER, _user);
        parameter.put(SessionParameter.PASSWORD, _pwd);
        parameter.put(SessionParameter.ATOMPUB_URL, _url);

        parameter.put(SessionParameter.BINDING_TYPE,
                BindingType.ATOMPUB.value());
        // Use the first repository
        List<Repository> repositories = factory.getRepositories(parameter);
        Session session = repositories.get(0).createSession();
        session.getRootFolder();

        return session;
    }

    public Session getSession() {
        if(_reuseSession) {
            if(_session == null) {
                _session = createSession();
            }
            return _session;
        } else {
            _session = null;
            return createSession();
        }
    }

}
