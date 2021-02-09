package org.vsav.dt.request.console.xml.ui;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;

import com._1c.g5.v8.dt.bsl.ui.menu.BslHandlerUtil;
import com._1c.g5.v8.dt.core.platform.IConfigurationProvider;
import com._1c.g5.v8.dt.debug.core.model.IBslStackFrame;
import com._1c.g5.v8.dt.debug.core.model.IRuntimeDebugClientTarget;
import com._1c.g5.v8.dt.debug.core.model.evaluation.EvaluationRequest;
import com._1c.g5.v8.dt.debug.core.model.evaluation.IEvaluationRequest;
import com._1c.g5.v8.dt.debug.core.model.values.BslValuePath;
import com._1c.g5.v8.dt.debug.model.calculations.CalculationResultBaseData;
import com._1c.g5.v8.dt.debug.model.calculations.ViewInterface;
import com.google.inject.Inject;

public class XmlCalculateHandler
    extends AbstractHandler
    implements IHandler
{

    @Inject
    private IConfigurationProvider configurationProvider;
    //private BslDebugDispatchingEObjectTextHover dispatchingHover;
    private IAction action;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {

        // получим активный Xtext редактор
        IWorkbenchPart part = HandlerUtil.getActivePart(event);
        XtextEditor target = part.getAdapter(XtextEditor.class);

        if (target == null)
        {
            return null;
        }

        // для полученного редактора убедимся, что под ним лежит файл в
        // проекте с конфигурацией, иначе это точно не модель объекта
        if (!(target.getEditorInput() instanceof IFileEditorInput))
            return null;
        IFileEditorInput input = (IFileEditorInput)target.getEditorInput();
        IFile file = input.getFile();
        if (file == null)
            return null;
        IProject project = file.getProject();
        if (project == null)
            return null;
        IXtextDocument doc = target.getDocument();

        // Получим выделенный текст
        ITextViewer viewer = BslHandlerUtil.getTextViewer(target);
        String text = getText(doc, viewer);
        if (text == null)
            return null;

        // Получим предмет отладки
        final IBslStackFrame frame = this.getFrame();

        if (frame != null)
        {

            IRuntimeDebugClientTarget targetDebug = frame.getDebugTarget();

            // define evaluating expression
            BslValuePath path = new BslValuePath("ОбщегоНазначения.ЗапросВСтрокуXML(" + text + ")"); //$NON-NLS-1$ //$NON-NLS-2$

            // generate random UUID to have access to child evaluation nodes later (if we want)
            UUID expressionUuid = UUID.randomUUID();

            int maxTestSize = 4000;
            boolean isMultiLine = true;
            List<ViewInterface> evaluationInterfaces = Collections.singletonList(ViewInterface.CONTEXT);

            IEvaluationRequest request =
                new EvaluationRequest(frame, expressionUuid, path, evaluationInterfaces, maxTestSize, isMultiLine,
                    result -> {
                    // this code will be performed asynchronously
                    if (result.isSuccess())
                    {
                        showResult(result.getResult());
                    }
                    else
                    {
                        showError(result.getErrorMessage());
                    }
                }
                );

            try
            {
                targetDebug.getEvaluationEngine().evaluateExpression(request);
            }
            catch (DebugException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param errorMessage
     */
    private void showError(String errorMessage)
    {
        String ErrorCode = "Error"; //$NON-NLS-1$
    }

    /**
     * @param result
     */
    private void showResult(CalculationResultBaseData result)
    {
        String textForRequest = presentation(result.getResultValueInfo().getPres());

        String cleanTextForRequest = textForRequest.substring(1, textForRequest.length() - 1);

        Display display = getDisplay();

        display.asyncExec(() -> {
            Clipboard clipboard = new Clipboard(display);

            TextTransfer[] textTransfer = { TextTransfer.getInstance() };
            clipboard.setContents(new Object[] { cleanTextForRequest }, textTransfer);
            clipboard.dispose();
        });
    }

    private String getText(IXtextDocument doc, ITextViewer viewer)
    {
        int startPos = (viewer.getSelectedRange()).x;
        int endPos = startPos + (viewer.getSelectedRange()).y;
        try
        {
            return doc.get(startPos, endPos - startPos);
        }
        catch (BadLocationException e)
        {
            return null;
        }
    }

    protected IBslStackFrame getFrame()
    {
        IAdaptable adaptable = DebugUITools.getDebugContext();
        return adaptable != null ? (IBslStackFrame)adaptable.getAdapter(IStackFrame.class) : null;
    }

    public static Display getDisplay()
    {
        Display display = Display.getCurrent();
        // может быть нулевым, если за пределами потока пользовательского интерфейса
        if (display == null)
            display = Display.getDefault();
        return display;
    }

    protected String presentation(byte[] presentationAsBytes)
    {
        return presentationAsBytes == null ? "" : new String(presentationAsBytes, StandardCharsets.UTF_8); //$NON-NLS-1$
    }

}
