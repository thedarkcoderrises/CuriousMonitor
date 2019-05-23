package com.tdcr.docker.ui.components;


import com.tdcr.docker.backend.data.entity.Event;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

public class EventsCardHeaderGenerator {

	private class HeaderWrapper {
		private Predicate<LocalDate> matcher;

		private EventsCardHeader header;

		private Long selected;

		public HeaderWrapper(Predicate<LocalDate> matcher, EventsCardHeader header) {
			this.matcher = matcher;
			this.header = header;
		}

		public boolean matches(LocalDate date) {
			return matcher.test(date);
		}

		public Long getSelected() {
			return selected;
		}

		public void setSelected(Long selected) {
			this.selected = selected;
		}

		public EventsCardHeader getHeader() {
			return header;
		}
	}

	private final DateTimeFormatter HEADER_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d");

	private final Map<Long, EventsCardHeader> eventsWithHeaders = new HashMap<>();
	private List<HeaderWrapper> headerChain = new ArrayList<>();

	private EventsCardHeader getRecentHeader() {
		return new EventsCardHeader("Recent", "Before this week");
	}

	private EventsCardHeader getYesterdayHeader() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		return new EventsCardHeader("Yesterday", secondaryHeaderFor(yesterday));
	}

	private EventsCardHeader getTodayHeader() {
		LocalDate today = LocalDate.now();
		return new EventsCardHeader("Today", secondaryHeaderFor(today));
	}

	private EventsCardHeader getThisWeekBeforeYesterdayHeader() {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		LocalDate thisWeekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
		return new EventsCardHeader("This week before yesterday", secondaryHeaderFor(thisWeekStart, yesterday));
	}

	private EventsCardHeader getThisWeekStartingTomorrow(boolean showPrevious) {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		LocalDate nextWeekStart = today.minusDays(today.getDayOfWeek().getValue()).plusWeeks(1);
		return new EventsCardHeader(showPrevious ? "This week starting tomorrow" : "This week",
				secondaryHeaderFor(tomorrow, nextWeekStart));
	}

	private EventsCardHeader getUpcomingHeader() {
		return new EventsCardHeader("Upcoming", "After this week");
	}

	private String secondaryHeaderFor(LocalDate date) {
		return HEADER_DATE_TIME_FORMATTER.format(date);
	}

	private String secondaryHeaderFor(LocalDate start, LocalDate end) {
		return secondaryHeaderFor(start) + " - " + secondaryHeaderFor(end);
	}

	public EventsCardHeader get(Long id) {
		return eventsWithHeaders.get(id);
	}

	public void resetHeaderChain(boolean showPrevious) {
		this.headerChain = createHeaderChain(showPrevious);
		eventsWithHeaders.clear();
	}

	public void ordersRead(List<Event> events) {
		Collections.sort(events, new Comparator<Event>() {
			@Override
			public int compare(Event o1, Event o2) {
				return o1.getDueDate().compareTo(o2.getDueDate());
			}
		});
		Iterator<HeaderWrapper> headerIterator = headerChain.stream().filter(h -> h.getSelected() == null).iterator();
		if (!headerIterator.hasNext()) {
			return;
		}

		HeaderWrapper current = headerIterator.next();
		for (Event event : events) {
			// If last selected, discard orders that match it.
			if (current.getSelected() != null && current.matches(event.getDueDate())) {
				continue;
			}
			while (current != null && !current.matches(event.getDueDate())) {
				current = headerIterator.hasNext() ? headerIterator.next() : null;
			}
			if (current == null) {
				continue;
			}
			current.setSelected(event.getId());
			eventsWithHeaders.put(event.getId(), current.getHeader());
		}
	}

	private List<HeaderWrapper> createHeaderChain(boolean showPrevious) {
		List<HeaderWrapper> headerChain = new ArrayList<>();
		LocalDate today = LocalDate.now();
		LocalDate startOfTheWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
		if (showPrevious) {
			LocalDate yesterday = today.minusDays(1);
			// Week starting on Monday
			headerChain.add(new HeaderWrapper(d -> d.isBefore(startOfTheWeek), this.getRecentHeader()));
			if (startOfTheWeek.isBefore(yesterday)) {
				headerChain.add(new HeaderWrapper(d -> d.isBefore(yesterday) && !d.isAfter(startOfTheWeek),
						this.getThisWeekBeforeYesterdayHeader()));
			}
			headerChain.add(new HeaderWrapper(yesterday::equals, this.getYesterdayHeader()));
		}
		LocalDate firstDayOfTheNextWeek = startOfTheWeek.plusDays(7);
		headerChain.add(new HeaderWrapper(today::equals, getTodayHeader()));
		headerChain.add(new HeaderWrapper(d -> d.isAfter(today) && d.isBefore(firstDayOfTheNextWeek),
				getThisWeekStartingTomorrow(showPrevious)));
		headerChain.add(new HeaderWrapper(d -> !d.isBefore(firstDayOfTheNextWeek), getUpcomingHeader()));
		return headerChain;
	}
}