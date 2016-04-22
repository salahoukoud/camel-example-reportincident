package saoah.tutorial.camel.example.reportincident;

import org.apache.camel.CamelContext;
import org.apache.camel.component.log.LogComponent;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * The webservice we have implemented.
 */
public class ReportIncidentEndpointImpl implements ReportIncidentEndpoint {

	private CamelContext camel;

	public ReportIncidentEndpointImpl() throws Exception {
		// create the camel context that is the "heart" of Camel
		camel = new DefaultCamelContext();

		// add the log component
		camel.addComponent("log", new LogComponent());

		// start Camel
		camel.start();
	}

	public OutputReportIncident reportIncident(InputReportIncident parameters) {
		System.out.println("Hello ReportIncidentEndpointImpl is called from " + parameters.getGivenName());

		OutputReportIncident out = new OutputReportIncident();
		out.setCode("OK");
		return out;
	}
}
