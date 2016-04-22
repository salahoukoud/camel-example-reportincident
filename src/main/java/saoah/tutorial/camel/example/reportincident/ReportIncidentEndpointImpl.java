package saoah.tutorial.camel.example.reportincident;

/**
 * The webservice we have implemented.
 */
public class ReportIncidentEndpointImpl implements ReportIncidentEndpoint {

	public OutputReportIncident reportIncident(InputReportIncident parameters) {
		System.out.println("Hello ReportIncidentEndpointImpl is called from " + parameters.getGivenName());

		OutputReportIncident out = new OutputReportIncident();
		out.setCode("OK");
		return out;
	}
}
