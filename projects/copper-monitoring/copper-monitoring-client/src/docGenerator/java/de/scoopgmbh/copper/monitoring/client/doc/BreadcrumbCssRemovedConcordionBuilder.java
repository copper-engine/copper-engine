/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.monitoring.client.doc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.concordion.Concordion;
import org.concordion.api.Command;
import org.concordion.api.EvaluatorFactory;
import org.concordion.api.Resource;
import org.concordion.api.Source;
import org.concordion.api.SpecificationLocator;
import org.concordion.api.SpecificationReader;
import org.concordion.api.Target;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.extension.ConcordionExtensionFactory;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFalseListener;
import org.concordion.api.listener.AssertTrueListener;
import org.concordion.api.listener.ConcordionBuildEvent;
import org.concordion.api.listener.ConcordionBuildListener;
import org.concordion.api.listener.DocumentParsingListener;
import org.concordion.api.listener.ExecuteListener;
import org.concordion.api.listener.RunListener;
import org.concordion.api.listener.SpecificationProcessingListener;
import org.concordion.api.listener.ThrowableCaughtListener;
import org.concordion.api.listener.VerifyRowsListener;
import org.concordion.internal.ClassNameBasedSpecificationLocator;
import org.concordion.internal.ClassPathSource;
import org.concordion.internal.CommandRegistry;
import org.concordion.internal.DocumentParser;
import org.concordion.internal.FileTarget;
import org.concordion.internal.SimpleEvaluatorFactory;
import org.concordion.internal.XMLParser;
import org.concordion.internal.XMLSpecificationReader;
import org.concordion.internal.command.AssertEqualsCommand;
import org.concordion.internal.command.AssertFalseCommand;
import org.concordion.internal.command.AssertTrueCommand;
import org.concordion.internal.command.EchoCommand;
import org.concordion.internal.command.ExecuteCommand;
import org.concordion.internal.command.LocalTextDecorator;
import org.concordion.internal.command.RunCommand;
import org.concordion.internal.command.SetCommand;
import org.concordion.internal.command.SpecificationCommand;
import org.concordion.internal.command.ThrowableCatchingDecorator;
import org.concordion.internal.command.ThrowableCaughtPublisher;
import org.concordion.internal.command.VerifyRowsCommand;
import org.concordion.internal.listener.AssertResultRenderer;
import org.concordion.internal.listener.DocumentStructureImprover;
import org.concordion.internal.listener.JavaScriptEmbedder;
import org.concordion.internal.listener.JavaScriptLinker;
import org.concordion.internal.listener.MetadataCreator;
import org.concordion.internal.listener.RunResultRenderer;
import org.concordion.internal.listener.SpecificationExporter;
import org.concordion.internal.listener.StylesheetEmbedder;
import org.concordion.internal.listener.StylesheetLinker;
import org.concordion.internal.listener.ThrowableRenderer;
import org.concordion.internal.listener.VerifyRowsResultRenderer;
import org.concordion.internal.util.Announcer;
import org.concordion.internal.util.Check;

public class BreadcrumbCssRemovedConcordionBuilder implements ConcordionExtender {

    private Announcer<ConcordionBuildListener> listeners = Announcer.to(ConcordionBuildListener.class);

    public static final String NAMESPACE_CONCORDION_2007 = "http://www.concordion.org/2007/concordion";
    private static final String PROPERTY_OUTPUT_DIR = "concordion.output.dir";
    private static final String PROPERTY_EXTENSIONS = "concordion.extensions";
//	private static final String EMBEDDED_STYLESHEET_RESOURCE = "/org/concordion/internal/resource/embedded.css";

    private SpecificationLocator specificationLocator = new ClassNameBasedSpecificationLocator();
    private Source source = new ClassPathSource();
    private Target target = null;
    private CommandRegistry commandRegistry = new CommandRegistry();
    private DocumentParser documentParser = new DocumentParser(commandRegistry);
    private SpecificationReader specificationReader;
    private EvaluatorFactory evaluatorFactory = new SimpleEvaluatorFactory();
    private SpecificationCommand specificationCommand = new SpecificationCommand();
    private AssertEqualsCommand assertEqualsCommand = new AssertEqualsCommand();
    private AssertTrueCommand assertTrueCommand = new AssertTrueCommand();
    private AssertFalseCommand assertFalseCommand = new AssertFalseCommand();
    private ExecuteCommand executeCommand = new ExecuteCommand();
    private RunCommand runCommand = new RunCommand();
    private VerifyRowsCommand verifyRowsCommand = new VerifyRowsCommand();
    private EchoCommand echoCommand = new EchoCommand();
    private ThrowableCaughtPublisher throwableListenerPublisher = new ThrowableCaughtPublisher();
    private LinkedHashMap<String, Resource> resourceToCopyMap = new LinkedHashMap<String, Resource>();
    private List<SpecificationProcessingListener> specificationProcessingListeners = new ArrayList<SpecificationProcessingListener>();

    public BreadcrumbCssRemovedConcordionBuilder() {
        this.throwableListenerPublisher = new ThrowableCaughtPublisher();
        withThrowableListener(new ThrowableRenderer());

        commandRegistry.register("", "specification", specificationCommand);
        withApprovedCommand(NAMESPACE_CONCORDION_2007, "run", runCommand);
        withApprovedCommand(NAMESPACE_CONCORDION_2007, "execute", executeCommand);
        withApprovedCommand(NAMESPACE_CONCORDION_2007, "set", new SetCommand());
        withApprovedCommand(NAMESPACE_CONCORDION_2007, "assertEquals", assertEqualsCommand);
        withApprovedCommand(NAMESPACE_CONCORDION_2007, "assertTrue", assertTrueCommand);
        withApprovedCommand(NAMESPACE_CONCORDION_2007, "assertFalse", assertFalseCommand);
        withApprovedCommand(NAMESPACE_CONCORDION_2007, "verifyRows", verifyRowsCommand);
        withApprovedCommand(NAMESPACE_CONCORDION_2007, "echo", echoCommand);

        AssertResultRenderer assertRenderer = new AssertResultRenderer();
        withAssertEqualsListener(assertRenderer);
        withAssertTrueListener(assertRenderer);
        withAssertFalseListener(assertRenderer);
        withVerifyRowsListener(new VerifyRowsResultRenderer());
        withRunListener(new RunResultRenderer());
        withDocumentParsingListener(new DocumentStructureImprover());
        withDocumentParsingListener(new MetadataCreator());
//		String stylesheetContent = IOUtil.readResourceAsString(EMBEDDED_STYLESHEET_RESOURCE);
        //withEmbeddedCSS(stylesheetContent);
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withSource(Source source) {
        this.source = source;
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withTarget(Target target) {
        this.target = target;
        return this;
    }


    public BreadcrumbCssRemovedConcordionBuilder withEvaluatorFactory(EvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = evaluatorFactory;
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withThrowableListener(ThrowableCaughtListener throwableListener) {
        this.throwableListenerPublisher = new ThrowableCaughtPublisher();
        throwableListenerPublisher.addThrowableListener(throwableListener);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withAssertEqualsListener(AssertEqualsListener listener) {
        assertEqualsCommand.addAssertEqualsListener(listener);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withAssertTrueListener(AssertTrueListener listener) {
        assertTrueCommand.addAssertListener(listener);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withAssertFalseListener(AssertFalseListener listener) {
        assertFalseCommand.addAssertListener(listener);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withVerifyRowsListener(VerifyRowsListener listener) {
        verifyRowsCommand.addVerifyRowsListener(listener);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withRunListener(RunListener listener) {
        runCommand.addRunListener(listener);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withExecuteListener(ExecuteListener listener) {
        executeCommand.addExecuteListener(listener);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withDocumentParsingListener(DocumentParsingListener listener) {
        documentParser.addDocumentParsingListener(listener);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withSpecificationProcessingListener(SpecificationProcessingListener listener) {
        specificationProcessingListeners.add(listener);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withBuildListener(ConcordionBuildListener listener) {
        listeners.addListener(listener);
        return this;
    }

    private BreadcrumbCssRemovedConcordionBuilder withApprovedCommand(String namespaceURI, String commandName, Command command) {
        ThrowableCatchingDecorator throwableCatchingDecorator = new ThrowableCatchingDecorator(new LocalTextDecorator(command));
        throwableCatchingDecorator.addThrowableListener(throwableListenerPublisher);
        Command decoratedCommand = throwableCatchingDecorator;
        commandRegistry.register(namespaceURI, commandName, decoratedCommand);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withCommand(String namespaceURI, String commandName, Command command) {
        Check.notEmpty(namespaceURI, "Namespace URI is mandatory");
        Check.notEmpty(commandName, "Command name is mandatory");
        Check.notNull(command, "Command is null");
        Check.isFalse(namespaceURI.contains("concordion.org"), "The namespace URI for user-contributed command '" + commandName + "' "
                + "must not contain 'concordion.org'. Use your own domain name instead.");
        return withApprovedCommand(namespaceURI, commandName, command);
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withResource(String sourcePath, Resource targetResource) {
        resourceToCopyMap.put(sourcePath, targetResource);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withEmbeddedCSS(String css) {
        StylesheetEmbedder embedder = new StylesheetEmbedder(css);
        withDocumentParsingListener(embedder);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withLinkedCSS(String cssPath, Resource targetResource) {
        withResource(cssPath, targetResource);
        StylesheetLinker cssLinker = new StylesheetLinker(targetResource);
        withDocumentParsingListener(cssLinker);
        withSpecificationProcessingListener(cssLinker);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withEmbeddedJavaScript(String javaScript) {
        JavaScriptEmbedder embedder = new JavaScriptEmbedder(javaScript);
        withDocumentParsingListener(embedder);
        return this;
    }

    @Override
    public BreadcrumbCssRemovedConcordionBuilder withLinkedJavaScript(String jsPath, Resource targetResource) {
        withResource(jsPath, targetResource);
        JavaScriptLinker javaScriptLinker = new JavaScriptLinker(targetResource);
        withDocumentParsingListener(javaScriptLinker);
        withSpecificationProcessingListener(javaScriptLinker);
        return this;
    }


    public Concordion build() {
        if (target == null) {
            target = new FileTarget(getBaseOutputDir());
        }
        XMLParser xmlParser = new XMLParser();

//		specificationCommand.addSpecificationListener(new BreadcrumbRenderer(source, xmlParser));
//		specificationCommand.addSpecificationListener(new PageFooterRenderer(target));

        specificationReader = new XMLSpecificationReader(source, xmlParser, documentParser);

        addExtensions();
        copyResources();

        addSpecificationListeners();

        specificationCommand.addSpecificationListener(new SpecificationExporter(target));

        listeners.announce().concordionBuilt(new ConcordionBuildEvent(target));

        return new Concordion(specificationLocator, specificationReader, evaluatorFactory);
    }

    private void addSpecificationListeners() {
        for (SpecificationProcessingListener listener : specificationProcessingListeners) {
            specificationCommand.addSpecificationListener(listener);
        }
    }

    private void copyResources() {
        for (Entry<String, Resource> resourceToCopy : resourceToCopyMap.entrySet()) {
            String sourcePath = resourceToCopy.getKey();
            Resource targetResource = resourceToCopy.getValue();
            try {
                InputStream inputStream = source.createInputStream(new Resource(sourcePath));
                target.copyTo(targetResource, inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy " + sourcePath + " to target " + targetResource, e);
            }
        }
    }

    private void addExtensions() {
        String extensionProp = System.getProperty(PROPERTY_EXTENSIONS);
        if (extensionProp != null) {
            String[] extensions = extensionProp.split("\\s*,\\s*");
            for (String className : extensions) {
                addExtension(className);
            }
        }
    }

    private void addExtension(String className) {
        Class<?> extensionClass;
        try {
            extensionClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find extension '" + className + "' on classpath", e);
        }
        Object extensionObject;
        try {
            extensionObject = extensionClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Cannot instantiate extension '" + className + "'", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Extension '" + className + "' or constructor are inaccessible", e);
        }

        ConcordionExtension extension;
        try {
            extension = (ConcordionExtension) extensionObject;
        } catch (ClassCastException e) {
            try {
                ConcordionExtensionFactory factory = (ConcordionExtensionFactory) extensionObject;
                extension = factory.createExtension();
            } catch (ClassCastException e1) {
                String message = String.format("Extension class '%s' must implement '%s' or '%s'", className,
                        ConcordionExtension.class.getName(), ConcordionExtensionFactory.class.getName());
                throw new RuntimeException(message);
            }
        }
        extension.addTo(this);
    }

    private File getBaseOutputDir() {
        String outputPath = System.getProperty(PROPERTY_OUTPUT_DIR);
        if (outputPath == null) {
            return new File(System.getProperty("java.io.tmpdir"), "concordion");
        }
        return new File(outputPath);
    }

}
