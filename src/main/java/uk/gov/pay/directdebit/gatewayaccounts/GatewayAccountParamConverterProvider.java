package uk.gov.pay.directdebit.gatewayaccounts;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidGatewayAccountException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class GatewayAccountParamConverterProvider implements ParamConverterProvider {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GatewayAccountParamConverterProvider.class);

    private GatewayAccountDao gatewayAccountDao;
    public GatewayAccountParamConverterProvider(GatewayAccountDao gatewayAccountDao) {
        this.gatewayAccountDao = gatewayAccountDao;
    }

    public GatewayAccountConverter createGatewayAccountConverter() {
        return new GatewayAccountConverter();
    }
    public class GatewayAccountConverter implements ParamConverter<GatewayAccount> {

        @Override
        public GatewayAccount fromString(String value) {
            Long accountId = convertAccountId(value);
            return gatewayAccountDao
                    .findById(accountId)
                    .orElseThrow(() -> new GatewayAccountNotFoundException(accountId.toString()));
        }

        @Override
        public String toString(GatewayAccount gatewayAccount) {
            return gatewayAccount.getId().toString();
        }

        private Long convertAccountId(String param) {
            try {
                return Long.parseLong(param);
            } catch (Exception exc) {
                LOGGER.error("Could not retrieve gateway account for request from URL");
                throw new InvalidGatewayAccountException("invalid id");
            }
        }
    }
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if(rawType.equals(GatewayAccount.class)){
            return ((ParamConverter<T>) createGatewayAccountConverter());
        }
        return null;
    }
}