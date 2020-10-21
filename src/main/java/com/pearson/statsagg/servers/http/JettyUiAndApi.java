package com.pearson.statsagg.servers.http;

import com.pearson.statsagg.servers.JettyServer;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.web_api.AlertCreate;
import com.pearson.statsagg.web_api.AlertEnable;
import com.pearson.statsagg.web_api.AlertRemove;
import com.pearson.statsagg.web_api.AlertsList;
import com.pearson.statsagg.web_api.MetricGroupCreate;
import com.pearson.statsagg.web_api.MetricGroupRemove;
import com.pearson.statsagg.web_api.MetricGroupsList;
import com.pearson.statsagg.web_api.NotificationGroupCreate;
import com.pearson.statsagg.web_api.NotificationGroupRemove;
import com.pearson.statsagg.web_api.NotificationGroupsList;
import com.pearson.statsagg.web_api.SuspensionCreate;
import com.pearson.statsagg.web_api.SuspensionEnable;
import com.pearson.statsagg.web_api.SuspensionRemove;
import com.pearson.statsagg.web_api.SuspensionsList;
import com.pearson.statsagg.web_ui.AlertAssociations;
import com.pearson.statsagg.web_ui.AlertDetails;
import com.pearson.statsagg.web_ui.AlertPreview;
import com.pearson.statsagg.web_ui.Alert_SuspensionAssociations;
import com.pearson.statsagg.web_ui.Alerts;
import com.pearson.statsagg.web_ui.AlertsReport;
import com.pearson.statsagg.web_ui.Benchmark;
import com.pearson.statsagg.web_ui.CreateAlert;
import com.pearson.statsagg.web_ui.CreateMetricGroup;
import com.pearson.statsagg.web_ui.CreateNotificationGroup;
import com.pearson.statsagg.web_ui.CreateSuspension;
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
import com.pearson.statsagg.web_ui.MetricGroups;
import com.pearson.statsagg.web_ui.MetricRecentValues;
import com.pearson.statsagg.web_ui.NotificationGroupDetails;
import com.pearson.statsagg.web_ui.NotificationGroup_AlertAssociations;
import com.pearson.statsagg.web_ui.NotificationGroups;
import com.pearson.statsagg.web_ui.OutputBlacklist;
import com.pearson.statsagg.web_ui.RegexTester;
import com.pearson.statsagg.web_ui.SuspensionAssociationsPreview;
import com.pearson.statsagg.web_ui.SuspensionDetails;
import com.pearson.statsagg.web_ui.Suspension_AlertAssociations;
import com.pearson.statsagg.web_ui.Suspension_MetricKeyAssociations;
import com.pearson.statsagg.web_ui.Suspensions;
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
            servletHolder.setInitParameter("resourceBase", "./src/webapp/");
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
        servletContextHandler.addServlet(Alert_SuspensionAssociations.class, "/Alert-SuspensionAssociations");
        servletContextHandler.addServlet(Alerts.class, "/Alerts");
        servletContextHandler.addServlet(AlertsReport.class, "/AlertsReport");
        servletContextHandler.addServlet(Benchmark.class, "/Benchmark");
        servletContextHandler.addServlet(CreateAlert.class, "/CreateAlert");
        servletContextHandler.addServlet(CreateMetricGroup.class, "/CreateMetricGroup");
        servletContextHandler.addServlet(CreateNotificationGroup.class, "/CreateNotificationGroup");
        servletContextHandler.addServlet(CreateSuspension.class, "/CreateSuspension");
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
        servletContextHandler.addServlet(MetricRecentValues.class, "/MetricRecentValues");
        servletContextHandler.addServlet(NotificationGroupDetails.class, "/NotificationGroupDetails");
        servletContextHandler.addServlet(NotificationGroup_AlertAssociations.class, "/NotificationGroup-AlertAssociations");
        servletContextHandler.addServlet(NotificationGroups.class, "/NotificationGroups");
        servletContextHandler.addServlet(OutputBlacklist.class, "/OutputBlacklist");
        servletContextHandler.addServlet(RegexTester.class, "/RegexTester");
        servletContextHandler.addServlet(SuspensionAssociationsPreview.class, "/SuspensionAssociationsPreview");
        servletContextHandler.addServlet(SuspensionDetails.class, "/SuspensionDetails");
        servletContextHandler.addServlet(Suspension_AlertAssociations.class, "/Suspension-AlertAssociations");
        servletContextHandler.addServlet(Suspension_MetricKeyAssociations.class, "/Suspension-MetricKeyAssociations");
        servletContextHandler.addServlet(Suspensions.class, "/Suspensions");
    }
    
    private void addApiServletsToContext(ServletContextHandler servletContextHandler) {
        
        if (servletContextHandler == null) return;
        
        servletContextHandler.addServlet(AlertCreate.class, "/api/alert-create");
        servletContextHandler.addServlet(AlertDetails.class, "/api/alert-details");
        servletContextHandler.addServlet(AlertEnable.class, "/api/alert-enable");
        servletContextHandler.addServlet(AlertRemove.class, "/api/alert-remove");
        servletContextHandler.addServlet(AlertsList.class, "/api/alerts-list");
        servletContextHandler.addServlet(MetricGroupCreate.class, "/api/metric-group-create");
        servletContextHandler.addServlet(MetricGroupDetails.class, "/api/metric-group-details");
        servletContextHandler.addServlet(MetricGroupRemove.class, "/api/metric-group-remove");
        servletContextHandler.addServlet(MetricGroupsList.class, "/api/metric-groups-list");
        servletContextHandler.addServlet(NotificationGroupCreate.class, "/api/notification-group-create");
        servletContextHandler.addServlet(NotificationGroupDetails.class, "/api/notification-group-details");
        servletContextHandler.addServlet(NotificationGroupRemove.class, "/api/notification-group-remove");
        servletContextHandler.addServlet(NotificationGroupsList.class, "/api/notification-groups-list");
        servletContextHandler.addServlet(SuspensionCreate.class, "/api/suspension-create");
        servletContextHandler.addServlet(SuspensionDetails.class, "/api/suspension-details");
        servletContextHandler.addServlet(SuspensionEnable.class, "/api/suspension-enable");
        servletContextHandler.addServlet(SuspensionRemove.class, "/api/suspension-remove");
        servletContextHandler.addServlet(SuspensionsList.class, "/api/suspensions-list");
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
