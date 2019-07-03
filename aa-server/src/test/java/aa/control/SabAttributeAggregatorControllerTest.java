package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.ArpAggregationRequest;
import aa.model.ArpValue;
import aa.model.UserAttribute;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_ENTITLEMENT;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Collections.singletonList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, value =
    {"spring.profiles.active=no-csrf, dev", "attribute_authorities_config_path=classpath:testSabAttributeAuthority.yml"})
public class SabAttributeAggregatorControllerTest extends AbstractIntegrationTest {

    @Override
    protected boolean isBasicAuthenticated() {
        return true;
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Test
    public void testSabRegExp() throws IOException, URISyntaxException {
        String stubResponse = IOUtils.toString(new ClassPathResource("sab/response_empty.xml").getInputStream(), Charset.defaultCharset());
        stubFor(post(urlEqualTo("/sab")).willReturn(aResponse().withStatus(200).withBody(stubResponse)));

        UserAttribute input = new UserAttribute(NAME_ID, singletonList("urn:collab:person:example.com:admin"));
        Map<String, List<ArpValue>> arp = Collections.singletonMap(EDU_PERSON_ENTITLEMENT, Arrays.asList(new ArpValue("*", "sab")));
        ArpAggregationRequest arpAggregationRequest = new ArpAggregationRequest( singletonList(input), arp);

        RequestEntity<ArpAggregationRequest> requestEntity = new RequestEntity<>(arpAggregationRequest, headers, HttpMethod.POST,
                new URI("http://localhost:" + port + "/aa/api/client/attribute/aggregation"));

        ResponseEntity<String> re = restTemplate.exchange(requestEntity, String.class);

        ResponseEntity<List<UserAttribute>> response = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<UserAttribute>>() {
        });

        List<UserAttribute> userAttributes = response.getBody();
        assertEquals(6, userAttributes.get(0).getValues().size());
    }

}