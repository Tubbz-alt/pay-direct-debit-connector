package uk.gov.pay.directdebit.webhook.gocardless.support;

import com.google.common.io.Resources;
import org.junit.Test;
import uk.gov.pay.directdebit.webhook.gocardless.exception.InvalidWebhookException;

import static com.google.common.base.Charsets.UTF_8;

/**
 * Secret and signatures from the tests for the GoCardless Pro Java client library example
 *
 * @see <a href="https://github.com/gocardless/gocardless-pro-java-example/blob/2d2b04f46f5786ededd14b48cf2e63a5d9d375d3/src/test/java/com/enterprisesolutions/core/WebhookVerifierTest.java">GoCardlessGoCardlessWebhookVerifierTest</a></a>
 */
public class GoCardlessGoCardlessWebhookVerifierTest {

    private final GoCardlessWebhookVerifier verifier = new GoCardlessWebhookVerifier(
            new GoCardlessWebhookSignatureCalculator("ElfJ-3tF9I_zutNVK2lBABQrw-BgAhkZKIlvmbgk"));

    @Test
    public void shouldVerifyWebhookWithCorrectSignature() throws Exception {
        String signature = "4d48a688e8bd6c313e3eecc78fa55b3e4ae23c65e70cf35038010f47742fb670";
        String body = Resources.toString(Resources.getResource("gocardless-webhook.json"), UTF_8);

        verifier.verify(body, signature);
    }

    @Test(expected = InvalidWebhookException.class)
    public void shouldThrowForWebhookWithIncorrectSignature() throws Exception {
        String signature = "4d48a688e8bd6c313e3eecc78fa55b3e4ae23c65e70cf35038010f47742fb671";
        String body = Resources.toString(Resources.getResource("gocardless-webhook.json"), UTF_8);

        verifier.verify(body, signature);
    }

}
