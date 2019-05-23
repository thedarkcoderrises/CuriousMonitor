package com.tdcr.docker.ui.views.events;

import com.tdcr.docker.backend.data.entity.Event;
import com.tdcr.docker.ui.components.EventsStateConverter;
import com.vaadin.flow.data.renderer.TemplateRenderer;

import java.time.LocalDate;

import static com.tdcr.docker.backend.utils.FormattingUtils.*;

/**
 * Help class to get ready to use TemplateRenderer for displaying order card list on the Storefront and Dashboard grids.
 * Using TemplateRenderer instead of ComponentRenderer optimizes the CPU and memory consumption.
 * <p>
 * In addition, component includes an optional header above the order card. It is used
 * to visually separate orders into groups. Technically all order cards are
 * equivalent, but those that do have the header visible create a visual group
 * separation.
 */
public class EventsCard {

	public static TemplateRenderer<Event> getTemplate() {
		return TemplateRenderer.of(
				  "<events-card"
				+ "  header='[[item.header]]'"
				+ "  events-card='[[item.eventsCard]]'>"
				+ "</events-card>");
	}

	public static EventsCard create(Event event) {
			return new EventsCard(event);
	}
	private static EventsStateConverter stateConverter = new EventsStateConverter();
	private Event event;
	private boolean recent, inWeek;

	public  EventsCard(Event event){
	 this.event = event;
		LocalDate now = LocalDate.now();
		LocalDate date = event.getDueDate();
		recent = date.equals(now) || date.equals(now.minusDays(1));
		inWeek = !recent && now.getYear() == date.getYear() && now.get(WEEK_OF_YEAR_FIELD) == date.get(WEEK_OF_YEAR_FIELD);
	}


	public String getState() {
		return stateConverter.encode(event.getState());
	}

	public String getTime() {
		return HOUR_FORMATTER.format(event.getDueTime());
	}

	public String getShortDay() {
		return inWeek ? SHORT_DAY_FORMATTER.format(event.getDueDate()) : null;
	}

	public String getSecondaryTime() {
		return inWeek ? HOUR_FORMATTER.format(event.getDueTime()) : null;
	}

	public String getMonth() {
		return recent || inWeek ? null : MONTH_AND_DAY_FORMATTER.format(event.getDueDate());
	}

	public String getFullDay() {
		return recent || inWeek ? null : WEEKDAY_FULLNAME_FORMATTER.format(event.getDueDate());
	}

	public String getShortDesc() {
		return event.getShortDesc();
	}
}
