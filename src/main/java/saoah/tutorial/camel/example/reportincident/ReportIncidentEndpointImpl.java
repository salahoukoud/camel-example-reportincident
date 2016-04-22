package saoah.tutorial.camel.example.reportincident;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.component.file.FileComponent;
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
		String name = parameters.getGivenName() + " " + parameters.getFamilyName();

		// let Camel do something with the name
		sendToCamelLog(name);
		sendToCamelFile(parameters.getIncidentId(), name);

		OutputReportIncident out = new OutputReportIncident();
		out.setCode("OK");
		return out;
	}

	private void sendToCamelLog(String name) {
		try {
			// get the log component
			Component component = camel.getComponent("log");

			// create an endpoint and configure it.
			// Notice the URI parameters this is a common pratice in Camel to configure
			// endpoints based on URI.
			// com.mycompany.part2 = the log category used. Will log at INFO level as default
			Endpoint endpoint = component.createEndpoint("log:saoah.tutorial.camel");

			// create an Exchange that we want to send to the endpoint
			Exchange exchange = endpoint.createExchange();
			// set the in message payload (=body) with the name parameter
			exchange.getIn().setBody(name);

			// now we want to send the exchange to this endpoint and we then need a producer
			// for this, so we create and start the producer.
			Producer producer = endpoint.createProducer();
			producer.start();
			// process the exchange will send the exchange to the log component, that will process
			// the exchange and yes log the payload
			producer.process(exchange);

			// stop the producer, we want to be nice and cleanup
			producer.stop();

		} catch (Exception e) {
			// we ignore any exceptions and just rethrow as runtime
			throw new RuntimeException(e);
		}
	}

	private void sendToCamelFile(String incidentId, String name) {
		try {
			// get the file component
			Component component = camel.getComponent("file");

			// create an endpoint and configure it.
			// Notice the URI parameters this is a common pratice in Camel to configure
			// endpoints based on URI.
			// file://target instructs the base folder to output the files. We put in the target folder
			// then its actumatically cleaned by mvn clean
			Endpoint endpoint = component.createEndpoint("file://target");

			// create an Exchange that we want to send to the endpoint
			Exchange exchange = endpoint.createExchange();
			// set the in message payload (=body) with the name parameter
			exchange.getIn().setBody(name);

			// now a special header is set to instruct the file component what the output filename
			// should be
			exchange.getIn().setHeader(FileComponent.HEADER_FILE_NAME, "incident-" + incidentId + ".txt");

			// now we want to send the exchange to this endpoint and we then need a producer
			// for this, so we create and start the producer.
			Producer producer = endpoint.createProducer();
			producer.start();
			// process the exchange will send the exchange to the file component, that will process
			// the exchange and yes write the payload to the given filename
			producer.process(exchange);

			// stop the producer, we want to be nice and cleanup
			producer.stop();
		} catch (Exception e) {
			// we ignore any exceptions and just rethrow as runtime
			throw new RuntimeException(e);
		}
	}
}
