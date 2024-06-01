package edu.neu.ccs.prl.zeugma.internal.parametric;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import com.pholser.junit.quickcheck.internal.generator.GeneratorRepository;
import com.pholser.junit.quickcheck.internal.generator.ServiceLoaderGeneratorSource;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

class StructuredInputGenerator {
    private static final GeneratorRepository BASE_REPOSITORY =
            new GeneratorRepository(new SourceOfRandomness(new Random())).register(new ServiceLoaderGeneratorSource());
    private static final long DEFAULT_SEED = 41;
    private final GeneratorRepository repository = createRepository();
    private final List<Generator<?>> generators;

    public StructuredInputGenerator(Executable executable) {
        this(getParameterTypeContexts(executable));
    }

    public StructuredInputGenerator(List<ParameterTypeContext> contexts) {
        this.generators = Collections.unmodifiableList(contexts.stream()
                .map(x -> createGenerator(repository, x))
                .collect(Collectors.toList()));
    }

    public Object[] generate(SourceOfRandomness source, GenerationStatus status) {
        return generators.stream().map(g -> g.generate(source, status)).toArray();
    }

    private static GeneratorRepository createRepository() {
        return (GeneratorRepository) BASE_REPOSITORY.withRandom(new SourceOfRandomness(new Random(DEFAULT_SEED)));
    }

    private static Generator<?> createGenerator(GeneratorRepository repository, ParameterTypeContext context) {
        Generator<?> generator = repository.generatorFor(context);
        generator.provide(repository);
        generator.configure(context.annotatedType());
        if (context.topLevel()) {
            generator.configure(context.annotatedElement());
        }
        return generator;
    }

    public static List<ParameterTypeContext> getParameterTypeContexts(Executable executable) {
        return Arrays.stream(executable.getParameters())
                .map(p -> ParameterTypeContext.forParameter(p).annotate(p))
                .collect(Collectors.toList());
    }
}
