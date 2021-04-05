package org.vsav.dt.request.console.xml.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;

import com._1c.g5.v8.dt.core.platform.IConfigurationProvider;
import com.google.inject.Inject;

public class XmlCalculateHandler
    extends AbstractHandler
    implements IHandler
{
    // сервис для получения семантических элементов модели встроенного языка по позиции внутри модуля
    @Inject
    private EObjectAtOffsetHelper offsetHelper;
    @Inject
    private IConfigurationProvider configurationProvider;
    private IAction action;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {

        if (!isSSL(event))
        {
            return null;
        }
        RequestConsoleConverter test1 = new RequestConsoleConverter(event, offsetHelper);

        return null;
    }

    private boolean isSSL(ExecutionEvent event)
    {
        // detected SSL
        XtextEditor activEditor = EditorUtils.getActiveXtextEditor(event);

        IFileEditorInput input = (IFileEditorInput)activEditor.getEditorInput();
        IFile file = input.getFile();
        if (file != null)
        {
            ;
            IProject project = file.getProject();
            if (project != null)
            {
                // Creation using a factory String chekSSL = DetectorSSL.getVersionSSL(project);
                DetectorSSL detectorSSL = Activator.getDefault().getInjector().getInstance(DetectorSSL.class);
                String chekSSL = detectorSSL.getVersionSSL(project);

                return !chekSSL.isEmpty();
            }
        }
        return false;
    }
}
