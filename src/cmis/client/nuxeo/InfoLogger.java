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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * @author Thibaud Arguillere
 *
 */
public class InfoLogger {
    protected Writer infoFileWriter;

    public InfoLogger() throws IOException {
        File theFile = null;

        //theFile = new File( "/etc/nuxeo/CMIS-Test-Logs/Test_" + System.currentTimeMillis() + ".txt" );
        theFile = new File( "/Users/thibaud/Desktop/TestCMISClient/Test_" + System.currentTimeMillis() + ".txt" );
        infoFileWriter = new FileWriter(theFile);
    }

    public void println(String inWhat) throws IOException {
        infoFileWriter.write(inWhat + "\n");
        infoFileWriter.flush();
    }

    public void release() throws IOException {
        if (infoFileWriter != null) {
            infoFileWriter.flush();
            infoFileWriter.close();
            infoFileWriter = null;
        }
    }
}
