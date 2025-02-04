package io.quarkus.annotation.processor.generate_doc;

import static io.quarkus.annotation.processor.generate_doc.DocGeneratorUtil.AGROAL_API_JAVA_DOC_SITE;
import static io.quarkus.annotation.processor.generate_doc.DocGeneratorUtil.OFFICIAL_JAVA_DOC_BASE_LINK;
import static io.quarkus.annotation.processor.generate_doc.DocGeneratorUtil.VERTX_JAVA_DOC_SITE;
import static io.quarkus.annotation.processor.generate_doc.DocGeneratorUtil.appendConfigItemsIntoExistingOnes;
import static io.quarkus.annotation.processor.generate_doc.DocGeneratorUtil.computeExtensionDocFileName;
import static io.quarkus.annotation.processor.generate_doc.DocGeneratorUtil.getJavaDocSiteLink;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.annotation.processor.Constants;

public class DocGeneratorUtilTest {
    @Test
    public void shouldReturnEmptyListForPrimitiveValue() {
        String value = getJavaDocSiteLink("int");
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink("long");
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink("float");
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink("boolean");
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink("double");
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink("char");
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink("short");
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink("byte");
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink(Boolean.class.getName());
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink(Byte.class.getName());
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink(Short.class.getName());
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink(Integer.class.getName());
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink(Long.class.getName());
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink(Float.class.getName());
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink(Double.class.getName());
        assertEquals(Constants.EMPTY, value);

        value = getJavaDocSiteLink(Character.class.getName());
        assertEquals(Constants.EMPTY, value);
    }

    @Test
    public void shouldReturnALinkToOfficialJavaDocIfIsJavaOfficialType() {
        String value = getJavaDocSiteLink(String.class.getName());
        assertEquals(OFFICIAL_JAVA_DOC_BASE_LINK + "java/lang/String.html", value);

        value = getJavaDocSiteLink(InetAddress.class.getName());
        assertEquals(OFFICIAL_JAVA_DOC_BASE_LINK + "java/net/InetAddress.html", value);

        value = getJavaDocSiteLink(BigInteger.class.getName());
        assertEquals(OFFICIAL_JAVA_DOC_BASE_LINK + "java/math/BigInteger.html", value);

        value = getJavaDocSiteLink(Duration.class.getName());
        assertEquals(OFFICIAL_JAVA_DOC_BASE_LINK + "java/time/Duration.html", value);

        value = getJavaDocSiteLink((Map.Entry.class.getName().replace('$', '.')));
        assertEquals(OFFICIAL_JAVA_DOC_BASE_LINK + "java/util/Map.Entry.html", value);

        value = getJavaDocSiteLink(Map.Entry.class.getName());
        assertEquals(OFFICIAL_JAVA_DOC_BASE_LINK + "java/util/Map.Entry.html", value);
    }

    @Test
    public void shouldReturnALinkToAgroalJavaDocIfTypeIsDeclaredInAgroalPackage() {
        String value = getJavaDocSiteLink(
                "io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation");
        assertEquals(AGROAL_API_JAVA_DOC_SITE
                + "io/agroal/api/configuration/AgroalConnectionFactoryConfiguration.TransactionIsolation.html", value);

        value = getJavaDocSiteLink("io.agroal.api.AgroalDataSource.FlushMode");
        assertEquals(AGROAL_API_JAVA_DOC_SITE + "io/agroal/api/AgroalDataSource.FlushMode.html", value);
    }

    @Test
    public void shouldReturnALinkToVertxJavaDocIfTypeIsDeclaredInVertxPackage() {
        String value = getJavaDocSiteLink(
                "io.vertx.core.Context");
        assertEquals(VERTX_JAVA_DOC_SITE + "io/vertx/core/Context.html", value);

        value = getJavaDocSiteLink("io.vertx.amqp.AmqpMessage");
        assertEquals(VERTX_JAVA_DOC_SITE + "io/vertx/amqp/AmqpMessage.html", value);
    }

    @Test
    public void shouldReturnEmptyLinkIfUnknownJavaDocType() {
        String value = getJavaDocSiteLink("io.quarkus.ConfigDocKey");
        assertEquals(Constants.EMPTY, value);
    }

    @Test
    public void shouldReturnConfigRootName() {
        String configRoot = "org.acme.ConfigRoot";
        String expected = "org.acme.ConfigRoot.adoc";
        String fileName = computeExtensionDocFileName(configRoot);
        assertEquals(expected, fileName);
    }

    @Test
    public void shouldAddCoreInComputedExtensionName() {
        String configRoot = "io.quarkus.runtime.RuntimeConfig";
        String expected = "quarkus-core-runtime-config.adoc";
        String fileName = computeExtensionDocFileName(configRoot);
        assertEquals(expected, fileName);

        configRoot = "io.quarkus.deployment.BuildTimeConfig";
        expected = "quarkus-core-build-time-config.adoc";
        fileName = computeExtensionDocFileName(configRoot);
        assertEquals(expected, fileName);

        configRoot = "io.quarkus.deployment.path.BuildTimeConfig";
        expected = "quarkus-core-build-time-config.adoc";
        fileName = computeExtensionDocFileName(configRoot);
        assertEquals(expected, fileName);
    }

    @Test
    public void shouldGuessArtifactId() {
        String configRoot = "io.quarkus.agroal.Config";
        String expected = "quarkus-agroal.adoc";
        String fileName = computeExtensionDocFileName(configRoot);
        assertEquals(expected, fileName);

        configRoot = "io.quarkus.keycloak.Config";
        expected = "quarkus-keycloak.adoc";
        fileName = computeExtensionDocFileName(configRoot);
        assertEquals(expected, fileName);

        configRoot = "io.quarkus.extension.name.BuildTimeConfig";
        expected = "quarkus-extension-name.adoc";
        fileName = computeExtensionDocFileName(configRoot);
        assertEquals(expected, fileName);
    }

    @Test
    public void shouldPreserveExistingConfigItemsWhenAppendAnEmptyConfigItems() {
        List<ConfigDocItem> existingConfigItems = Arrays.asList(new ConfigDocItem(), new ConfigDocItem());
        appendConfigItemsIntoExistingOnes(existingConfigItems, Collections.emptyList());
        assertEquals(2, existingConfigItems.size());
    }

    @Test
    public void shouldAppendNewConfigItemsAtTheEndOfExistingConfigItems() {
        List<ConfigDocItem> existingConfigItems = new ArrayList<>(
                Arrays.asList(new ConfigDocItem(null, new ConfigDocKey()), new ConfigDocItem(null, new ConfigDocKey())));
        ConfigDocItem newItem = new ConfigDocItem(null, new ConfigDocKey());
        ConfigDocSection configDocSection = new ConfigDocSection();
        configDocSection.setSectionDetailsTitle("title");
        ConfigDocItem section = new ConfigDocItem(configDocSection, null);
        List<ConfigDocItem> newConfigItems = Arrays.asList(newItem, section);

        appendConfigItemsIntoExistingOnes(existingConfigItems, newConfigItems);

        assertEquals(4, existingConfigItems.size());
        List<ConfigDocItem> addedList = existingConfigItems.subList(2, 4);
        assertEquals(newItem, addedList.get(0));
        assertEquals(section, addedList.get(1));
    }

    @Test
    public void shouldAppendConfigSectionConfigItemsIntoExistingConfigItemsOfConfigSectionWithSameTitle() {
        ConfigDocSection existingSection = new ConfigDocSection();
        existingSection.setSectionDetailsTitle("title");
        ConfigDocItem configItem = new ConfigDocItem(null, new ConfigDocKey());
        existingSection.addConfigDocItems(Arrays.asList(configItem));

        ConfigDocItem configDocItem = new ConfigDocItem(existingSection, null);
        List<ConfigDocItem> existingConfigItems = new ArrayList<>(Arrays.asList(configDocItem));

        ConfigDocSection configDocSection = new ConfigDocSection();
        configDocSection.setSectionDetailsTitle("title");
        ConfigDocItem newConfigItem = new ConfigDocItem(null, new ConfigDocKey());
        configDocSection.addConfigDocItems(Arrays.asList(newConfigItem));
        ConfigDocItem section = new ConfigDocItem(configDocSection, null);

        appendConfigItemsIntoExistingOnes(existingConfigItems, Arrays.asList(section));

        assertEquals(1, existingConfigItems.size());
        assertEquals(2, existingSection.getConfigDocItems().size());

        assertEquals(configItem, existingSection.getConfigDocItems().get(0));
        assertEquals(newConfigItem, existingSection.getConfigDocItems().get(1));
    }

    // TODO - should deep merge be supported? Or we should only merge top level sections?
    @Test
    public void shouldDeepAppendConfigSectionConfigItemsIntoExistingConfigItemsOfConfigSectionWithSameTitle() {
        ConfigDocSection deepSection = new ConfigDocSection();
        deepSection.setSectionDetailsTitle("title");
        ConfigDocItem deepConfigKey = new ConfigDocItem(null, new ConfigDocKey());
        deepSection.addConfigDocItems(Arrays.asList(deepConfigKey));
        ConfigDocItem deepConfigItem = new ConfigDocItem(deepSection, null);

        ConfigDocSection section = new ConfigDocSection();
        section.setSectionDetailsTitle("");
        section.addConfigDocItems(Arrays.asList(deepConfigItem));

        ConfigDocItem configItemWithDeepSection = new ConfigDocItem(section, null);
        List<ConfigDocItem> existingConfigItems = new ArrayList<>(Arrays.asList(configItemWithDeepSection));

        ConfigDocSection configDocSection = new ConfigDocSection();
        configDocSection.setSectionDetailsTitle("title");
        ConfigDocItem configItem = new ConfigDocItem(null, new ConfigDocKey());
        configDocSection.addConfigDocItems(Arrays.asList(configItem));
        ConfigDocItem configDocItem = new ConfigDocItem(configDocSection, null);

        appendConfigItemsIntoExistingOnes(existingConfigItems, Arrays.asList(configDocItem));

        assertEquals(1, existingConfigItems.size());
        assertEquals(2, deepSection.getConfigDocItems().size());

        assertEquals(deepConfigKey, deepSection.getConfigDocItems().get(0));
        assertEquals(configItem, deepSection.getConfigDocItems().get(1));
    }
}
