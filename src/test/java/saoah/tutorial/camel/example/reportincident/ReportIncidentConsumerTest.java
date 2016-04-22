package saoah.tutorial.camel.example.reportincident;

import org.apache.camel.component.file.FileComponent;
import org.jvnet.mock_javamail.Mailbox;

import saoah.tutorial.camel.example.reportincident.ReportIncidentEndpointImpl;
import junit.framework.TestCase;

public class ReportIncidentConsumerTest extends TestCase {

	private ReportIncidentEndpointImpl endPoint;

	public void testConsumer() throws Exception {
		// we run this unit test with the consumer, hence the true parameter
		endPoint = new ReportIncidentEndpointImpl(true);

		// get the mailbox
		Mailbox box = Mailbox.get("incident@mycompany.com");
		assertEquals("Should not have mails", 0, box.size());

		// drop a file in the folder that the consumer listen
		// here is a trick to reuse Camel! so we get the producer template and just
		// fire a message that will create the file for us
		endPoint.getTemplate().sendBodyAndHeader("file://target/subfolder?append=false", "Hello World", FileComponent.HEADER_FILE_NAME, "mail-incident-test.txt");

		// let the consumer have time to run
		Thread.sleep(3 * 1000);

		// get the mock mailbox and check if we got mail ;)
		assertEquals("Should have got 1 mail", 1, box.size());
		assertEquals("Subject wrong", "New incident reported", box.get(0).getSubject());
		assertEquals("Mail body wrong", "Hello World", box.get(0).getContent());
	}
}
