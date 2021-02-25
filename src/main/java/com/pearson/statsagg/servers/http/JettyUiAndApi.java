package com.pearson.statsagg.servers.http;

import com.pearson.statsagg.servers.JettyServer;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.file_utils.FileIo;
import com.pearson.statsagg.web_ui.AlertAssociations;
import com.pearson.statsagg.web_ui.AlertDetails;
import com.pearson.statsagg.web_ui.AlertPreview;
import com.pearson.statsagg.web_ui.AlertTemplate_DerivedAlerts;
import com.pearson.statsagg.web_ui.AlertTemplateDetails;
import com.pearson.statsagg.web_ui.AlertTemplates;
import com.pearson.statsagg.web_ui.Alert_SuspensionAssociations;
import com.pearson.statsagg.web_ui.Alerts;
import com.pearson.statsagg.web_ui.AlertsReport;
import com.pearson.statsagg.web_ui.Benchmark;
import com.pearson.statsagg.web_ui.CreateAlert;
import com.pearson.statsagg.web_ui.CreateAlertTemplate;
import com.pearson.statsagg.web_ui.CreateMetricGroup;
import com.pearson.statsagg.web_ui.CreateMetricGroupTemplate;
import com.pearson.statsagg.web_ui.CreateNotificationGroup;
import com.pearson.statsagg.web_ui.CreateNotificationGroupTemplate;
import com.pearson.statsagg.web_ui.CreatePagerDutyService;
import com.pearson.statsagg.web_ui.CreateSuspension;
import com.pearson.statsagg.web_ui.CreateVariableSet;
import com.pearson.statsagg.web_ui.CreateVariableSetList;
import com.pearson.statsagg.web_ui.ForgetMetrics;
import com.pearson.statsagg.web_ui.ForgetMetricsPreview;
import com.pearson.statsagg.web_ui.HealthCheck;
import com.pearson.statsagg.web_ui.Home;
import com.pearson.statsagg.web_ui.Lookup;
import com.pearson.statsagg.web_ui.MergedRegexMetricsPreview;
import com.pearson.statsagg.web_ui.MetricAlertAssociations;
import com.pearson.statsagg.web_ui.MetricGroupAlertAssociations;
import com.pearson.statsagg.web_ui.MetricGroupDetails;
import com.pearson.statsagg.web_ui.MetricGroupMetricKeyAssociations;
import com.pearson.statsagg.web_ui.MetricGroupTemplateDetails;
import com.pearson.statsagg.web_ui.MetricGroupTemplate_DerivedMetricGroups;
import com.pearson.statsagg.web_ui.MetricGroupTemplates;
import com.pearson.statsagg.web_ui.MetricGroups;
import com.pearson.statsagg.web_ui.MetricRecentValues;
import com.pearson.statsagg.web_ui.NotificationGroupDetails;
import com.pearson.statsagg.web_ui.NotificationGroupTemplateDetails;
import com.pearson.statsagg.web_ui.NotificationGroupTemplate_DerivedNotificationGroups;
import com.pearson.statsagg.web_ui.NotificationGroupTemplates;
import com.pearson.statsagg.web_ui.NotificationGroup_AlertAssociations;
import com.pearson.statsagg.web_ui.NotificationGroups;
import com.pearson.statsagg.web_ui.OutputBlacklist;
import com.pearson.statsagg.web_ui.PagerDutyServiceDetails;
import com.pearson.statsagg.web_ui.PagerDutyServices;
import com.pearson.statsagg.web_ui.RegexTester;
import com.pearson.statsagg.web_ui.SuspensionAssociationsPreview;
import com.pearson.statsagg.web_ui.SuspensionDetails;
import com.pearson.statsagg.web_ui.Suspension_AlertAssociations;
import com.pearson.statsagg.web_ui.Suspension_MetricKeyAssociations;
import com.pearson.statsagg.web_ui.Suspensions;
import com.pearson.statsagg.web_ui.VariableSetDetails;
import com.pearson.statsagg.web_ui.VariableSetListDetails;
import com.pearson.statsagg.web_ui.VariableSetLists;
import com.pearson.statsagg.web_ui.VariableSets;
import java.io.File;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class JettyUiAndApi implements JettyServer {

    private static final Logger logger = LoggerFactory.getLogger(JettyUiAndApi.class.getName());

    private final int port_;
    private final String contextPath_;
    private final int stopServerTimeout_;
    private Server jettyServer_ = null;
    
    public JettyUiAndApi(int port, String contextPath, int stopServerTimeout) {
        port_ = port;
        
        if ((contextPath == null) || contextPath.isBlank()) contextPath_ = "/";
        else contextPath_ = "/" + contextPath;

        stopServerTimeout_ = stopServerTimeout;
    }
    
    public void startServer() {
        try {
            jettyServer_ = new Server(port_);
            jettyServer_.setStopAtShutdown(true);
            jettyServer_.setStopTimeout(stopServerTimeout_);

            GzipHandler gzipHandler = new GzipHandler();
            gzipHandler.setIncludedMethods("GET", "POST", "PUT");
            gzipHandler.setMinGzipSize(2048);
            gzipHandler.setIncludedMimeTypes("text/html", "text/html;charset=utf-8", "text/plain", "text/xml", "text/css", 
                    "application/javascript", "text/javascript", "application/x-font-woff");
            jettyServer_.setHandler(gzipHandler);
            
            ServletContextHandler servletContextHandler = new ServletContextHandler();
            gzipHandler.setHandler(servletContextHandler);
            servletContextHandler.setContextPath(contextPath_);
            DefaultServlet defaultServlet = new DefaultServlet();
            ServletHolder servletHolder = new ServletHolder("default", defaultServlet);
            
            boolean doesSrcWebappExist = FileIo.doesFileExist(new File("./src/webapp"));
            if (doesSrcWebappExist) servletHolder.setInitParameter("resourceBase", "./src/webapp/");
            else servletHolder.setInitParameter("resourceBase", "./webapp/");
            servletContextHandler.addServlet(servletHolder, "/*");

            // ui servlets
            addUiServletsToContext(servletContextHandler);

            // api servlets
            addApiServletsToContext(servletContextHandler);
            
            // start jetty
            jettyServer_.start();

            if (jettyServer_.isRunning()) {
                logger.info("Successfully started UI & API  HTTP server on port " + port_);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    private void addUiServletsToContext(ServletContextHandler servletContextHandler) {
        
        if (servletContextHandler == null) return;
        
        servletContextHandler.addServlet(AlertAssociations.class, "/AlertAssociations");
        servletContextHandler.addServlet(AlertDetails.class, "/AlertDetails");
        servletContextHandler.addServlet(AlertPreview.class, "/AlertPreview");
        servletContextHandler.addServlet(AlertTemplateDetails.class, "/AlertTemplateDetails");
        servletContextHandler.addServlet(AlertTemplate_DerivedAlerts.class, "/AlertTemplate-DerivedAlerts");   
        servletContextHandler.addServlet(AlertTemplates.class, "/AlertTemplates");
        servletContextHandler.addServlet(Alert_SuspensionAssociations.class, "/Alert-SuspensionAssociations");
        servletContextHandler.addServlet(Alerts.class, "/Alerts");
        servletContextHandler.addServlet(AlertsReport.class, "/AlertsReport");
        servletContextHandler.addServlet(Benchmark.class, "/Benchmark");
        servletContextHandler.addServlet(CreateAlert.class, "/CreateAlert");
        servletContextHandler.addServlet(CreateAlertTemplate.class, "/CreateAlertTemplate");
        servletContextHandler.addServlet(CreateMetricGroup.class, "/CreateMetricGroup");
        servletContextHandler.addServlet(CreateMetricGroupTemplate.class, "/CreateMetricGroupTemplate");
        servletContextHandler.addServlet(CreateNotificationGroup.class, "/CreateNotificationGroup");
        servletContextHandler.addServlet(CreateNotificationGroupTemplate.class, "/CreateNotificationGroupTemplate");
        servletContextHandler.addServlet(CreatePagerDutyService.class, "/CreatePagerDutyService");
        servletContextHandler.addServlet(CreateSuspension.class, "/CreateSuspension");
        servletContextHandler.addServlet(CreateVariableSet.class, "/CreateVariableSet");
        servletContextHandler.addServlet(CreateVariableSetList.class, "/CreateVariableSetList");
        servletContextHandler.addServlet(ForgetMetrics.class, "/ForgetMetrics");
        servletContextHandler.addServlet(ForgetMetricsPreview.class, "/ForgetMetricsPreview");
        servletContextHandler.addServlet(HealthCheck.class, "/HealthCheck");
        servletContextHandler.addServlet(Home.class, "/Home");
        servletContextHandler.addServlet(Lookup.class, "/Lookup");
        servletContextHandler.addServlet(MergedRegexMetricsPreview.class, "/MergedRegexMetricsPreview");
        servletContextHandler.addServlet(MetricAlertAssociations.class, "/MetricAlertAssociations");
        servletContextHandler.addServlet(MetricGroupAlertAssociations.class, "/MetricGroupAlertAssociations");
        servletContextHandler.addServlet(MetricGroupDetails.class, "/MetricGroupDetails");
        servletContextHandler.addServlet(MetricGroupMetricKeyAssociations.class, "/MetricGroupMetricKeyAssociations");
        servletContextHandler.addServlet(MetricGroups.class, "/MetricGroups");
        servletContextHandler.addServlet(MetricGroupTemplates.class, "/MetricGroupTemplates");
        servletContextHandler.addServlet(MetricGroupTemplateDetails.class, "/MetricGroupTemplateDetails");
        servletContextHandler.addServlet(MetricGroupTemplate_DerivedMetricGroups.class, "/MetricGroupTemplate-DerivedMetricGroups");
        servletContextHandler.addServlet(MetricRecentValues.class, "/MetricRecentValues");
        servletContextHandler.addServlet(NotificationGroupDetails.class, "/NotificationGroupDetails");
        servletContextHandler.addServlet(NotificationGroupTemplateDetails.class, "/NotificationGroupTemplateDetails");
        servletContextHandler.addServlet(NotificationGroupTemplate_DerivedNotificationGroups.class, "/NotificationGroupTemplate-DerivedNotificationGroups");
        servletContextHandler.addServlet(NotificationGroupTemplates.class, "/NotificationGroupTemplates");
        servletContextHandler.addServlet(NotificationGroup_AlertAssociations.class, "/NotificationGroup-AlertAssociations");
        servletContextHandler.addServlet(NotificationGroups.class, "/NotificationGroups");
        servletContextHandler.addServlet(OutputBlacklist.class, "/OutputBlacklist");
        servletContextHandler.addServlet(PagerDutyServiceDetails.class, "/PagerDutyServiceDetails");
        servletContextHandler.addServlet(PagerDutyServices.class, "/PagerDutyServices");
        servletContextHandler.addServlet(RegexTester.class, "/RegexTester");
        servletContextHandler.addServlet(SuspensionAssociationsPreview.class, "/SuspensionAssociationsPreview");
        servletContextHandler.addServlet(SuspensionDetails.class, "/SuspensionDetails");
        servletContextHandler.addServlet(Suspension_AlertAssociations.class, "/Suspension-AlertAssociations");
        servletContextHandler.addServlet(Suspension_MetricKeyAssociations.class, "/Suspension-MetricKeyAssociations");
        servletContextHandler.addServlet(Suspensions.class, "/Suspensions");
        servletContextHandler.addServlet(VariableSetDetails.class, "/VariableSetDetails");
        servletContextHandler.addServlet(VariableSetListDetails.class, "/VariableSetListDetails");
        servletContextHandler.addServlet(VariableSetLists.class, "/VariableSetLists");
        servletContextHandler.addServlet(VariableSets.class, "/VariableSets");
    }
    
    private void addApiServletsToContext(ServletContextHandler servletContextHandler) {
        
        if (servletContextHandler == null) return;
        
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.AlertCreate.class, "/api/alert-create");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.AlertDetails.class, "/api/alert-details");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.AlertEnable.class, "/api/alert-enable");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.AlertRemove.class, "/api/alert-remove");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.AlertTemplateCreate.class, "/api/alert-template-create");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.AlertTemplateDetails.class, "/api/alert-template-details");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.AlertsList.class, "/api/alerts-list");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.MetricGroupCreate.class, "/api/metric-group-create");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.MetricGroupDetails.class, "/api/metric-group-details");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.MetricGroupRemove.class, "/api/metric-group-remove");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.MetricGroupsList.class, "/api/metric-groups-list");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.NotificationGroupCreate.class, "/api/notification-group-create");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.NotificationGroupDetails.class, "/api/notification-group-details");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.NotificationGroupRemove.class, "/api/notification-group-remove");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.NotificationGroupsList.class, "/api/notification-groups-list");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.PagerDutyServiceCreate.class, "/api/pagerduty-service-create");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.PagerDutyServiceDetails.class, "/api/pagerduty-service-details");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.PagerDutyServiceRemove.class, "/api/pagerduty-service-remove");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.PagerDutyServicesList.class, "/api/pagerduty-services-list");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.SuspensionCreate.class, "/api/suspension-create");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.SuspensionDetails.class, "/api/suspension-details");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.SuspensionEnable.class, "/api/suspension-enable");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.SuspensionRemove.class, "/api/suspension-remove");
        servletContextHandler.addServlet(com.pearson.statsagg.web_api.SuspensionsList.class, "/api/suspensions-list");
    }
    
    @Override
    public boolean isRunning() {
        if (jettyServer_ == null) return false;
        
        try {
            return jettyServer_.isRunning();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
    }
    
    @Override
    public void stopServer() {
        try {
            jettyServer_.stop();
            jettyServer_ = null;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

}
