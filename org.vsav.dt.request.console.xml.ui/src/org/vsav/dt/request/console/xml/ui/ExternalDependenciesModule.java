/**
 *
 */
package org.vsav.dt.request.console.xml.ui;

import org.eclipse.core.runtime.Plugin;

import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IConfigurationProvider;
import com._1c.g5.wiring.AbstractServiceAwareModule;

/**
 * @author vsav
 *
 */
public class ExternalDependenciesModule
    extends AbstractServiceAwareModule
{

    /**
     * @param bundle
     */
    public ExternalDependenciesModule(Plugin bundle)
    {
        super(bundle);
    }

    @Override
    protected void doConfigure()
    {
        bind(IConfigurationProvider.class).toService();
        bind(IBmModelManager.class).toService();
        //bind(IConfigurationProvider.class).toService();

    }

}
