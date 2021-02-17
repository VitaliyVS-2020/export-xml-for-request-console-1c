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
import org.eclipse.jface.text.ITextSelection;
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

                if (semanticObject instanceof StaticFeatureAccess)
                {
                    //Элемент модели, соответствующий именному выражению, к которому обращаются НЕ (НЕ!!!) через “точку”.
                    //Данный элемент унаследовал все свойства и методы от com._1c.g5.v8.dt.bsl.model.FeatureAccess
                    evaluateExpressionDebug(((StaticFeatureAccess)semanticObject).getName());
                }
                else if (semanticObject instanceof DynamicFeatureAccess)
                {
                    //Элемент модели, соответствующий именному выражению, к которому обращаются через “точку”.
                    //Данный элемент унаследовал все свойства и методы от com._1c.g5.v8.dt.bsl.model.FeatureAccess
                    Object tex1 = null;
                }
                else
                {
                    //Ничего evaluateExpressionDebug(String )
                }

            }

        });
    }

    /**
     * @param name
     */
    protected void evaluateExpressionDebug(String text)
    {

        this.variable = text;
        this.expression = "ОбщегоНазначения.ЗапросВСтрокуXML(" + text + ")";

        final IBslStackFrame frame = this.getFrame();

        if (frame != null)
        {

            IRuntimeDebugClientTarget targetDebug = frame.getDebugTarget();

            BslValuePath path = new BslValuePath(this.expression);

            // generate random UUID to have access to child evaluation nodes later (if we want)
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
                //DebugCorePlugin.log((Throwable)e);
            }

        }

    }

    /**
     * @param result
     */
    private void requestResult(CalculationResultBaseData result)
    {

        //!! ПРОВЕРИТ  String   getLocalVariableName()

        String TypeName = result.getResultValueInfo().getTypeName();

        if ("ДинамическийСписок".equals(TypeName))
        {
            // in progress...
        }
        else if ("Запрос".equals(TypeName))
        {
            // in progress...
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
