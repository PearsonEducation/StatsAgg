# StatsAgg

## Overview
StatsAgg is a metric aggregation and alerting platform. It currently accepts Graphite-formatted metrics, OpenTSDB-formatted metrics, and StatsD-formatted metrics.

StatsAgg works by receiving Graphite, StatsD, and OpenTSDB metrics, (optionally) aggregating them, alerting on them, and outputting them to a metric storage platform. In essence, StatsAgg is a middle-man that sits between the metric sender & the metric storage applications. The 'value add' is metric aggregation (Graphite & StatsD), and providing a common alerting platform for all supported metric types. The diagram (see below diagram) shows an example deployment & use-case pattern for StatsAgg.

[StatsAgg component diagram](./docs/component-diagram.png)

<br>

## What are StatsAgg's core features?

* Receives and aggregates StatsD metrics
    * StatsAgg contains a complete re-implementation of StatsD
	* All metric-types are fully supported (and in some cases, enhanced)
    * TCP & UDP support (both can run concurrently)
* Receives (and optionally, aggregates) Graphite metrics
    * TCP & UDP support (both can run concurrently)
    * Can aggregates into minimum, average, maximum, median, count, rate, and sum for the values of an individual metric-path (during an 'aggregation window').
	* Aggregation works similarly to StatsD timer aggregation, but for Graphite metrics.
* Receives OpenTSDB metrics
    * Telnet & HTTP interfaces are supported
	* Supports GZIP compression on HTTP interface 
* Outputs metrics to metric storage platforms
    * Graphite (and Graphite compatible services)
    * OpenTSDB (via telnet & HTTP)
    * Outputting to a storage engine is completely optional; you can send metrics into StatsAgg without having them forwarded to a metric storage solution. This also means that if you're having issues with your metric storage solution, StatsAgg will still be available & capable of alerting off the metrics that it receives.
* A robust alerting mechanism 
    * Can alert off of any received/aggregated metric
    * Regular-expression based mechanism for tying metrics & alerts together
    * 'threshold' or 'availability' based alerting
    * A flexible alert & metric suspension mechanism
    * Alerts notifications can be sent via email or viewed in the StatsAgg website
* A web-based UI for managing alerts & metrics

A more detailed discussion of StatsAgg's features can be found in the [StatsAgg user manual](./docs/manual.pdf)

<br>

## Why should I use StatsAgg?

StatsAgg was originally written to fill some gaps that in some other popular open-source monitoring tools. Specifically...

* Graphite, StatsD, InfluxDB, and OpenTSDB do not have native alerting mechanisms
    * Most alerting solutions for StatsD, Graphite, InfluxDB, and/or OpenTSDB metrics are provided by (expensive) SaaS venders.
    * The alerting mechanism in StatsAgg compares favourably to many pay-based solutions.
* StatsAgg can act as a sort of 'metric transcoder' between various technologies. 
    * It allows any combination of input metrics StatsD, Graphite, InfluxDB, and OpenTSDB metrics to be output to OpenTSDB, Graphite, InfluxDB, etc.
    * Support for more input & output formats will increase as StatsAgg evolves.
* StatsAgg provides an alternative way of managing servers/services/etc compared to tools like Nagios, Zabbix, etc
    * StatsAgg allows you to break away from viewing everything from the perspective of servers/hosts. You could structure your metrics to group everything by host, but you aren't required to.
    * Generally speaking, tools like Nagios, Zabbix, etc lack the ability to alert off of free-form metrics. Since StatsAgg uses regular-expressions to tie metrics to alerts, you can just as easily alert off of a 'free-form metric hierarchy' as you can a 'highly structured metric hierarchy'.
    * Tools like Nagios, Zabbix, etc aren't built around having fine data point granularity (more frequent than once per minute). StatsAgg was written to be able to receive, aggregate, alert on, and output metrics that are sent at any interval.
* StatsD limitations
    * StatsD doesn't allow the TCP server & the UDP server to be active at the same time. StatsAgg does.
    * StatsD doesn't persist Gauge metrics, so a restart will result of StatsD 'forgetting' what the previous Gauge metric values were. StatsAgg can persists Gauge values, so a restart won't result in Gauges resetting themselves.
* StatsAgg provides a web-based UI for managing alerts & metrics.
* Performance
    * StatsAgg is Java-based, and has been thoroughly tuned for performance.
    * A server with 2 cpu cores & 4 gigabytes of RAM should have no trouble processing tens-of-thousands of metrics per second.
	* A server with 4+ cores & 8+ gigabytes of RAM can process hundreds-of-thousands to millions of metrics per second.

<br>

## What isn't StatsAgg?
StatsAgg aims only to fill a gap in open-source monitoring tool stack. The biggest void that it is filling is answering the question of "how can we alert off of all these metrics that we're collecting". 

StatsAgg is not, and likely never will be, a solution for:

* Metrics dashboarding. There are many great tools on the market that accomplish this. For example, Grafana.
* Metric storage. OpenTSDB, Graphite, InfluxDB, etc all are specifically made for metric storage, whereas StatsAgg is mainly meant to function as a metric 'pass-through'.

<br>

## Screenshots
* [Homepage](./docs/home.png)
* [Alerts List](./docs/alerts.png)
* [Create an Alert](./docs/create_alert.png)
* [A preview of an email alert](./docs/preview_alert.png)
* [Suspensions List](./docs/alert_suspensions.png)
* [Create a Suspension](./docs/create_suspension.png)
* [Metric Groups List](./docs/metric_groups.png)
* [Create a Metric Group](./docs/create_metricgroup.png)
* [Notification Groups List](./docs/notification_groups.png)

<br>

## Installation
Detailed installation instructions can be found in the [StatsAgg user manual](./docs/manual.pdf)

<br>

## Example programs/frameworks/etc that are compatible with StatsAgg
* [Java Metrics](https://dropwizard.github.io/metrics)
* [CollectD](https://collectd.org/)
* [tcollector](https://github.com/OpenTSDB/tcollector/)
* [scollector](https://github.com/bosun-monitor/bosun/tree/master/cmd/scollector)
* [StatsPoller](https://github.com/PearsonEducation/StatsPoller/) 
* Anything that can output in Graphite, StatsD, OpenTSDB, or InfluxDB (0.6x - 0.8x) format

<br>

## Accepted input formats
Detailed information about StatsAgg's metric format support, including examples, can be found in the [StatsAgg user manual](./docs/manual.pdf)

<br>

## Technology
* StatsAgg is a Java 11 based standalone app. A valid JRE (version 17 or newer) is the only requirement to run StatsAgg.
* StatsAgg uses a database for storing things like 'StatsD gauge values', alert definitions & statuses, metric group definitions, etc. The database technology can be Apache Derby or MySQL 5.7+.
* StatsAgg can run on almost any modern OS. Windows, Linux, etc.

<br>

## Limitations
* StatsAgg only supports running in a single-server configuration. 
    * While this is a limitation, a lot of time/energy was put into tuning StatsAgg's performance. For *most* implementations, a single StatsAgg server should be adequate. 
* A few StatsD features that are in the main StatsD program are missing in StatsAgg. Missing features include...
    * configuring StatsAgg to be a 'repeater' (you can use the official StatsD program to do this, and forward to StatsAgg).
    * configuring StatsAgg to be in a 'proxy cluster' configuration (you can use the official StatsD program to do this, and forward to StatsAgg). 
    * outputting frequently sent metric-keys to log files.
* StatsAgg is not (currently) meant for use as an incident management tool. It doesn't support alert history, event management, etc.
* StatsAgg does not (currently) support the Graphite 'Pickle' format (may be included in a future build).
* OpenTSDB listens for metrics on a single port for the telnet & HTTP formats. StatsAgg listens for OpenTSDB metrics on two different ports. See the [manual](./docs/manual.pdf) for more information.
* The OpenTSDB HTTP interface has full support for the 'summary' parameter, and partial support for the 'details' parameter. See the OpenTSDB documentation for more information.
    * The 'details' parameter will return accurate counts for 'failed' & 'success', but the 'errors' field is not currently being populated.

<br>

## Thanks to...
* StatsD : https://github.com/statsd/statsd
* Graphite : https://graphiteapp.org/
* OpenTSDB : http://opentsdb.net/
* InfluxDB : https://www.influxdata.com/
* Pearson : https://www.pearson.com/