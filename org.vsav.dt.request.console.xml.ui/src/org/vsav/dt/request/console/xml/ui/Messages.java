package org.vsav.dt.request.console.xml.ui;

import org.eclipse.osgi.util.NLS;

class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = "org.vsav.dt.request.console.xml.ui.messages"; //$NON-NLS-1$
    public static String RequestConsoleXml_dialog_title_not_supported_variable_type;
    public static String RequestConsoleXml_dialog_message_not_supported_variable_type_0;
    public static String RequestConsoleXml_dialog_message_finding_libraries_in_configuration;

    static
    {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
