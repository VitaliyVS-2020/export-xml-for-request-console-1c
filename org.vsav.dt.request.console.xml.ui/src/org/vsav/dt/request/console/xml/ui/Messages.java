package org.vsav.dt.request.console.xml.ui;

import org.eclipse.osgi.util.NLS;

class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = "org.vsav.dt.request.console.xml.ui.messages"; //$NON-NLS-1$
    public static String RequestConsoleXml_variable_exception;
    public static String RequestConsoleXml_expression_exception;

    static
    {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
