package alfio.build;

import ch.digitalfondue.jfiveparse.HtmlSerializer;
import ch.digitalfondue.jfiveparse.NodeMatcher;
import ch.digitalfondue.jfiveparse.Parser;
import ch.digitalfondue.jfiveparse.Selector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class FrontendIndexTransformer {
    private FrontendIndexTransformer() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: FrontendIndexTransformer <inputHtml> <outputHtml> [basePath]");
        }
        File input = new File(args[0]);
        File output = new File(args[1]);
        String basePath = args.length > 2 ? args[2] : "frontend-public/";

        output.getParentFile().mkdirs();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8)) {
            var document = new Parser().parse(reader);

            NodeMatcher scriptNodes = Selector.select().element("script").toMatcher();
            document.getAllNodesMatching(scriptNodes).forEach(node -> node.setAttribute("src", basePath + node.getAttribute("src")));

            NodeMatcher cssNodes = Selector.select().element("link").attrValEq("rel", "stylesheet").toMatcher();
            document.getAllNodesMatching(cssNodes).forEach(node -> node.setAttribute("href", basePath + node.getAttribute("href")));

            java.nio.file.Files.writeString(output.toPath(), HtmlSerializer.serialize(document), StandardCharsets.UTF_8);
        }
    }
}
