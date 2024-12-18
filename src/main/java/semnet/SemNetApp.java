package semnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import io.javalin.Javalin;
import io.javalin.rendering.JavalinRenderer;
import io.javalin.rendering.template.JavalinThymeleaf;

/**
 * SemNetAppクラス
 */
public class SemNetApp {
    public static void main(String[] args) {
        // H2データベースの初期化
        Database.initializeDatabase();

        // Thymeleafのテンプレート設定
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");

        // TemplateEngineの設定
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        // JavalinにThymeleafを登録
        JavalinRenderer.register(new JavalinThymeleaf(templateEngine), ".html");

        // Javalinアプリの作成
        Javalin app = Javalin.create().start(7000);

        app.get("/semnet", ctx -> {
            Map<String, Object> model = new HashMap<>();
            String queryStr = ctx.queryParam("queryStr");
            SemanticNet sn = new SemanticNet();
            //セマンティックネットを初期化
            sn.initialize();
            
            if (sn.isEmpty()) {
                sn.addInitialLinks();
            }

            model.put("sn", sn);
//            if (queryStr != null) {
//                ArrayList<Link> query = strToQuery(queryStr);
//                String result = sn.query(query);
//                model.put("result", result);
//            } else {
                queryStr = "?x is-a ?y\n?y has-a ?z";
//            }
            model.put("query", queryStr);
            ctx.render("/semnet.html", model);
        });

        app.post("/semnet", ctx -> {
            Map<String, Object> model = new HashMap<>();
            String queryStr = ctx.formParam("queryStr");
            SemanticNet sn = new SemanticNet();

            if (sn.isEmpty()) {
                sn.addInitialLinks();
            }

            model.put("sn", sn);

            if (queryStr != null) {
                ArrayList<Link> query = strToQuery(queryStr);
                String result = sn.query(query);
                model.put("result", result);
            }
            model.put("query", queryStr);
            ctx.render("/semnet.html", model);
        });
    }

    private static ArrayList<Link> strToQuery(String queryStr) {
        ArrayList<Link> query = new ArrayList<>();
        if (queryStr != null) {
            String[] lines = queryStr.split("\n");
            for (String line : lines) {
                line = line.trim();
                String[] tokens = line.split("\\s+");
                if (tokens.length == 3) {
                    query.add(new Link(tokens[1], tokens[0], tokens[2], new SemanticNet()));
                }
            }
        }
        return query;
    }
}


