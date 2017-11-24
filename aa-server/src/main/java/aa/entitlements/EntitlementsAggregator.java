package aa.entitlements;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EntitlementsAggregator extends AbstractAttributeAggregator {

    private String token;

    public EntitlementsAggregator(AttributeAuthorityConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected RestTemplate initializeRestTemplate(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        int timeOut = attributeAuthorityConfiguration.getTimeOut();
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(timeOut);
        httpRequestFactory.setConnectTimeout(timeOut);
        httpRequestFactory.setReadTimeout(timeOut);
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
        return restTemplate;
    }

    @Override
    public List<UserAttribute> aggregate(List<UserAttribute> input) {
        return doAggregate(input, true);
    }

    private List<UserAttribute> doAggregate(List<UserAttribute> input, boolean retryBadToken) {
        String eduPersonPrincipalName = getUserAttributeSingleValue(input, EDU_PERSON_PRINCIPAL_NAME);
        AttributeAuthorityConfiguration configuration = super.getAttributeAuthorityConfiguration();

        if (StringUtils.isEmpty(this.token)) {
            this.token = obtainToken();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer".concat(" ").concat(this.token));

        HttpEntity<Map<String, String>> request = new HttpEntity<>(headers);
        String endPoint = configuration.getEndpoint().concat("/api/Entitlement/{name}");
        try {
            ResponseEntity<List> response = getRestTemplate().exchange(endPoint, HttpMethod.GET, request, List.class,
                eduPersonPrincipalName);
            List<Map<String, String>> body = response.getBody();
            List<String> values = body.stream().map(m -> m.entrySet().stream().findFirst().map(entry ->
                entry.getKey().concat(":").concat(entry.getValue()))).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());

            return mapValuesToUserAttribute(EDU_PERSON_ENTITLEMENT, values);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED) && retryBadToken) {
                return doAggregate(input, false);
            }
            throw e;
        }
    }

    private String obtainToken() {
        AttributeAuthorityConfiguration configuration = super.getAttributeAuthorityConfiguration();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", configuration.getUser());
        map.add("password", configuration.getPassword());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = getRestTemplate().postForEntity(configuration.getEndpoint().concat
            ("/Token"), request, Map.class);
        Object accessToken = response.getBody().get("access_token");
        return String.class.cast(accessToken);
    }
}
