package org.opengpa.core.agent.output;

import org.junit.jupiter.api.Test;
import org.springframework.ai.parser.BeanOutputParser;

import static org.junit.jupiter.api.Assertions.*;

class AgentOutputTest {

    private static String SIMPLE_ACTION = "{\n" +
            "  \"action\": {\n" +
            "    \"parameters\": {\n" +
            "      \"query\": \"Zverev match schedule June 7 2024\"\n" +
            "    },\n" +
            "    \"name\": \"search_web\"\n" +
            "  },\n" +
            "  \"is_final\": false,\n" +
            "  \"reasoning\": \"I need to find the latest information about Zverev's match schedule for June 7, 2024.\"\n" +
            "}";

    private static String SPECIAL_CHARACTERS = "{\n" +
            "  \"action\": {\n" +
            "    \"parameters\": {\n" +
            "      \"message\": \"Here is an example for the security configuration class:\\n\\n```java\\nimport org.springframework.context.annotation.Configuration;\\nimport org.springframework.security.config.annotation.web.builders.HttpSecurity;\\nimport org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;\\nimport org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;\\n\\n@Configuration\\n@EnableWebSecurity\\npublic class SecurityConfig extends WebSecurityConfigurerAdapter {\\n\\n    @Override\\n    protected void configure(HttpSecurity http) throws Exception {\\n        http\\n            .authorizeRequests()\\n                .antMatchers(\\\"/\\\", \\\"/login\\\").permitAll()\\n                .anyRequest().authenticated()\\n                .and()\\n            .oauth2Login()\\n                .loginPage(\\\"/login\\\")\\n                .defaultSuccessURL(\\\"/home\\\", true);\\n    }\\n}\\n```\\n\\nThis class configures HTTP security to permit all requests to the root and login page, while all other requests require authentication. It also sets up OAuth2 login with a custom login page and a default success URL.\"\n" +
            "    },\n" +
            "    \"name\": \"output_message\"\n" +
            "  },\n" +
            "  \"is_final\": true,\n" +
            "  \"reasoning\": \"The user requested an example for the security configuration class, and providing a code example directly addresses the request.\"\n" +
            "}";

    @Test
    public void testSimpleAction() {
        var outputParser = new BeanOutputParser<>(AgentOutput.class);
        AgentOutput output = outputParser.parse(SIMPLE_ACTION);
        assertEquals("Zverev match schedule June 7 2024", output.getAction().getParameters().get("query"));
    }

    @Test
    public void testSpecialCharacters() {
        var outputParser = new BeanOutputParser<>(AgentOutput.class);
        AgentOutput output = outputParser.parse(SPECIAL_CHARACTERS);
        assertEquals("output_message", output.getAction().getName());
    }

}