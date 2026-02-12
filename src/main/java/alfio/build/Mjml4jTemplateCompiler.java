package alfio.build;

import ch.digitalfondue.mjml4j.Mjml4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Mjml4jTemplateCompiler {
    private Mjml4jTemplateCompiler() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: Mjml4jTemplateCompiler <inputDir> <outputDir>");
        }

        Path inputDir = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);
        Files.createDirectories(outputDir);

        try (var paths = Files.walk(inputDir)) {
            paths.filter(path -> path.toString().endsWith(".mjml"))
                .forEach(path -> renderTemplate(path, outputDir));
        }
    }

    private static void renderTemplate(Path path, Path outputDir) {
        try {
            String input = Files.readString(path, StandardCharsets.UTF_8);
            String output = Mjml4j.render(input);
            String outputName = path.getFileName().toString().replaceFirst("\\.mjml$", ".ms");
            Files.writeString(outputDir.resolve(outputName), output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to render MJML template: " + path, e);
        }
    }
}

