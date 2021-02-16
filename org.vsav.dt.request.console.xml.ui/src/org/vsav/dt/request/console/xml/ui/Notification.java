/**
 *
 */
package org.vsav.dt.request.console.xml.ui;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;

/**
 * @author vsav
 *
 */
public class Notification
{
    public static void copyClipboard(String textClipboard)
    {
        Display display = getDisplay();

        display.asyncExec(() -> {
            Clipboard clipboard = new Clipboard(display);
            TextTransfer[] textTransfer = { TextTransfer.getInstance() };
            clipboard.setContents(new Object[] { textClipboard }, textTransfer);
            clipboard.dispose();
        });
    }

    private static Display getDisplay()
    {
        Display display = Display.getCurrent();
        // может быть нулевым, если за пределами потока пользовательского интерфейса
        if (display == null)
            display = Display.getDefault();
        return display;
    }
}

