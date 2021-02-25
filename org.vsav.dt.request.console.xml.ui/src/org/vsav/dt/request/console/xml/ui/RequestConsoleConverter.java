package org.vsav.dt.request.console.xml.ui;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.util.concurrent.IUnitOfWork.Void;

import com._1c.g5.v8.dt.bsl.model.DynamicFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.StaticFeatureAccess;
import com._1c.g5.v8.dt.debug.core.model.IBslStackFrame;
import com._1c.g5.v8.dt.debug.core.model.IRuntimeDebugClientTarget;
import com._1c.g5.v8.dt.debug.core.model.evaluation.EvaluationRequest;
import com._1c.g5.v8.dt.debug.core.model.evaluation.IEvaluationRequest;
import com._1c.g5.v8.dt.debug.core.model.values.BslValuePath;
import com._1c.g5.v8.dt.debug.model.calculations.CalculationResultBaseData;
import com._1c.g5.v8.dt.debug.model.calculations.ViewInterface;

/**
 * @author vsav
 *
 */
public class RequestConsoleConverter
{
    public String variable;
    public String expression;

    public RequestConsoleConverter(ExecutionEvent event, EObjectAtOffsetHelper offsetHelper)
    {
        this.variable = "";
        this.expression = "";

        XtextEditor editor2 = EditorUtils.getActiveXtextEditor(event);
        ITextSelection selection = (ITextSelection)editor2.getSelectionProvider().getSelection();
        editor2.getDocument().priorityReadOnly(new Void<XtextResource>()
        {
            @Override
            public void process(XtextResource state) throws Exception
            {
                Object semanticObject = offsetHelper.resolveElementAt(state, selection.getOffset());

                String expressionName = getExpressionFeatureAccess(semanticObject);
                evaluateExpressionDebug(expressionName);
            }
        });
    }

    private static String getExpressionFeatureAccess(Object semanticObject)
    {
        if (semanticObject instanceof StaticFeatureAccess)
        {
            //Элемент модели, соответствующий именному выражению, к которому обращаются НЕ (НЕ!!!) через “точку”.
            //Данный элемент унаследовал все свойства и методы от com._1c.g5.v8.dt.bsl.model.FeatureAccess
            return ((StaticFeatureAccess)semanticObject).getName();
        }
        else if (semanticObject instanceof DynamicFeatureAccess)
        {
            //Элемент модели, соответствующий именному выражению, к которому обращаются через “точку”.
            //Данный элемент унаследовал все свойства и методы от com._1c.g5.v8.dt.bsl.model.FeatureAccess
            return getExpressionFeatureAccess(((DynamicFeatureAccess)semanticObject).getSource()) + "."
                + ((DynamicFeatureAccess)semanticObject).getName();
        }
        else
        {
            return "";
        }

    }
    /**
     * @param name
     */
    protected void evaluateExpressionDebug(String textVariable)
    {

        this.variable = textVariable;

        final IBslStackFrame frame = this.getFrame();

        if (frame != null)
        {

            IRuntimeDebugClientTarget targetDebug = frame.getDebugTarget();

            BslValuePath path = new BslValuePath(this.variable);

            UUID expressionUuid = UUID.randomUUID();

            int maxTestSize = 4000;
            boolean isMultiLine = true;
            List<ViewInterface> evaluationInterfaces = Collections.singletonList(ViewInterface.CONTEXT);

            IEvaluationRequest request = new EvaluationRequest(frame, expressionUuid, path, evaluationInterfaces,
                maxTestSize, isMultiLine, result -> {
                    // this code will be performed asynchronously
                    if (result.isSuccess())
                    {
                        requestResult(result.getResult());
                    }
                    else
                    {
                        requestError(result.getErrorMessage());
                    }
                });

            try
            {
                targetDebug.getEvaluationEngine().evaluateExpression(request);
            }
            catch (DebugException e)
            {
                e.printStackTrace();
            }

        }

    }

    /**
     * @param result
     */
    private void requestResult(CalculationResultBaseData result)
    {

        String TypeName = result.getResultValueInfo().getTypeName();

        if ("ДинамическийСписок".equals(TypeName))
        {
            // Пример: ОбщегоНазначения.ЗначениеВСтрокуXML(Новый Структура("Текст, Параметры", Список.ТекстЗапроса, Список.Параметры)) (char)34 = "
            this.expression = "ОбщегоНазначения.ЗапросВСтрокуXML(Новый Структура(" + (char)34 + "Текст, Параметры"
                + (char)34 + ", " + this.variable + ".ТекстЗапроса, " + this.variable + ".Параметры))";
            evaluateExpressionDebug(this.expression);
        }
        else if ("Запрос".equals(TypeName))
        {
            this.expression = "ОбщегоНазначения.ЗапросВСтрокуXML(" + this.variable + ")";
            evaluateExpressionDebug(this.expression);
        }
        else if ("Строка".equals(TypeName))
        {
            String textForRequest = presentation(result.getResultValueInfo().getPres());
            String cleanTextForRequest = textForRequest.substring(1, textForRequest.length() - 1);
            Notification.copyClipboard(cleanTextForRequest);
        }
        else
        {
            //Сообщим об ошибке
            Display.getDefault().asyncExec(new Runnable()
            {
                @Override
                public void run()
                {
                    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

                    MessageDialog dialog = new MessageDialog(shell, Messages.RequestConsoleXml_variable_exception,
                        (Image)null, Messages.RequestConsoleXml_variable_exception, 2,
                        new String[] { IDialogConstants.OK_LABEL }, 0);

                    if (dialog.open() != 0)
                    {
                        //write logs
                    }
                }
            });
        }

    }

    protected IBslStackFrame getFrame()
    {
        IAdaptable adaptable = DebugUITools.getDebugContext();
        return adaptable != null ? (IBslStackFrame)adaptable.getAdapter(IStackFrame.class) : null;
    }

    protected String presentation(byte[] presentationAsBytes)
    {
        return presentationAsBytes == null ? "" : new String(presentationAsBytes, StandardCharsets.UTF_8);
    }

    private void requestError(String errorMessage)
    {
        // TODO //Сообщим об ошибке
    }

}
