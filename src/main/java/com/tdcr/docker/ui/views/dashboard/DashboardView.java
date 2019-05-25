package com.tdcr.docker.ui.views.dashboard;

import com.github.dockerjava.api.model.Info;
import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.backend.data.entity.DockImage;
import com.tdcr.docker.backend.data.entity.ImageDetails;
import com.tdcr.docker.backend.data.entity.Incident;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.ComputeStats;
import com.tdcr.docker.backend.utils.DataUtil;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.views.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;

@Tag("dashboard-view")
@HtmlImport("src/views/dashboard/dashboard-view.html")
@Route(value = AppConst.PAGE_DASHBOARD, layout = MainView.class)
@PageTitle(AppConst.TITLE_DASHBOARD)
public class DashboardView extends PolymerTemplate<TemplateModel> implements HasLogger {

	private static final String[] MONTH_LABELS = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
			"Aug", "Sep", "Oct", "Nov", "Dec"};
	private static  final List yearInitLst = Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0);

	private static final String[] dockerInfoKeys = new String[] {"Containers", "Running", "Paused", "Stopped", "Images", "CPUs","Memory(Gb)"};

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

	@Id("deliveriesThisYear")
	private Chart imageContainerChart;

	@Id("yearlySalesGraph")
	private Chart errorRateGraph;

	@Id("dockimageGrid")
	private Grid<DockImage> grid;

	@Id("monthlyProductSplit")
	private Chart dockerInfoBarChart;

	@Id("todayCountChart")
	private Chart totalIncCntChart;

	@Autowired
	DockerService dockerService;

	@Value("${x-axis:5}")
	int xAxis;


	private ListDataProvider<DockImage> dataProvider;

	private Map<String,ListSeries> errorMap = new HashMap<>();

	private Map<String,ListSeries> dockerInfoMap = new HashMap<>();

	ComboBox<String> dockerCombobox;

	private volatile boolean exitRunningLoop = true;

	@PostConstruct
	void init() {
		initIncClosedCountSolidgaugeChart();
		updateChartDetails(null);
		initErrorRateChart();
		initContainerCountChart();
		initGridDetails();
		setupSearchBar();
		initDockerInfo();
//		initImageContainerChart();
	}

	private void initImageContainerChart() {
		imageContainerChart.setId("chart");

		final Configuration conf = imageContainerChart.getConfiguration();

		conf.setTitle("Global happiness index");
		conf.setSubTitle("Source: www.happyplanetindex.org");
		conf.getLegend().setEnabled(false);

		XAxis x = new XAxis();
		x.setType(AxisType.CATEGORY);
		conf.addxAxis(x);

		YAxis y = new YAxis();
		y.setTitle("Total percent market share");
		conf.addyAxis(y);

		PlotOptionsColumn column = new PlotOptionsColumn();
		column.setCursor(Cursor.POINTER);
		column.setDataLabels(new DataLabels(true));

		conf.setPlotOptions(column);

		DataSeries regionsSeries = new DataSeries();
		regionsSeries.setName("Regions");
		PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
		plotOptionsColumn.setColorByPoint(true);
		regionsSeries.setPlotOptions(plotOptionsColumn);

		DataSeriesItem regionItem = new DataSeriesItem(
				"Latin America and Carribean", 60);
		DataSeries countriesSeries = new DataSeries("Countries");
		countriesSeries.setId("Latin America and Carribean Countries");

		DataSeriesItem countryItem = new DataSeriesItem("Costa Rica", 64);
		DataSeries detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Costa Rica");
		String[] categories = new String[] { "Life Expectancy",
				"Well-being (0-10)", "Footprint (gha/capita)" };
		Number[] ys = new Number[] { 79.3, 7.3, 2.5 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Colombia", 59.8);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Colombia");
		ys = new Number[] { 73.7, 6.4, 1.8 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Belize", 59.3);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Belize");
		ys = new Number[] { 76.1, 6.5, 2.1 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("El Salvador", 58.9);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details El Salvador");
		ys = new Number[] { 72.2, 6.7, 2.0 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

		regionItem = new DataSeriesItem("Western Nations", 50);

		countriesSeries = new DataSeries("Countries");
		countriesSeries.setId("Western Nations Countries");

		countryItem = new DataSeriesItem("New Zealand", 51.6);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details New Zealand");
		ys = new Number[] { 80.7, 7.2, 4.3 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Norway", 51.4);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Norway");
		ys = new Number[] { 81.1, 7.6, 4.8 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Switzerland", 50.3);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Switzerland");
		ys = new Number[] { 82.3, 7.5, 5.0 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("United Kingdom", 47.9);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details United Kingdom");
		ys = new Number[] { 80.2, 7.0, 4.7 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

		regionItem = new DataSeriesItem("Middle East and North Africa", 53);

		countriesSeries = new DataSeries("Countries");
		countriesSeries.setId("Middle East and North Africa Countries");

		countryItem = new DataSeriesItem("Israel", 55.2);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Israel");
		ys = new Number[] { 81.6, 7.4, 4.0 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Algeria", 52.2);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Algeria");
		ys = new Number[] { 73.1, 5.2, 1.6 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Jordan", 51.7);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Jordan");
		ys = new Number[] { 73.4, 5.7, 2.1 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Palestine", 51.2);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Palestine");
		ys = new Number[] { 72.8, 4.8, 1.4 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

		regionItem = new DataSeriesItem("Sub-Saharan Africa", 42);

		countriesSeries = new DataSeries("Countries");
		countriesSeries.setId("Sub-Saharan Africa Countries");

		countryItem = new DataSeriesItem("Nigeria", 51.6);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Nigeria");
		ys = new Number[] { 66.7, 4.6, 1.2 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Malawi", 42.5);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Malawi");
		ys = new Number[] { 54.2, 5.1, 0.8 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Ghana", 40.3);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Ghana");
		ys = new Number[] { 64.2, 4.6, 1.7 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Ethiopia", 39.2);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Ethiopia");
		ys = new Number[] { 59.3, 4.4, 1.1 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

		regionItem = new DataSeriesItem("South Asia", 53);

		countriesSeries = new DataSeries("Countries");
		countriesSeries.setId("South Asia Countries");

		countryItem = new DataSeriesItem("Bangladesh", 56.3);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Bangladesh");
		ys = new Number[] { 68.9, 5.0, 0.7 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Pakistan", 54.1);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Pakistan");
		ys = new Number[] { 65.4, 5.3, 0.8 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("India", 50.9);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details India");
		ys = new Number[] { 65.4, 5.0, 0.9 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Sri Lanka", 51.2);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Sri Lanka");
		ys = new Number[] { 74.9, 4.2, 1.2 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

		regionItem = new DataSeriesItem("East Asia", 55);

		countriesSeries = new DataSeries("Countries");
		countriesSeries.setId("East Asia Countries");

		countryItem = new DataSeriesItem("Vietnam", 60.4);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Vietnam");
		ys = new Number[] { 75.2, 5.8, 1.4 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Indonesia", 55.5);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Indonesia");
		ys = new Number[] { 69.4, 5.5, 1.1 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Thailand", 53.5);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Thailand");
		ys = new Number[] { 74.1, 6.2, 2.4 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		countryItem = new DataSeriesItem("Philippines", 52.4);
		detailsSeries = new DataSeries("Details");
		detailsSeries.setId("Details Philippines");
		ys = new Number[] { 68.7, 4.9, 1.0 };
		detailsSeries.setData(categories, ys);
		countriesSeries.addItemWithDrilldown(countryItem, detailsSeries);

		regionsSeries.addItemWithDrilldown(regionItem, countriesSeries);

		conf.addSeries(regionsSeries);
	}

	private void initErrorRateChart() {
		Configuration conf = errorRateGraph.getConfiguration();
		conf.getChart().setType(ChartType.AREASPLINE);
		conf.getChart().setBorderRadius(3);
		conf.setTitle("Error Rate");
		conf.getyAxis().setTickInterval(1);
		conf.getyAxis().getTitle().setText("Exceptions per sec");

		Tooltip tooltip = new Tooltip();
		tooltip.setShared(true);
		tooltip.setValueSuffix(" exception");
		conf.setTooltip(tooltip);

		XAxis xAxis = new XAxis();
		String xarray[] = new String[this.xAxis];
		int temp = (this.xAxis-1);
		for(int i = 0;i <this.xAxis;i++){
			xarray[i] = "t"+(i==temp? "":"-"+(temp-i));
		}
		xAxis.setCategories(xarray);
		conf.addxAxis(xAxis);

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
					selectedImage.getImageDetails().getTotalIncidents()+AppConst.EMPTY_STR);
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


	private void updateErrorRateConfigChart(DockImage selectedImage) {
		Configuration conf = errorRateGraph.getConfiguration();
		clearErrorRateGrapf(errorRateGraph);
		exitRunningLoop = true;
		if(selectedImage != null &&
				selectedImage.getImageDetails()!= null ){
			ImageDetails imageDetails = dockerService.getImageDetails(selectedImage.getImageId());
			if(imageDetails.getErrorMap().isEmpty()) return;
			int errorType = imageDetails.getErrorMap().size();
			Number[][] errorPerSec = new Number[errorType][xAxis];
			for (int j = 0; j<errorType;j++)
				for (int i = 0; i<xAxis;i++) {
					errorPerSec[j][i] = 0;
				}
			int k=0;
			for (String errorTypeKey:
					imageDetails.getErrorMap().keySet()) {
				if(!errorMap.containsKey(errorTypeKey)){
					ListSeries series = new ListSeries( errorTypeKey,errorPerSec[k]);
					errorMap.put(errorTypeKey,series);
					conf.addSeries(series);
					exitRunningLoop = true;
				}else{
					resetErrorRateGraph(errorMap.get(errorTypeKey));
					continue;
				}
				k++;
			}
			exitRunningLoop = false;
		}
	}

	private void updateErrorRateChart(DockImage selectedImage) {
		updateErrorRateConfigChart(selectedImage);
		if(selectedImage!=null &&
				selectedImage.getImageDetails()!=null){
			ImageDetails imageDetails = dockerService.getImageDetails(selectedImage.getImageId());
			if(imageDetails.getErrorMap().isEmpty()) return;
			Command cmd = new Command() {
					@Override
					public void execute() {
						try {
							for (String errorTypeKey:
									imageDetails.getErrorMap().keySet()) {
								ListSeries ls = errorMap.get(errorTypeKey);
								int index = selectedImage.getErrorIndex();
								int resetIndex =ls.getData().length-1;
								int errorCount = 0;
								if (index == resetIndex) {
									ImageDetails imgDtl = dockerService.getImageDetails(imageDetails.getImageId());
									errorCount = imgDtl.getErrorMap().get(errorTypeKey);
									selectedImage.setErrorIndex(index - 1);
								}else if(index >0){
									errorCount = ((Number) ls.getData()[index + 1]).intValue();
									selectedImage.setErrorIndex(index - 1);
								}else if (index == 0) {
									errorCount = ((Number) ls.getData()[index + 1]).intValue();
									selectedImage.setErrorIndex(resetIndex);
								}
								ls.updatePoint(index, errorCount);
							}
						} catch (Exception e) {}
					}
				};
			selectedImage.setErrorIndex(xAxis-1);
			runWhileAttached(errorRateGraph, cmd,500,true,this,"errorRateGraph");
		}
	}

	private void clearErrorRateGrapf(Chart errorRateGraph) {
		for (Series series :
				errorRateGraph.getConfiguration().getSeries()) {
			resetErrorRateGraph((ListSeries) series);
		}
	}

	private void resetErrorRateGraph(ListSeries ls) {
		Command resetCmd = new Command() {
			@Override
			public void execute() {
				try {
					int maxIndex =ls.getData().length-1;
					int errorCount = 0;
					for(int index = maxIndex ; index>=0;index--)
						ls.updatePoint(index, errorCount);
					ls.setName(AppConst.EMPTY_STR);
				} catch (Exception e) {}
			}
		};
		runWhileAttached(errorRateGraph, resetCmd,0,false,this,"errorRateGraph");
	}

	public void runWhileAttached(final Component component,
										final Command task, final int interval, boolean updateComponentAgain, DashboardView dashboardView, String componentName) {
		final Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					do {
						Future<Void> future = component.getUI().get().access(task);
						future.get();
						Thread.sleep(interval);
						dashboardView.getLogger().info("runWhileAttached loop for chart: {}", componentName);
					} while (component.getUI() != null && updateComponentAgain && !exitRunningLoop);
					if(updateComponentAgain){
						dashboardView.getLogger().info("Exiting runWhileAttached loop for chart: {}", componentName);
					}else{
						dashboardView.getLogger().info("No runWhileAttached loop set for chart: {}", componentName);
					}

				} catch (Exception e) {
				}
			}
		};
		thread.start();
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
			},0,false, this, "totalContainerChart");
		}else{
			runWhileAttached(totalContainerChart, new Command() {
				@Override
				public void execute() {
					ListSeries ls = (ListSeries) totalContainerChart.getConfiguration().getSeries().get(0);
					for (int i=0;i<12;i++) {
						ls.updatePoint(i,0);
					}
				}
			},0,false, this, "totalContainerChart");
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
		configuration.setSeries(new ListSeries("",0));

	}


	private void updateCircleChart(ImageDetails imageDetails) {
		Configuration configuration = totalIncCntChart.getConfiguration();
		ListSeries ls = (ListSeries) configuration.getSeries().get(0);
		runWhileAttached(totalIncCntChart, new Command() {
			@Override
			public void execute() {
				if(imageDetails!=null){
					int newPoint = Integer.valueOf(imageDetails.getTotalCloseIncidents()) ==0? 1:Integer.valueOf(imageDetails.getTotalCloseIncidents());
					configuration.fireAxesRescaled(configuration.getyAxis(),
							0,newPoint,true,true);

					ls.updatePoint(0,Integer.valueOf(imageDetails.getTotalCloseIncidents()));
				}else{
					configuration.fireAxesRescaled(configuration.getyAxis(),
							0,	1,true,true);
					ls.updatePoint(0,0);
				}

			}
		},0,false, this, "totalIncCntChart");
	}

	private void initGridDetails() {
		addGridColumns();
		grid.addSelectionListener(e ->{
			updateChartDetails(getSelectedRow());
			updateErrorRateChart(getSelectedRow());
		});
	}

	private void initDataProvider() {
		dockerService.updateDockerClient(searchBar.getComboBox().getValue());
		this.dataProvider= DataProvider.ofCollection(dockerService.listAllImages(searchBar.getComboBox().getValue()));
		grid.setDataProvider(dataProvider);
	}

	private void addGridColumns() {
		grid.addColumn(DockImage::getImageName)
				.setWidth("150px").setHeader("ImageName")
				.setFlexGrow(1).setSortable(true)
				.setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(new ComponentRenderer<>(image ->{
			NumberField minContainer= new NumberField();
			minContainer.setValue(Double.valueOf(image.getTotalContainerCount()));
			minContainer.setMin(0);
			minContainer.setMax(10);
			minContainer.setHasControls(true);
			minContainer.addValueChangeListener(e ->{
				dockerService.cloneContainerOnImage(image,e.getValue()>e.getOldValue());
				searchBar.getActionButton().click();
			});
			return minContainer;
		})).setHeader("Container Count").setWidth("120px");
		/*grid.addColumn(new ComponentRenderer<>(image -> {
			Button save = new Button("");
			save.setIcon(VaadinIcon.ENTER_ARROW.create());
			return save;
		})).setTextAlign(ColumnTextAlign.CENTER).setWidth("50px");*/
		grid.addColumn(DockImage::getSize).setHeader("Size")
				.setWidth("80px").setSortable(true).setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(new ComponentRenderer<>(image -> {
			Button update = new Button("", event -> {
					dockerService.setSubscriptionToContainer(
							image.getImageId(),!image.isSubscription(),searchBar.getComboBox().getValue()
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
		})).setKey("notify").setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn(new ComponentRenderer<>(image -> {
			Button inc = new Button("");
				if (image.getImageDetails()!= null &&
					image.getImageDetails().getTotalIncidents() >0) {
					inc.setClassName("inc-btn-show");
					inc.setIcon(VaadinIcon.BUG.create());
					inc.addClickListener(e ->{
						showIncDetails(image);
					});
				}else{
					inc.setClassName("inc-btn-hide");
				}
				inc.setWidth("10%");
				return inc;
			})).setTextAlign(ColumnTextAlign.CENTER);

		grid.setSelectionMode(Grid.SelectionMode.SINGLE);

	}

	void showIncDetails(DockImage image){
		ImageDetails imageDetails = dockerService.getImageDetails(image.getImageId());
		if(imageDetails== null ||
				imageDetails.getIncidents() == null ||
				imageDetails.getIncidents().isEmpty()) return;

		Dialog incDialog = new Dialog();
		incDialog.setOpened(true);
		incDialog.setCloseOnOutsideClick(true);
		TextField imageName = new TextField("ImageName");
		imageName.setValue(image.getImageName());

		TextField incidentNumber = new TextField("Incident");

		TextField shortDescription = new TextField("Description");
		shortDescription.getElement().setAttribute("colspan", "2");

		TextField category = new TextField("Category");

		TextField caller = new TextField("Caller");

		TextField assignedto = new TextField("AssignedTo");

		TextField incState = new TextField("State");


		ComboBox<String> incidents = new ComboBox<>();
		incidents.setLabel("OtherIncidents");

		FormLayout form = new FormLayout(imageName, incidentNumber,category,caller,assignedto,incState, shortDescription, incidents);
		Map<String,Incident> incNumberMap= new HashMap<>();
		for (Incident incident:
				imageDetails.getIncidents()) {
			incNumberMap.put(incident.getIncNumber()+"-"+incident.getIncState(),incident);
		}

		incidents.setItems(incNumberMap.keySet());
		updateIncFormFields(imageDetails.getIncidents().get(0),incidentNumber,category,caller,assignedto,incState, shortDescription);
		incidents.addValueChangeListener( e ->{
			if(StringUtils.isEmpty(e.getValue())) return;
			updateIncFormFields(incNumberMap.get(e.getValue()), incidentNumber,category,caller,assignedto,incState, shortDescription);
		});

		incDialog.add(form);
		incDialog.setWidth("800px");
		incDialog.setHeight("400px");
		incDialog.open();
	}

	private void updateIncFormFields(Incident inc, TextField incidentNumber,
									 TextField category, TextField caller, TextField assignedto,
									 TextField incState, TextField shortDescription) {
		incidentNumber.setValue(inc.getIncNumber());
		shortDescription.setValue(inc.getShortDescription());
		category.setValue(inc.getCategory());
		caller.setValue(inc.getCaller());
		assignedto.setValue(inc.getAssignedTo());
		incState.setValue(inc.getIncState());

	}


	private void setupSearchBar() {
		searchBar.getActionButton().setIcon(VaadinIcon.REFRESH.create());
		searchBar.getActionButton().addClickListener(e ->{
			if(!validateComboBoxSelection()) return;
			initDataProvider();
			updateChartDetails(getSelectedRow());
		});
		searchBar.addSearchValueChangeListener(e ->
				dataProvider.setFilter(DockImage::getImageName,
						s -> DataUtil.caseInsensitiveContains(s, searchBar.getFilter())));
		this.dockerCombobox =searchBar.getComboBox();
		dockerCombobox.setItems(dockerService.getDockerDaemons());
		dockerCombobox.setPlaceholder(AppConst.DOCKER_DAEMON_STR);
		dockerCombobox.addValueChangeListener(e -> {
			if (e.getSource().isEmpty()) {
				Notification.show("Inavlid action!");
			} else {
				dockerService.updateDockerClient(e.getValue());
				updateData(e.getValue());
				searchBar.getActionButton().click();
			}
		});
	}
	private boolean validateComboBoxSelection() {
		if(searchBar.getComboBox().getValue() == null){
			Notification.show("Please select a DockerDaemon!");
			return false;
		}else{
			return true;
		}
	}


	private DockImage getSelectedRow() {
		Set selectedItems = grid.getSelectedItems();
		if(selectedItems.isEmpty()){
			return null;
		}
		return (DockImage) selectedItems.toArray()[0];
	}

	private void initDockerInfo() {

		Configuration config = dockerInfoBarChart.getConfiguration();
		config.setTitle("Docker Info");
		dockerInfoBarChart.getConfiguration().getChart().setType(ChartType.COLUMN);

		List<Series> lst = new ArrayList();
		for (String dockerKeys :
				dockerInfoKeys) {
			ListSeries ls =new ListSeries(dockerKeys,Arrays.asList(0));
			lst.add(ls);
			dockerInfoMap.put(dockerKeys,ls);
		}
		config.setSeries(lst);
		XAxis x = new XAxis();
		x.setCrosshair(new Crosshair());
		x.setCategories("");
		config.addxAxis(x);

		YAxis y = new YAxis();
		y.setMin(0);
		y.setTitle("Count");
		config.addyAxis(y);


		PlotOptionsArea plotOptions = new PlotOptionsArea();
		config.setPlotOptions(plotOptions);

		Tooltip tooltip = new Tooltip();
		tooltip.setShared(true);
		config.setTooltip(tooltip);

	}

	void updateData(String dockerDaemon){
		Configuration conf = dockerInfoBarChart.getConfiguration();
		runWhileAttached(dockerInfoBarChart, new Command() {
			@Override
			public void execute() {
				Info dockerInfo = dockerService.getDockerInfo();
				for (String dockerKey:
					 dockerInfoMap.keySet()) {
					ListSeries ls = dockerInfoMap.get(dockerKey);
					switch (dockerKey){
						case "Containers": ls.updatePoint(0,dockerInfo.getContainers());
							continue;
						case "Running": ls.updatePoint(0,dockerInfo.getContainersRunning());
							continue;
						case "Paused": ls.updatePoint(0,dockerInfo.getContainersPaused());
							continue;
						case "Stopped": ls.updatePoint(0,dockerInfo.getContainersStopped());continue;
						case "Images": ls.updatePoint(0,dockerInfo.getImages());continue;
						case "CPUs": ls.updatePoint(0,dockerInfo.getNCPU());continue;
						case "Memory(Gb)": ls.updatePoint(0,
								Math.round(Double.valueOf(ComputeStats.calculateSize(dockerInfo.getMemTotal(),false))));continue;
					}
				}

				dockerInfoBarChart.getConfiguration().setSubTitle("Source: "+dockerDaemon);
			}
		},0,false,this,"dockerInfoBarChart");
	}
}
