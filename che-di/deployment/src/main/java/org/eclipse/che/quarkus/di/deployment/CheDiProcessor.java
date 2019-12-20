package org.eclipse.che.quarkus.di.deployment;

import io.quarkus.arc.deployment.AdditionalStereotypeBuildItem;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.arc.processor.Transformation;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

import javax.annotation.Priority;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.jandex.AnnotationInstance.create;


public class CheDiProcessor {

    private static final String FEATURE = "che-di";


    private static final DotName JAVAX_INJECT_NAMED_ANNOTATION = DotName.createSimple("javax.inject.Named");
    private static final DotName JAVAX_INJECT_ANNOTATION = DotName.createSimple("javax.inject.Inject");


    private static final DotName MP_CONFIG_PROPERTY_ANNOTATION = DotName.createSimple(ConfigProperty.class.getName());

    private static final DotName CDI_INJECT_ANNOTATION = DotNames.INJECT;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AnnotationsTransformerBuildItem beanTransformer(
            final BeanArchiveIndexBuildItem beanArchiveIndexBuildItem,
            final BuildProducer<AdditionalStereotypeBuildItem> additionalStereotypeBuildItemBuildProducer) {
//        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {
//
//            public boolean appliesTo(org.jboss.jandex.AnnotationTarget.Kind kind) {
//                return kind == org.jboss.jandex.AnnotationTarget.Kind.CLASS;
//            }
//
//            public void transform(TransformationContext context) {
//                if (context.getTarget().asClass().name().toString().equals("com.foo.Bar")) {
//                   // context.transform().add(MyInterceptorBinding.class).done();
//                }
//            }
//        });
        return new AnnotationsTransformerBuildItem(context -> {
            final Collection<AnnotationInstance> annotations = context.getAnnotations();
            if (annotations.isEmpty()) {
                return;
            }
            final AnnotationTarget target = context.getTarget();
            // Note that only built-in scopes are used because annotation transformers can be used before custom contexts are registered
            final Set<AnnotationInstance> annotationsToAdd = getAnnotationsToAdd(target,
                    Arrays.stream(BuiltinScope.values()).map(BuiltinScope::getName).collect(Collectors.toList()));
            if (!annotationsToAdd.isEmpty()) {
                final Transformation transform = context.transform();
                for (AnnotationInstance annotationInstance : annotationsToAdd) {
                    transform
                            .remove(ann -> ann.name().toString().equals("javax.inject.Named"))
                            .add(annotationInstance);

                }
                transform.done();
            }
        });
    }


    /**
     * Map spring annotations from an annotated class to equivalent CDI annotations
     *
     * @param target The annotated class
     * @return The CDI annotations to add to the class
     */
    Set<AnnotationInstance> getAnnotationsToAdd(
            final AnnotationTarget target,
            //final Map<DotName, Set<DotName>> stereotypeScopes,
            final List<DotName> allArcScopes) {
        List<DotName> arcScopes = allArcScopes != null ? allArcScopes
                : Arrays.stream(BuiltinScope.values()).map(i -> i.getName()).collect(Collectors.toList());
       // final Set<DotName> stereotypes = stereotypeScopes.keySet();

        final Set<AnnotationInstance> annotationsToAdd = new HashSet<>();

        //if it's a class, it's a Bean or a Bean producer
        if (target.kind() == AnnotationTarget.Kind.CLASS) {

        } else if (target.kind() == AnnotationTarget.Kind.FIELD) {
            final FieldInfo fieldInfo = target.asField();
             if (fieldInfo.hasAnnotation(JAVAX_INJECT_NAMED_ANNOTATION)) {
                final AnnotationInstance annotation = fieldInfo.annotation(JAVAX_INJECT_NAMED_ANNOTATION);
                addSpringValueAnnotations(target, annotation, !fieldInfo.hasAnnotation(JAVAX_INJECT_ANNOTATION), annotationsToAdd);
            }
        } else if (target.kind() == AnnotationTarget.Kind.METHOD) {
            final MethodInfo methodInfo = target.asMethod();

            // add method parameter conversion annotations
            for (AnnotationInstance annotation : methodInfo.annotations()) {
                if (annotation.target().kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
                    if (annotation.name().equals(JAVAX_INJECT_NAMED_ANNOTATION)) {
                        addSpringValueAnnotations(annotation.target(), annotation, false, annotationsToAdd);
                    }
                }
            }

        }
        return annotationsToAdd;
    }

    private void addSpringValueAnnotations(AnnotationTarget target, AnnotationInstance annotation, boolean addInject,
                                           Set<AnnotationInstance> annotationsToAdd) {
        final AnnotationValue annotationValue = annotation.value();
        if (annotationValue == null) {
            return;
        }
        String defaultValue = null;
        String propertyName = annotationValue.asString().replace("${", "").replace("}", "");
        if (propertyName.contains(":")) {
            final int index = propertyName.indexOf(':');
            if (index < propertyName.length() - 1) {
                defaultValue = propertyName.substring(index + 1);
            }
            propertyName = propertyName.substring(0, index);

        }
        final List<AnnotationValue> annotationValues = new ArrayList<>();
        annotationValues.add(AnnotationValue.createStringValue("name", propertyName));
        if (defaultValue != null && !defaultValue.isEmpty()) {
            annotationValues.add(AnnotationValue.createStringValue("defaultValue", defaultValue));
        }
        annotationsToAdd.add(create(
                MP_CONFIG_PROPERTY_ANNOTATION,
                target,
                annotationValues));
        if (addInject) {
            annotationsToAdd.add(create(
                    CDI_INJECT_ANNOTATION,
                    target,
                    Collections.emptyList()));
        }
    }
}
