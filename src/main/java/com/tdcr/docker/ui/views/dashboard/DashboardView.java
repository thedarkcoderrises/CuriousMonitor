package com.tdcr.docker.ui.views.dashboard;

import com.tdcr.docker.backend.data.entity.DockImage;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.DataUtil;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.views.MainView;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Tag("dashboard-view")
@HtmlImport("src/views/dashboard/dashboard-view.html")
@Route(value = AppConst.PAGE_DASHBOARD, layout = MainView.class)
@PageTitle(AppConst.TITLE_DASHBOARD)
public class DashboardView extends PolymerTemplate<TemplateModel> {

	private static final String[] MONTH_LABELS = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
			"Aug", "Sep", "Oct", "Nov", "Dec"};

	@Id("search")
	private SearchBar searchBar;

	@Id("totalIncCloseCnt")
	private DashboardCounterLabel totalIncCloseCnt;

	@Id("totalIncOpenCnt")
	private DashboardCounterLabel totalIncOpenCnt;

	@Id("totalIncCnt")
	private DashboardCounterLabel totalIncCnt;

	@Id("totalActiveContainers")
	private DashboardCounterLabel totalActiveContainers;

	@Id("deliveriesThisMonth")
	private Chart totalContsinerChart;
//
//	@Id("deliveriesThisYear")
//	private Chart deliveriesThisYearChart;
//
	@Id("yearlySalesGraph")
	private Chart artErrorRateGraph;
//
	@Id("dockimageGrid")
	private Grid<DockImage> grid;
//
//	@Id("monthlyProductSplit")
//	private Chart monthlyProductSplit;

	@Id("todayCountChart")
	private Chart totalIncCntChart;

	@Autowired
	DockerService dockerService;

	private ListDataProvider<DockImage> dataProvider;

	ComboBox<String> dockerCombobox;

	@PostConstruct
	void init() {
		totalIncCnt.setData("Total Incidents","","10");
		totalIncOpenCnt.setData("Open Incidents","","2");
		totalIncCloseCnt.setData("Closed Incidents", "Next Release 10/05", "8");
		totalActiveContainers.setData("Running Containers", "Out of 5","2");
		initTodayCountSolidgaugeChart();
		populateArtErrorRateChart();
		populateTotalContainerRunningCharts();
		initGridDetails();
		setupSearchBar();
	}

	private void populateArtErrorRateChart() {
		Configuration conf = artErrorRateGraph.getConfiguration();
		conf.getChart().setType(ChartType.AREASPLINE);
		conf.getChart().setBorderRadius(3);

		conf.setTitle("Average Response & Error Rate");

		conf.getxAxis().setVisible(false);
		conf.getxAxis().setCategories(MONTH_LABELS);

		conf.getyAxis().getTitle().setText(null);

		Number[][][] salesPerMonth = new Number[2][2][12];
		for(int k=0;k<2;k++)
		for (int j = 0; j<2;j++)
		for (int i = 0; i<12;i++) {
			double randomDouble = Math.random();
			randomDouble = randomDouble * 30 + 1;
			int randomInt = (int) randomDouble;
			salesPerMonth[k][j][i] = randomInt;
		}

		for(int k=0;k<2;k++){
			String label = "C"+(k+1)+"-ART";
			for (int i = 0; i < 2; i++) {
				if(i == 1){
					label ="C"+(k+1)+"-ERROR";
				}
				conf.addSeries(new ListSeries(label,salesPerMonth[k][i]));
			}
		}
	}


	private void populateTotalContainerRunningCharts() {
		LocalDate today = LocalDate.now();

		Configuration containerConf = totalContsinerChart.getConfiguration();
		configureColumnChart(containerConf);

		List<Number> totalContaiersList = Arrays.asList(1,4,5,5,5,4,2,2);
		String[] deliveriesThisMonthCategories = IntStream.rangeClosed(1, totalContaiersList.size())
				.mapToObj(String::valueOf).toArray(String[]::new);

		containerConf.setTitle("Containers in " + DataUtil.getFullMonthName(today));
		containerConf.getxAxis().setCategories(deliveriesThisMonthCategories);
		containerConf.addSeries(new ListSeries("per Day", totalContaiersList));
	}
	private void configureColumnChart(Configuration conf) {
		conf.getChart().setType(ChartType.COLUMN);
		conf.getChart().setBorderRadius(4);

		conf.getxAxis().setTickInterval(1);
		conf.getxAxis().setMinorTickLength(0);
		conf.getxAxis().setTickLength(0);

		conf.getyAxis().getTitle().setText(null);

		conf.getLegend().setEnabled(false);
	}


	private void initTodayCountSolidgaugeChart() {
		Configuration configuration = totalIncCntChart.getConfiguration();
		configuration.getChart().setType(ChartType.SOLIDGAUGE);
		configuration.setTitle("");
		configuration.getTooltip().setEnabled(false);

		configuration.getyAxis().setMin(0);
		configuration.getyAxis().setMax(10);
		configuration.getyAxis().getLabels().setEnabled(false);

		PlotOptionsSolidgauge opt = new PlotOptionsSolidgauge();
		opt.getDataLabels().setEnabled(false);
		PlotOptionsWaterfall pow = new PlotOptionsWaterfall();
		configuration.setPlotOptions(opt);

		DataSeriesItemWithRadius point = new DataSeriesItemWithRadius();
		point.setY(8);
		point.setInnerRadius("100%");
		point.setRadius("110%");
		configuration.setSeries(new DataSeries(point));

		Pane pane = configuration.getPane();
		pane.setStartAngle(0);
		pane.setEndAngle(360);

		Background background = new Background();
		background.setShape(BackgroundShape.ARC);
		background.setInnerRadius("100%");
		background.setOuterRadius("110%");
		pane.setBackground(background);
	}

	private void initGridDetails() {
		addGridColumns();
//		initDataProvider();
	}

	private void initDataProvider() {
		dockerService.updateDockerClient("LOCAL_DD");
		this.dataProvider= DataProvider.ofCollection(dockerService.listAllImages());
		grid.setDataProvider(dataProvider);
	}

	private void addGridColumns() {
		grid.addColumn(DockImage::getImageName)
				.setWidth("150px").setHeader("ImageName")
				.setFlexGrow(1).setSortable(true)
				.setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(DockImage::getTotalContainerCount)
				.setWidth("100px").setHeader("Total Containers")
				.setFlexGrow(1).setSortable(true)
				.setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(DockImage::getSize).setHeader("Size")
				.setWidth("100px").setSortable(true);
		grid.addColumn(new ComponentRenderer<>(image -> {
			Button update = new Button("", event -> {
					dockerService.setSubscriptionToContainer(
							image.getImageId(),!image.isSubscription()
					);
					String msg ="Subscription removed";
					if(!image.isSubscription()){
						msg = "Subscribed ";
					}
					Notification.show(msg,1000,Notification.Position.MIDDLE);
				searchBar.getActionButton().click();
			});
			if(image.isSubscription()){
				update.setIcon(VaadinIcon.BELL.create());
			}else{
				update.setIcon(VaadinIcon.BELL_O.create());
			}
			return update;
		})).setKey("notify").setTextAlign(ColumnTextAlign.CENTER);;

		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
	}

	private void setupSearchBar() {
		searchBar.getActionButton().setIcon(VaadinIcon.REFRESH.create());
		searchBar.getActionButton().addClickListener(e ->{
			initDataProvider();
		});
		searchBar.addSearchValueChangeListener(e ->
				dataProvider.setFilter(DockImage::getImageName,
						s -> DataUtil.caseInsensitiveContains(s, searchBar.getFilter())));
		this.dockerCombobox =searchBar.getComboBox();
		dockerCombobox.setItems(dockerService.getDockerDeamons());
		dockerCombobox.setPlaceholder(AppConst.DOCKER_DEAMON_STR);
		dockerCombobox.addValueChangeListener(e -> {
			if (e.getSource().isEmpty()) {
				Notification.show("Inavlid action!");
			} else {
				dockerService.updateDockerClient(e.getValue());
				searchBar.getActionButton().click();
			}
		});
	}

}
