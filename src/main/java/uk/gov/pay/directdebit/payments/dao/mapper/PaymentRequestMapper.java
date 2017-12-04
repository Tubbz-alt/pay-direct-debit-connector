package uk.gov.pay.directdebit.payments.dao.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PaymentRequestMapper implements ResultSetMapper<PaymentRequest> {
    private static final String ID_COLUMN = "id";
    private static final String EXTERNAL_ID_COLUMN = "external_id";
    private static final String GATEWAY_ACCOUNT_ID_COLUMN = "gateway_account_id";
    private static final String AMOUNT_COLUMN = "amount";
    private static final String REFERENCE_COLUMN = "reference";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String RETURN_URL_COLUMN = "return_url";
    private static final String CREATED_DATE_COLUMN = "created_date";

    @Override
    public PaymentRequest map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new PaymentRequest(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(AMOUNT_COLUMN),
                resultSet.getString(RETURN_URL_COLUMN),
                resultSet.getLong(GATEWAY_ACCOUNT_ID_COLUMN),
                resultSet.getString(DESCRIPTION_COLUMN),
                resultSet.getString(REFERENCE_COLUMN),
                resultSet.getString(EXTERNAL_ID_COLUMN),
                ZonedDateTime.ofInstant(resultSet.getTimestamp(CREATED_DATE_COLUMN).toInstant(), ZoneId.of("UTC")));
    }
}