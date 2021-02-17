package org.vsav.dt.request.console.xml.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;

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
    //private BslDebugDispatchingEObjectTextHover dispatchingHover;
    private IAction action;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {

        RequestConsoleConverter test1 = new RequestConsoleConverter(event, offsetHelper);

        return null;
    }

}
