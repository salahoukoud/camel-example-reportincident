package saoah.tutorial.camel.example.reportincident;

import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.log.LogComponent;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * The webservice we have implemented.
 */
public class ReportIncidentEndpointImpl implements ReportIncidentEndpoint {

	private CamelContext camel;
	private ProducerTemplate template;

	public ReportIncidentEndpointImpl() throws Exception {
		// create the camel context that is the "heart" of Camel
		camel = new DefaultCamelContext();
		// get the ProducerTemplate thst is a Spring'ish xxxTemplate based producer for very
		// easy sending exchanges to Camel.
		template = camel.createProducerTemplate();

		// start Camel
		camel.start();
	}

	public OutputReportIncident reportIncident(InputReportIncident parameters) {
		String name = parameters.getGivenName() + " " + parameters.getFamilyName();

		/**
		 * Camel Log
		 */
		// let Camel do something with the name
		sendToCamelLog(name);
		sendToCamelLogByCamelTemplate(name);

		/**
		 * Camel File
		 */
		sendToCamelFile(parameters.getIncidentId(), name);
		sendToCamelFileByCamelTemplate(parameters.getIncidentId(), name);

		/**
		 * Camel Message Translation
		 */
		sendMailBodyToCamelLog(parameters);
		generateEmailBodyAndStoreAsFile(parameters);
		

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

			// OR USE fully java based configuration of endpoints by setter
			// FileEndpoint endpoint2 = (FileEndpoint)component.createEndpoint("");
			// endpoint2.setFile(new File("target/subfolder"));
			// endpoint2.setAutoCreate(true);

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

	private void sendToCamelLogByCamelTemplate(String name) {
		template.sendBody("log:saoah.tutorial.camel", name);
	}

	private void sendToCamelFileByCamelTemplate(String incidentId, String name) {
		String filename = "easy-incident-" + incidentId + ".txt";
		template.sendBodyAndHeader("file://target/subfolder", name, FileComponent.HEADER_FILE_NAME, filename);
	}

	private String createMailBody(InputReportIncident parameters) {
		StringBuilder sb = new StringBuilder();
		sb.append("Incident ").append(parameters.getIncidentId());
		sb.append(" has been reported on the ").append(parameters.getIncidentDate());
		sb.append(" by ").append(parameters.getGivenName());
		sb.append(" ").append(parameters.getFamilyName());
		// of the mail body with more appends to the string builder
		return sb.toString();
	}

	private void sendMailBodyToCamelLog(InputReportIncident parameters) {
		String mailBody = createMailBody(parameters);
		template.sendBody("log:saoah.tutorial.camel", parameters);
	}

	private void generateEmailBodyAndStoreAsFile(InputReportIncident parameters) {
		// generate the mail body using velocity template
		// notice that we just pass in our POJO (= InputReportIncident) that we
		// got from Apache CXF to Velocity.
		Object mailBody = template.sendBody("velocity:MailBody.vm", parameters);
		// Note: the response is a String and can be cast to String if needed
		// store the mail in a file
		String filename = "mail-incident-" + parameters.getIncidentId() + ".txt";
		template.sendBodyAndHeader("file://target/subfolder", mailBody, FileComponent.HEADER_FILE_NAME, filename);
	}
}
