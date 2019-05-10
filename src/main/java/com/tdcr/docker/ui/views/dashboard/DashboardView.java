package com.tdcr.docker.ui.views.dashboard;

import com.tdcr.docker.backend.data.entity.DockImage;
import com.tdcr.docker.backend.data.entity.ImageDetails;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.DataUtil;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.views.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.server.Command;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

@Tag("dashboard-view")
@HtmlImport("src/views/dashboard/dashboard-view.html")
@Route(value = AppConst.PAGE_DASHBOARD, layout = MainView.class)
@PageTitle(AppConst.TITLE_DASHBOARD)
public class DashboardView extends PolymerTemplate<TemplateModel> {

	private static final String[] MONTH_LABELS = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
			"Aug", "Sep", "Oct", "Nov", "Dec"};
	private static  final List yearInitLst = Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0);

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
	private Chart totalContainerChart;
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
		initIncClosedCountSolidgaugeChart();
		updateChartDetails(null);
		populateArtErrorRateChart();
		initContainerCountChart();
		initGridDetails();
		setupSearchBar();
	}

	private void updateChartDetails(DockImage selectedImage) {
		String value = AppConst.ZERO_STR;
		if (selectedImage == null || selectedImage.getImageDetails()== null){
			totalIncCnt.setData("Total Incidents","",value);
			totalIncOpenCnt.setData("Open Incidents","",value);
			totalIncCloseCnt.setData("Closed Incidents", "Next Release: ", value);
			totalActiveContainers.setData("Running Containers", "Out of "+
							((selectedImage!=null) ?selectedImage.getTotalContainerCount()+"":value),
							(selectedImage!=null) ?selectedImage.getRunningContainerCount()+"":value);
			updateCircleChart(null);
		}else if(selectedImage.getImageDetails()!=null){
			totalIncCnt.setData("Total Incidents","",
					selectedImage.getImageDetails().getTotalIncidents());
			totalIncOpenCnt.setData("Open Incidents","",
					selectedImage.getImageDetails().getTotalOpenIncidents());
			totalIncCloseCnt.setData("Closed Incidents",
					"Next Release: ",
					selectedImage.getImageDetails().getTotalCloseIncidents());
			totalActiveContainers.setData("Running Containers",
					"Out of"+selectedImage.getRunningContainerCount()
							+"/"+selectedImage.getTotalContainerCount(),
					(selectedImage.getRunningContainerCount())+AppConst.EMPTY_STR);
			updateCircleChart(selectedImage.getImageDetails());
		}
		updateContainerCountChart(selectedImage);

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
			salesPerMonth[k][j][i] = 0;
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


	private void initContainerCountChart() {
		LocalDate today = LocalDate.now();

		Configuration containerConf = totalContainerChart.getConfiguration();
		configureColumnChart(containerConf);
		containerConf.setTitle("Containers in " + today.getYear());
		containerConf.getxAxis().setCategories(MONTH_LABELS);
		containerConf.addSeries(new ListSeries("per Month", yearInitLst));
	}

	private void updateContainerCountChart(DockImage selectedImage) {

		if(selectedImage != null){
			Map<Integer,Integer> containerCntMap = selectedImage.getContainerEntry();
			runWhileAttached(totalContainerChart, new Command() {
				@Override
				public void execute() {
					ListSeries ls = (ListSeries) totalContainerChart.getConfiguration().getSeries().get(0);
					for (int i=0;i<12;i++) {
						int value= containerCntMap.get(i)==null? 0:containerCntMap.get(i);
						ls.updatePoint(i,value);
					}
				}
			},1000);
		}else{
			runWhileAttached(totalContainerChart, new Command() {
				@Override
				public void execute() {
					ListSeries ls = (ListSeries) totalContainerChart.getConfiguration().getSeries().get(0);
					for (int i=0;i<12;i++) {
						ls.updatePoint(i,0);
					}
				}
			},1000);
		}

	}
	private void configureColumnChart(Configuration conf) {
		conf.getChart().setType(ChartType.COLUMN);
		conf.getChart().setBorderRadius(3);
		conf.getyAxis().setTickInterval(1);

		conf.getxAxis().setTickInterval(1);
		conf.getxAxis().setMinorTickLength(0);
		conf.getxAxis().setTickLength(0);

		conf.getyAxis().getTitle().setText(null);

		conf.getLegend().setEnabled(false);
	}


	private void initIncClosedCountSolidgaugeChart() {
		Configuration configuration = totalIncCntChart.getConfiguration();
		configuration.getChart().setType(ChartType.SOLIDGAUGE);

		configuration.setTitle("");
		configuration.getTooltip().setEnabled(false);
		configuration.getChart().setReflow(true);

		configuration.getyAxis().setMin(0);
		configuration.getyAxis().setMax(1);
		configuration.getyAxis().getLabels().setEnabled(false);

		PlotOptionsSolidgauge opt = new PlotOptionsSolidgauge();
		opt.getDataLabels().setEnabled(false);
		configuration.setPlotOptions(opt);

//		DataSeriesItemWithRadius point = new DataSeriesItemWithRadius();
//		point.setY(0);
//		point.setInnerRadius("100%");
//		point.setRadius("110%");
//		configuration.setSeries(new DataSeries(point));
		configuration.setSeries(new ListSeries("",0));

	}


	private void updateCircleChart(ImageDetails imageDetails) {
		Configuration configuration = totalIncCntChart.getConfiguration();
		ListSeries ls = (ListSeries) configuration.getSeries().get(0);
		runWhileAttached(totalIncCntChart, new Command() {
			@Override
			public void execute() {
				if(imageDetails!=null){
					configuration.fireAxesRescaled(configuration.getyAxis(),
							0,Integer.valueOf(imageDetails.getTotalIncidents()),true,true);
					ls.updatePoint(0,Integer.valueOf(imageDetails.getTotalCloseIncidents()));
				}else{
					configuration.fireAxesRescaled(configuration.getyAxis(),
							0,	1,true,true);
					ls.updatePoint(0,0);
				}

			}
		},1000);
	}

	public static void runWhileAttached(final Component component,
										final Command task, final int interval) {
		// Until reliable push available in our demo servers
		UI.getCurrent().setPollInterval(interval);

		final Thread thread = new Thread() {
			@Override
			public void run() {
				try {
						Future<Void> future = component.getUI().get().access(task);
						future.get();
				} catch (Exception e) {
				}
			}
		};
		thread.start();
	}


	private void initGridDetails() {
		addGridColumns();
		grid.addSelectionListener(e ->{
			updateChartDetails(getSelectedRow());
		});
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
			updateChartDetails(getSelectedRow());
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


	private DockImage getSelectedRow() {
		Set selectedItems = grid.getSelectedItems();
		if(selectedItems.isEmpty()){
			return null;
		}
		return (DockImage) selectedItems.toArray()[0];
	}
}
