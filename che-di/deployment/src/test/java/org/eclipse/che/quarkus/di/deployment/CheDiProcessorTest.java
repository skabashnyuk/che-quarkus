package org.eclipse.che.quarkus.di.deployment;

import io.quarkus.arc.processor.BeanArchives;
import io.quarkus.deployment.util.IoUtil;
import org.eclipse.che.quarkus.di.deployment.CheDiProcessor;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheDiProcessorTest {
    final CheDiProcessor processor = new CheDiProcessor();
    final IndexView index = getIndex(Inject.class, TestChePropertiesComponent.class, TestSystemPropertiesComponent.class,
            TestEnvPropertiesComponent.class, TestConfOverrideComponent.class, TestConfOverrideWithUnderscoresComponent.class,
            TestConfAliasComponent.class, TestConfConstructorParameters.class);


    private IndexView getIndex(final Class<?>... classes) {
        final Indexer indexer = new Indexer();
        for (final Class<?> clazz : classes) {
            final String className = clazz.getName();
            try (InputStream stream = IoUtil.readClass(getClass().getClassLoader(), className)) {
                final ClassInfo beanInfo = indexer.index(stream);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to index: " + className, e);
            }
        }
        return BeanArchives.buildBeanArchiveIndex(indexer.complete());
    }

    @Test
    public void getAnnotationsToAddBeanMethodExplicitSingleton() {
//        final Map<DotName, Set<DotName>> scopes = processor
//        System.out.println(index);

        final MethodInfo target = index.getClassByName(DotName.createSimple(TestConfConstructorParameters.class.getName()))
                .methods().get(0);


        final Set<AnnotationInstance> ret = processor.getAnnotationsToAdd(target,  null);
        assertEquals( 1,ret.size());
//        final Set<AnnotationInstance> expected = setOf(
//                AnnotationInstance.create(DotName.createSimple(RequestScoped.class.getName()), target,
//                        Collections.emptyList()));
//        assertEquals(expected, ret);
    }


    @Test
    public void getAnnotationsToAddBeanMethodExplicitSingleton2() {
//        final Map<DotName, Set<DotName>> scopes = processor
//        System.out.println(index);

        final FieldInfo target = index.getClassByName(DotName.createSimple(TestChePropertiesComponent.class.getName())).field("parameter_int");



        final Set<AnnotationInstance> ret = processor.getAnnotationsToAdd(target,  null);
        assertEquals( 1,ret.size());
//        final Set<AnnotationInstance> expected = setOf(
//                AnnotationInstance.create(DotName.createSimple(RequestScoped.class.getName()), target,
//                        Collections.emptyList()));
//        assertEquals(expected, ret);
    }


    static class TestChePropertiesComponent {
        @Named("test_int")
        @javax.inject.Inject
        int parameter_int;

        @Named("test_int")
        @javax.inject.Inject
        int parameter_long;

        @Named("test_bool")
        @javax.inject.Inject
        boolean parameter_bool;

        @Named("test_uri")
        @javax.inject.Inject
        URI parameter_uri;

        @Named("test_url")
        @javax.inject.Inject
        URL parameter_url;

        @Named("test_file")
        @javax.inject.Inject
        File parameter_file;

        @Named("test_strings")
        @javax.inject.Inject
        String[] parameter_strings;

//        @Named("test_pair_of_strings")
//        @javax.inject.Inject
//        Pair<String, String> parameter_pair;
//
//        @Named("test_pair_of_strings2")
//        @javax.inject.Inject
//        Pair<String, String> parameter_pair2;
//
//        @Named("test_pair_of_strings3")
//        @javax.inject.Inject
//        Pair<String, String> parameter_pair3;
//
//        @Named("test_pair_array")
//        @javax.inject.Inject
//        Pair<String, String>[] parameter_pair_array;

        @Named("some.dir.in_tmp_dir")
        @javax.inject.Inject
        File someDir;

        @Named("suffixed.PATH")
        @javax.inject.Inject
        String suffixedPath;

        @Named("nullable")
        @javax.inject.Inject
        // @Nullable
                String nullable;
    }

    static class TestSystemPropertiesComponent {
        @Named("sys.test_int")
        @javax.inject.Inject
        int parameter_int;

        @Named("sys.test_int")
        @javax.inject.Inject
        int parameter_long;

        @Named("sys.test_bool")
        @javax.inject.Inject
        boolean parameter_bool;

        @Named("sys.test_uri")
        @javax.inject.Inject
        URI parameter_uri;

        @Named("sys.test_url")
        @javax.inject.Inject
        URL parameter_url;

        @Named("sys.test_file")
        @javax.inject.Inject
        File parameter_file;

        @Named("sys.test_strings")
        @javax.inject.Inject
        String[] parameter_strings;

//        @Named("sys.test_pair_of_strings")
//        @javax.inject.Inject
//        Pair<String, String> parameter_pair;
//
//        @Named("sys.test_pair_of_strings2")
//        @javax.inject.Inject
//        Pair<String, String> parameter_pair2;
//
//        @Named("sys.test_pair_of_strings3")
//        @javax.inject.Inject
//        Pair<String, String> parameter_pair3;
//
//        @Named("sys.test_pair_array")
//        @javax.inject.Inject
//        Pair<String, String>[] parameter_pair_array;

        @Named("sys.some.dir.in_tmp_dir")
        @javax.inject.Inject
        File someDir;

        @Named("sys.suffixed.PATH")
        @javax.inject.Inject
        String suffixedPath;

        @Named("sys.nullable")
        @javax.inject.Inject
        // @Nullable
                String nullable;
    }

    static class TestEnvPropertiesComponent {
        @Named("env.PATH")
        @javax.inject.Inject
        String string;
    }

    static class TestConfOverrideComponent {
        @Named("che.some.name")
        @javax.inject.Inject
        // @Nullable
                String string;

        @Named("che.some.other.name")
        @javax.inject.Inject
        //@Nullable
                String otherString;
    }

    static class TestConfOverrideWithUnderscoresComponent {
        @Named("che.some.name")
        @javax.inject.Inject
        // @Nullable
                String string;

        @Named("che.some.other.name_with_underscores")
        @javax.inject.Inject
        //  @Nullable
                String otherString;
    }

    static class TestConfAliasComponent {
        @Named("che.some.name")
        @javax.inject.Inject
        String string;

        @Named("new.some.name")
        @javax.inject.Inject
        String otherString;

        @Named("very.new.some.name")
        @javax.inject.Inject
        String otherOtherString;
    }


    static class TestConfConstructorParameters {
        @Inject
        public TestConfConstructorParameters(@Named("very.new.some.name") String name) {
        }
    }
}
