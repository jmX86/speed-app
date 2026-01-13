package hr.fer.dippro.server;

import io.javalin.Javalin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class DataServer implements Runnable {
    private final int startRow;
    private int currentRow;

    public DataServer(int startRow) {
        this.startRow = startRow;
        this.currentRow = startRow;
    }

    @Override
    public void run() {
        Javalin app = Javalin.create().start(8080);

        // A dynamic path parameter
        app.get("/sensor-data", ctx -> {
            System.out.println("GET /sensor-data {resp_line: " + currentRow + "}");
            ctx.result(getNewLine());
        });
    }

    private String getNewLine() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("csv/vel.csv");

        String processedContent = "";

        assert inputStream != null;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            processedContent = reader.lines()
                    .skip(currentRow)
                    .limit(1)
                    .collect(Collectors.joining(""));

            currentRow++;
        }

        return processedContent;
    }
}
