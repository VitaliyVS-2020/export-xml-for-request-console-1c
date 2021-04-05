package org.vsav.dt.request.console.xml.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;

import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.AbstractBmTask;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.bm.integration.IBmTask;
import com._1c.g5.v8.dt.bsl.model.Block;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.FeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.SimpleStatement;
import com._1c.g5.v8.dt.bsl.model.Statement;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.metadata.mdclass.CommonModule;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com.google.inject.Inject;

public class DetectorSSL
{
    @Inject
    private IBmModelManager bmModelManager;

    private static final String COMMON_MODULE_UPDATE_SSL = "ОбновлениеИнформационнойБазыБСП"; //$NON-NLS-1$
    private static final String METHOD_UPDATE = "ПриДобавленииПодсистемы"; //$NON-NLS-1$
    private static final String NAME_FEATURE_ACCESS = "Версия"; //$NON-NLS-1$

    public String getVersionSSL(IProject project)
    {
        IBmModel model = bmModelManager.getModel(project);

        IBmTask<String> getVersionSSLTask =
            new AbstractBmTask<>(Messages.RequestConsoleXml_dialog_message_finding_libraries_in_configuration)
            {
                @Override
                public String execute(IBmTransaction transaction, IProgressMonitor monitor)
                {
                    Configuration configuration = (Configuration)transaction.getTopObjectByFqn("Configuration"); //$NON-NLS-1$
                    EList<CommonModule> modules = configuration.getCommonModules();

                    for (CommonModule module : modules)
                        if (COMMON_MODULE_UPDATE_SSL.equals(module.getName()))
                        {
                            for (Method method : module.getModule().allMethods())
                            {
                                if (METHOD_UPDATE.equals(method.getName()))
                                {
                                    for (Statement statement : ((Block)method).allStatements())
                                    {
                                        if (statement instanceof SimpleStatement)
                                        {
                                            SimpleStatement simpleStatement = (SimpleStatement)statement;
                                            Expression leftExpression = simpleStatement.getLeft();

                                            String leftFieldName = ((FeatureAccess)leftExpression).getName();

                                            if (NAME_FEATURE_ACCESS.equals(leftFieldName))
                                            {
                                                Expression rightField = simpleStatement.getRight();
                                                if (rightField instanceof StringLiteral)
                                                {
                                                    return ((StringLiteral)rightField).getLines().iterator().next();
                                                }

                                            }

                                        }

                                    }
                                }
                            }
                        }
                    ;
                    return ""; //$NON-NLS-1$
                }
            };
        return model.executeReadonlyTask(getVersionSSLTask);
    }
}
