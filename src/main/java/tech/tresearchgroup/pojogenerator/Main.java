package tech.tresearchgroup.pojogenerator;

import com.google.gson.GsonBuilder;
import com.speedment.common.codegen.internal.java.JavaGenerator;
import com.speedment.common.codegen.model.Class;
import com.speedment.common.codegen.model.Field;
import com.speedment.common.codegen.model.File;
import com.speedment.common.codegen.model.Value;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import tech.tresearchgroup.pojogenerator.model.DataObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {
    private static final CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException, URISyntaxException {
        generatePojo("Result");
    }

    public static Class generatePojo(String className) throws IOException, ExecutionException, InterruptedException, URISyntaxException {
        client.start();
        SimpleHttpRequest request = new SimpleHttpRequest(
            org.apache.hc.core5.http.Method.GET,
            new URI("https://us.api.blizzard.com/data/wow/connected-realm/4372/auctions/index?namespace=dynamic-classic-us&locale=en_US&access_token=USx6bqJ7RWMqJz5RaVWc9QsOzY2LejI1gW")
        );
        long start = System.currentTimeMillis();
        Future<SimpleHttpResponse> future = client.execute(request, null);
        String data = future.get().getBody().getBodyText();
        client.close();
        System.out.println("Recieved: " + data);
        JSONObject jsonObject = new JSONObject(data);

        DataObject dataObject = getKeys(className, jsonObject);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(dataObject.getAttributes()));
        long stop = System.currentTimeMillis();
        System.out.println("Finished in: " + (stop - start));
        return null;
    }

    public static DataObject getKeys(String className, JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        DataObject dataObject = new DataObject();
        dataObject.setClassName(className);

        while (keys.hasNext()) {
            String key = keys.next();
            System.out.println("Key: " + key);
            Object keyObj = jsonObject.get(key);
            if (keyObj instanceof JSONObject jsonObj) {
                String name = (String) jsonObj.names().get(0);
                DataObject subObj = getKeys(name, ((JSONObject) jsonObject.get(key)));
                dataObject.addAttribute(key, subObj);
            } else if (keyObj instanceof JSONArray) {
                DataObject subObj = getKeys(key, ((JSONArray) jsonObject.get(key)));
                dataObject.addAttribute(key, subObj);
            } else if (
                keyObj instanceof JSONString ||
                    keyObj instanceof String ||
                    keyObj instanceof Integer ||
                    keyObj instanceof Boolean ||
                    keyObj instanceof Long
            ) {
                dataObject.addAttribute(key, keyObj);
            } else {
                System.err.println("Unsupported: " + keyObj.getClass().getSimpleName());
            }
        }
        System.out.println(
        );
        Class theClass = Class.of("BasicExample").public_();

        theClass.add(
            Field.of("BASIC_MESSAGE", String.class)
                .public_().final_().static_()
                .set(Value.ofText("Hello, world!"))
        );
        new JavaGenerator().on(
            File.of("org/example/BasicExample.java")
                .add(
                    theClass
                )
        ).get();
        return dataObject;
    }

    public static DataObject getKeys(String key, JSONArray jsonArray) {
        DataObject dataObject = new DataObject();
        dataObject.setClassName(key);
        for (Object object : jsonArray) {
            if (object instanceof JSONObject jsonObj) {
                String name = (String) jsonObj.names().get(0);
                DataObject subObj = getKeys(name, jsonObj);
                dataObject.addAttribute(name, subObj);
            } else if (object instanceof JSONArray) {
                DataObject subObj = getKeys(key, (JSONArray) object);
                dataObject.addAttribute(key, subObj);
            } else if (
                object instanceof JSONString ||
                    object instanceof String ||
                    object instanceof Integer ||
                    object instanceof Boolean ||
                    object instanceof Long
            ) {
                dataObject.addAttribute(key, object);
            } else {
                System.err.println("Unsupported: " + object.getClass().getSimpleName());
            }
        }
        return dataObject;
    }
}
