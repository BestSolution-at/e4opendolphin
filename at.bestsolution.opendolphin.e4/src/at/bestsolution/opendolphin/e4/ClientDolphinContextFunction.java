package at.bestsolution.opendolphin.e4;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.opendolphin.core.client.ClientDolphin;
import org.opendolphin.core.client.ClientModelStore;
import org.opendolphin.core.client.comm.HttpClientConnector;
import org.opendolphin.core.client.comm.UiThreadHandler;
import org.opendolphin.core.comm.JsonCodec;

public class ClientDolphinContextFunction extends ContextFunction {
	private Map<String,DolphinCreator> dolphinClients = new HashMap<>();
	
	private static final String SERVICE_ENDPOINT = System.getProperty("opendolphin._default.endpointurl");
	
	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		DolphinCreator c = dolphinClients.get(contextKey);
		if( c == null ) {
			MApplication mApplication = context.get(MApplication.class);
			c = ContextInjectionFactory.make(DolphinCreator.class,mApplication.getContext());
			dolphinClients.put(contextKey, c);
		}
		return c.getClient();
	}
	
	public static class DolphinCreator {
		private ClientDolphin d;
		
		@Inject
		public DolphinCreator(UiThreadHandler threadHandler) {
			d = new ClientDolphin();
	        d.setClientModelStore(new ClientModelStore(d));
	        HttpClientConnector connector = new HttpClientConnector(d, SERVICE_ENDPOINT);
	        connector.setCodec(new JsonCodec());
	        connector.setUiThreadHandler(threadHandler);
	        d.setClientConnector(connector);
		}
		
		public ClientDolphin getClient() {
			return d;
		}
	}
}
