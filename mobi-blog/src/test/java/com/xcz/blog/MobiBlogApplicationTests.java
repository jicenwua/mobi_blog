package com.xcz.blog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

//@SpringBootTest
class MobiBlogApplicationTests {

    /**
     * 验证 Spring 上下文能否正常加载
     */
    @Test
    void contextLoads() throws JsonProcessingException {

        String config ="{\n" +
                "  \"weather_wallpaper_config\": {\n" +
                "    \"sunnyUrl\": \"https://...\",\n" +
                "    \"cloudyUrl\": \"https://...\",\n" +
                "    \"rainyUrl\": \"https://...\"\n" +
                "  }\n" +
                "}";

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode root = objectMapper.readTree(config);
        System.out.println(root);
        String configKey = root.fieldNames().next();
        JsonNode valueNode = root.get(configKey);
        String configJson = valueNode.isTextual()
                ? valueNode.asText()
                : objectMapper.writeValueAsString(valueNode);
        System.out.println(configJson);
    }

}
