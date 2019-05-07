package com.tdcr.docker.ui.components;

import com.tdcr.docker.ui.views.dashboard.DashboardCounterLabel;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

@Tag("stats-board-view")
@HtmlImport("src/components/stats-board-view.html")
public class StatsBoardView extends PolymerTemplate<StatsBoardView.StatsBoardViewModel> {

    @Id("memoryStats")
    private Chart memoryStats;

    @Id("networkIO")
    private DashboardCounterLabel networkIO;

    public StatsBoardView() {
    }

    public StatsBoardView init(){
        this.memoryStats = new Chart();
        this.networkIO = new DashboardCounterLabel();
        this.networkIO.init();
        return this;
    }

    public interface StatsBoardViewModel extends TemplateModel {
        // Add setters and getters for template properties here.
    }

    public Chart getMemoryStats() {
        return memoryStats;
    }

    public DashboardCounterLabel getNetworkIO() {
        return networkIO;
    }
}
