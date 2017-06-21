package aa.aggregators.orcid;

import aa.model.Account;
import aa.model.AccountType;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import aa.repository.AccountRepository;
import aa.repository.AccountRepositoryTest;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_PRINCIPAL_NAME;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static aa.aggregators.AttributeAggregator.ORCID;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrcidAttributeAggregatorTest {

    private OrcidAttributeAggregator subject;

    private List<UserAttribute> input = singletonList(new UserAttribute(NAME_ID, singletonList("urn")));

    private AccountRepository accountRepository ;

    @Before
    public void before() {
        AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("orcid");
        configuration.setEndpoint("http://localhost:8889/orcid");
        configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute()));
        this.accountRepository = mock(AccountRepository.class);
        subject = new OrcidAttributeAggregator(configuration, accountRepository);
    }

    @Test
    public void testGetOrcidHappyFlow() throws Exception {
        Account account = new Account("urn", "schacHome", AccountType.ORCID);
        account.setLinkedId("0000-0002-9588-5133");
        when(accountRepository.findByUrnIgnoreCaseAndAccountType("urn", AccountType.ORCID))
            .thenReturn(Optional.of(account));
        List<UserAttribute> userAttributes = subject.aggregate(input);

        assertEquals(1, userAttributes.size());

        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals("urn:mace:dir:attribute-def:eduPersonOrcid", userAttribute.getName());
        assertEquals("orcid", userAttribute.getSource());
        assertEquals(Collections.singletonList(account.getLinkedId()), userAttribute.getValues());
    }

    @Test
    public void testGetOrcidNotPresent() throws Exception {
        when(accountRepository.findByUrnIgnoreCaseAndAccountType("urn", AccountType.ORCID))
            .thenReturn(Optional.empty());
        List<UserAttribute> userAttributes = subject.aggregate(input);

        assertEquals(0, userAttributes.size());
    }
}