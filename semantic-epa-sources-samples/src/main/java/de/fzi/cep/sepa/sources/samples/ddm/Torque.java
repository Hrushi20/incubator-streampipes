package de.fzi.cep.sepa.sources.samples.ddm;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.rdf2go.vocabulary.XSD;

import de.fzi.cep.sepa.desc.EventStreamDeclarer;
import de.fzi.cep.sepa.model.impl.EventGrounding;
import de.fzi.cep.sepa.model.impl.EventProperty;
import de.fzi.cep.sepa.model.impl.EventSchema;
import de.fzi.cep.sepa.model.impl.EventStream;
import de.fzi.cep.sepa.model.impl.graph.SEP;
import de.fzi.cep.sepa.sources.samples.config.AkerVariables;
import de.fzi.cep.sepa.sources.samples.config.SourcesConfig;

public class Torque implements EventStreamDeclarer{

	private String topicName;
	
	@Override
	public EventStream declareModel(SEP sep) {
		
		EventStream stream = new EventStream();
		
		EventSchema schema = new EventSchema();
		List<EventProperty> eventProperties = new ArrayList<EventProperty>();
		eventProperties.add(new EventProperty(XSD._long.toString(), "variable_type", "", de.fzi.cep.sepa.commons.Utils.createURI("http://schema.org/Number")));
		eventProperties.add(new EventProperty(XSD._string.toString(), "variable_timestamp", "", de.fzi.cep.sepa.commons.Utils.createURI("http://schema.org/DateTime")));
		eventProperties.add(new EventProperty(XSD._double.toString(), "variable_value", "", de.fzi.cep.sepa.commons.Utils.createURI("http://sepa.event-processing.org/sepa#torque")));
		
		
		EventGrounding grounding = new EventGrounding();
		grounding.setPort(61616);
		grounding.setUri("tcp://localhost:61616");
		grounding.setTopicName("SEPA.SEP.DDM.Torque");
		this.topicName = grounding.getTopicName();
		
		stream.setEventGrounding(grounding);
		schema.setEventProperties(eventProperties);
		stream.setEventSchema(schema);
		stream.setName(AkerVariables.DrillingTorque.eventName());
		stream.setDescription(AkerVariables.DrillingTorque.description());
		stream.setUri(sep.getUri() + "/torque");
		stream.setIconUrl(SourcesConfig.iconBaseUrl + "/Torque_Icon2" +"_HQ.png");
		
		return stream;
	}

	@Override
	public void executeStream() {
		// send POST request to event replay util
		// call some generic method which takes a source ID as a parameter and performs the request
		// AkerVariables.GearLubeOilTemperature.tagNumber returns tag number for this event stream
		// topicName denotes the actual topic to subscribe for
	}

	@Override
	public boolean isExecutable() {
		return true;
	}
}
