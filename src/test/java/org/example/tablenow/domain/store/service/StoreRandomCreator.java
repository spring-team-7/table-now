package org.example.tablenow.domain.store.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class StoreRandomCreator {

    private List<String> adjectives;
    private List<String> nouns;
    private List<String> description_templates;
    private List<String> areas;
    private List<String> road_names;

    private final Random random = new Random();

    public StoreRandomCreator() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(new File("src/test/java/org/example/tablenow/domain/store/resources/store_data.json"));

        adjectives = objectMapper.readValue(node.get("adjectives").toString(), List.class);
        nouns = objectMapper.readValue(node.get("nouns").toString(), List.class);
        description_templates = objectMapper.readValue(node.get("description_templates").toString(), List.class);
        areas = objectMapper.readValue(node.get("areas").toString(), List.class);
        road_names = objectMapper.readValue(node.get("road_names").toString(), List.class);
    }

    public Map<String, String> createRandomStoreInfo() {
        Map<String, String> storeInfo = new HashMap<>();
        String adjective = adjectives.get(random.nextInt(adjectives.size()));
        String noun = nouns.get(random.nextInt(nouns.size()));
        storeInfo.put("name", String.format("%s %s", adjective, noun));

        String description_temp = description_templates.get(random.nextInt(description_templates.size()));
        storeInfo.put("description", String.format(description_temp, noun));
        return storeInfo;
    }

    public String createRandomAddress() {
        String area = areas.get(random.nextInt(areas.size()));
        String roadName = road_names.get(random.nextInt(road_names.size()));
        return String.format("%s시 %d구 %s %d번길 %d", area,
                random.nextInt(1, 20),
                roadName,
                random.nextInt(10, 300),
                random.nextInt(1, 100));
    }
}
