package io.quarkus.che.di.deployment;

import io.quarkus.arc.deployment.AdditionalStereotypeBuildItem;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;


class CheDiProcessor {

    private static final String FEATURE = "che-di";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AnnotationsTransformerBuildItem beanTransformer(
            final BeanArchiveIndexBuildItem beanArchiveIndexBuildItem,
            final BuildProducer<AdditionalStereotypeBuildItem> additionalStereotypeBuildItemBuildProducer) {
        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {

            public boolean appliesTo(org.jboss.jandex.AnnotationTarget.Kind kind) {
                return kind == org.jboss.jandex.AnnotationTarget.Kind.CLASS;
            }

            public void transform(TransformationContext context) {
                if (context.getTarget().asClass().name().toString().equals("com.foo.Bar")) {
                   // context.transform().add(MyInterceptorBinding.class).done();
                }
            }
        });
    }
}
