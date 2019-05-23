package com.tdcr.docker.ui.components;

import com.tdcr.docker.backend.data.EventState;
import com.vaadin.flow.templatemodel.ModelEncoder;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tdcr.docker.backend.utils.DataUtil.convertIfNotNull;

public class EventsStateConverter implements ModelEncoder<EventState, String> {

	private Map<String, EventState> values;

	public EventsStateConverter() {
		values = Arrays.stream(EventState.values())
				.collect(Collectors.toMap(EventState::toString, Function.identity()));
	}

	@Override
	public EventState decode(String presentationValue) {
		return convertIfNotNull(presentationValue, values::get);
	}

	@Override
	public String encode(EventState modelValue) {
		return convertIfNotNull(modelValue, EventState::toString);
	}

}
