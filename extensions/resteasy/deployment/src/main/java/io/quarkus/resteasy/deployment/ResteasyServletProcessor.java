package io.quarkus.resteasy.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.Application;

import org.jboss.logging.Logger;
import org.jboss.resteasy.microprofile.config.FilterConfigSourceImpl;
import org.jboss.resteasy.microprofile.config.ServletConfigSourceImpl;
import org.jboss.resteasy.microprofile.config.ServletContextConfigSourceImpl;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;

import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.resteasy.common.deployment.ResteasyInjectionReadyBuildItem;
import io.quarkus.resteasy.runtime.ExceptionMapperRecorder;
import io.quarkus.resteasy.runtime.ResteasyFilter;
import io.quarkus.resteasy.server.common.deployment.ResteasyServerConfigBuildItem;
import io.quarkus.undertow.deployment.FilterBuildItem;
import io.quarkus.undertow.deployment.ServletBuildItem;
import io.quarkus.undertow.deployment.ServletInitParamBuildItem;
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;

/**
 * Processor that finds JAX-RS classes in the deployment
 */
public class ResteasyServletProcessor {
    private static final Logger log = Logger.getLogger("io.quarkus.resteasy");

    private static final String JAVAX_WS_RS_APPLICATION = Application.class.getName();
    private static final String JAX_RS_FILTER_NAME = JAVAX_WS_RS_APPLICATION;
    private static final String JAX_RS_SERVLET_NAME = JAVAX_WS_RS_APPLICATION;

    @BuildStep
    public void jaxrsConfig(Optional<ResteasyServerConfigBuildItem> resteasyServerConfig,
            BuildProducer<ResteasyJaxrsConfigBuildItem> resteasyJaxrsConfig, HttpRootPathBuildItem httpRootPathBuildItem) {
        if (resteasyServerConfig.isPresent()) {
            resteasyJaxrsConfig.produce(
                    new ResteasyJaxrsConfigBuildItem(httpRootPathBuildItem.adjustPath(resteasyServerConfig.get().getPath())));
        }
    }

    @BuildStep
    public void build(
            Capabilities capabilities,
            Optional<ResteasyServerConfigBuildItem> resteasyServerConfig,
            BuildProducer<FeatureBuildItem> feature,
            BuildProducer<FilterBuildItem> filter,
            BuildProducer<ServletBuildItem> servlet,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            BuildProducer<ServletInitParamBuildItem> servletInitParameters,
            ResteasyInjectionReadyBuildItem resteasyInjectionReady) throws Exception {
        if (!capabilities.isCapabilityPresent(Capabilities.SERVLET)) {
            // todo remove this info message after this is in the wild a few releases
            log.info("Resteasy running without servlet container.");
            log.info("- Add quarkus-undertow to run Resteasy within a servlet container");
            return;
        }
        feature.produce(new FeatureBuildItem(FeatureBuildItem.RESTEASY));

        if (resteasyServerConfig.isPresent()) {
            reflectiveClass.produce(new ReflectiveClassBuildItem(false, false,
                    ServletConfigSourceImpl.class,
                    ServletContextConfigSourceImpl.class,
                    FilterConfigSourceImpl.class));
            String path = resteasyServerConfig.get().getPath();

            //if JAX-RS is installed at the root location we use a filter, otherwise we use a Servlet and take over the whole mapped path
            if (path.equals("/") || path.isEmpty()) {
                filter.produce(FilterBuildItem.builder(JAX_RS_FILTER_NAME, ResteasyFilter.class.getName()).setLoadOnStartup(1)
                        .addFilterServletNameMapping("default", DispatcherType.REQUEST).setAsyncSupported(true)
                        .build());
                reflectiveClass.produce(new ReflectiveClassBuildItem(false, false, ResteasyFilter.class.getName()));
            } else {
                String mappingPath = getMappingPath(path);
                servlet.produce(ServletBuildItem.builder(JAX_RS_SERVLET_NAME, HttpServlet30Dispatcher.class.getName())
                        .setLoadOnStartup(1).addMapping(mappingPath).setAsyncSupported(true).build());
                reflectiveClass.produce(new ReflectiveClassBuildItem(false, false, HttpServlet30Dispatcher.class.getName()));
            }

            for (Entry<String, String> initParameter : resteasyServerConfig.get().getInitParameters().entrySet()) {
                servletInitParameters.produce(new ServletInitParamBuildItem(initParameter.getKey(), initParameter.getValue()));
            }
        }
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    @Record(STATIC_INIT)
    void addServletsToExceptionMapper(List<ServletBuildItem> servlets, ExceptionMapperRecorder recorder) {
        recorder.setServlets(servlets.stream().filter(s -> !JAX_RS_SERVLET_NAME.equals(s.getName()))
                .collect(Collectors.toMap(s -> s.getName(), s -> s.getMappings())));
    }

    private String getMappingPath(String path) {
        String mappingPath;
        if (path.endsWith("/")) {
            mappingPath = path + "*";
        } else {
            mappingPath = path + "/*";
        }
        return mappingPath;
    }
}
